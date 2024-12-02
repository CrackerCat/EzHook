package com.wulinpeng.ezhook.compiler.visitor

import com.wulinpeng.ezhook.compiler.EzHookInfo
import com.wulinpeng.ezhook.compiler.copyFunctionToParent
import com.wulinpeng.ezhook.compiler.isClassMember
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irBlockBody
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.name.Name

/**
 * author: wulinpeng
 * create: 2024/11/22 23:16
 * desc: transform functions which need to be hooked
 */
class EzHookIrTransformer(val collectInfos: List<EzHookInfo>, val pluginContext: CommonBackendContext): IrElementTransformerVoidWithContext() {

    companion object {
        private const val LAST_PARAM_NAME = "ez_hook_origin"
        private const val NEW_FUNCTION_SUFFIX = "_ez_hook"
    }

    override fun visitFunctionNew(function: IrFunction): IrStatement {
        val hookInfo = findHookInfo(function) ?: return super.visitFunctionNew(function)
        // 0. if hook function is set to inline, copy the function to the target function's parent
        val hookFunction = if (hookInfo.inline) {
            hookInfo.function.copyFunctionToParent(function.name.asString(), function.parent)
        } else {
            hookInfo.function
        }
        // 1. copy the origin function for originCall
        val newFunction = function.copyFunctionToParent("${function.name.asString()}$NEW_FUNCTION_SUFFIX")
        // 2. add param to the hook function when function is a class member function
        if (function.isClassMember()) {
            hookFunction.addValueParameter(Name.identifier(LAST_PARAM_NAME), function.parentAsClass.defaultType)
        }
        // 3. replace 'callOrigin()' of the hook function's body
        hookFunction.transform(EzHookCallOriginTransformer(hookFunction, newFunction, pluginContext), null)

        // 4. transform the origin function to call the hook function
        println("EzHook: Hooking ${function.name} with ${hookFunction.name}")
        function.body = pluginContext.createIrBuilder(function.symbol).irBlockBody(function) {

            val result = createTmpVariable(
                irExpression = irCall(hookFunction).apply {
                    function.valueParameters.forEachIndexed { index, param ->
                        putValueArgument(index, irGet(param))
                    }
                    // put dispatch receiver as the last param if exist
                    if (function.isClassMember()) {
                        putValueArgument(function.valueParameters.size, irGet(function.dispatchReceiverParameter!!))
                    }
                },
                nameHint = "returnValue",
                origin = IrDeclarationOrigin.DEFINED
            )
            +irReturn(irGet(result))
        }
        return function
    }

    private fun findHookInfo(function: IrFunction): EzHookInfo? {
        return collectInfos.filter {
            // match function name
            it.targetFunctionFqName == function.kotlinFqName.asString()
        }.filter {
            // match parameter
            val hookFunction = it.function
            hookFunction.valueParameters.size == function.valueParameters.size
        }.filter {
            // match parameter type
            it.function.valueParameters.zip(function.valueParameters).all { (hookParam, param) ->
                hookParam.type.getClass()!!.kotlinFqName!!.asString() == param.type.getClass()!!.kotlinFqName!!.asString()
            }
        }.apply {
            assert(size <= 1) {
                "Find more than 1 functions to be Hooked"
            }
        }.firstOrNull()
    }
}

class EzHookCallOriginTransformer(val hookFunction: IrFunction, val targetFunction: IrFunction, val context: CommonBackendContext): IrElementTransformerVoidWithContext() {

    companion object {
        private const val CALL_ORIGIN = "com.wulinpeng.ezhook.runtime.callOrigin"
    }

    /**
     * variables to override this function params
     */
    private val overrideParams = mutableListOf<IrVariable>()

    override fun visitVariable(declaration: IrVariable): IrStatement {
        val name = declaration.name.asString()
        val type = declaration.type.getClass()!!.kotlinFqName!!.asString()
        if (hookFunction.valueParameters.any {
            it.name.asString() == name && it.type.getClass()!!.kotlinFqName!!.asString() == type
        }) {
            overrideParams.add(declaration)
        }
        return super.visitVariable(declaration)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.symbol.owner.fqNameWhenAvailable?.asString() == CALL_ORIGIN) {
            context.createIrBuilder(hookFunction.symbol).apply {
                return irCall(targetFunction.symbol).apply {
                    // for class member function, make the last param as dispatch receiver
                    if (targetFunction.isClassMember()) {
                        val lastParam = hookFunction.valueParameters.last()
                        dispatchReceiver = irGet(lastParam)
                    }
                    for (i in 0 until targetFunction.valueParameters.size) {
                        val valueParam = targetFunction.valueParameters[i]
                        // override param
                        val irVariable = overrideParams.find { it.name.asString() == valueParam.name.asString() }
                        putValueArgument(i, irGet(irVariable ?: valueParam))
                    }
                }
            }
        } else {
            return super.visitCall(expression)
        }
    }
}