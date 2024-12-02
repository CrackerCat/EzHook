@file:OptIn(UnsafeCastFunction::class)

package com.wulinpeng.ezhook.compiler

import com.wulinpeng.ezhook.compiler.hook.IrLoweringHookExtension
import com.wulinpeng.ezhook.compiler.visitor.EzHookCollectorVisitor
import com.wulinpeng.ezhook.compiler.visitor.EzHookIrTransformer
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction

class EzHookExtension: IrLoweringHookExtension {
    val collectInfos = mutableListOf<EzHookInfo>()

    override fun traverse(context: CommonBackendContext, module: IrModuleFragment) {
        module.accept(EzHookCollectorVisitor(collectInfos), null)
    }

    override fun transform(context: CommonBackendContext, module: IrModuleFragment) {
        module.transform(EzHookIrTransformer(collectInfos, context), null)
    }
}

data class EzHookInfo(val function: IrFunction, val targetFunctionFqName: String, val inline: Boolean)