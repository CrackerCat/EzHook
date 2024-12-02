@file:OptIn(UnsafeCastFunction::class)

package com.wulinpeng.ezhook.compiler.visitor

import com.wulinpeng.ezhook.compiler.EzHookInfo
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.interpreter.getAnnotation
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isTopLevel
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction

/**
 * author: wulinpeng
 * create: 2024/11/22 23:15
 * desc: collect functions with @EzHook annotation
 */
class EzHookCollectorVisitor(val collectInfo: MutableList<EzHookInfo>): IrElementVisitor<Unit, Nothing?> {

    companion object {
        private const val EZ_HOOK_ANNOTATION = "com.wulinpeng.ezhook.runtime.EzHook"
    }

    override fun visitElement(element: IrElement, data: Nothing?) {
    }

    override fun visitModuleFragment(declaration: IrModuleFragment, data: Nothing?) {
        println("EzHook: visitModuleFragment ${declaration.name}")
        declaration.acceptChildren(this, data)
    }

    override fun visitFile(declaration: IrFile, data: Nothing?) {
        declaration.acceptChildren(this, data)
    }

    override fun visitClass(declaration: IrClass, data: Nothing?) {
        declaration.acceptChildren(this, data)
    }

    override fun visitFunction(declaration: IrFunction, data: Nothing?) {
        if (declaration.hasAnnotation(FqName(EZ_HOOK_ANNOTATION))) {
            if (!declaration.isTopLevel) {
                error("EzHook annotation can only be used on top level functions")
            }
            if (declaration.visibility != DescriptorVisibilities.PUBLIC) {
                error("EzHook annotation can only be used on public functions")
            }
            val anno = declaration.getAnnotation(FqName(EZ_HOOK_ANNOTATION))
            val targetFunctionFqName = (anno.getValueArgument(0) as IrConst<String>).value
            val inline = (anno.getValueArgument(1) as? IrConst<Boolean>)?.value ?: false
            println("EzHook: visit @EzHook Function ${declaration.name} with target function $targetFunctionFqName")
            collectInfo.add(EzHookInfo(declaration, targetFunctionFqName, inline))
        }
    }

}