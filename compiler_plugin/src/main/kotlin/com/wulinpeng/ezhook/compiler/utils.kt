package com.wulinpeng.ezhook.compiler

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.load.java.setMethodName
import org.jetbrains.kotlin.name.Name

/**
 * author: wulinpeng
 * create: 2024/11/22 00:09
 * desc:
 */
fun IrConstructorCall.defaultParamValue(index: Int): IrExpression? {
    return symbol.owner.valueParameters[1].defaultValue?.expression
}

fun IrFunction.isClassMember(): Boolean {
    return parent is IrClass
}

fun IrFunction.copyFunctionToParent(newName: String, newParent: IrDeclarationParent = parent): IrFunction {

    return deepCopyWithSymbols(newParent).apply {
        name = Name.identifier(newName)
        setDeclarationsParent(newParent)
        (newParent as IrDeclarationContainer).addChild(this)
    }
}