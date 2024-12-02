package com.wulinpeng.ezhook.compiler.hook

import com.wulinpeng.ezhook.compiler.hook.JsIrLoweringHook.hookLoweringPhase
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.LoggingContext
import org.jetbrains.kotlin.backend.common.phaser.IrValidationBeforeLoweringPhase
import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import java.io.File

/**
 * author: wulinpeng
 * create: 2024/11/25 22:13
 * desc: Hook ir lowering phase to add custom lowering logic
 */
object JsIrLoweringHook {
    private const val NATIVE_LOWERING_PHASES_CLASS = "org.jetbrains.kotlin.ir.backend.js.JsLoweringPhasesKt"

    fun runHook(traverser: (CommonBackendContext, IrModuleFragment) -> Unit,
                transformer: (CommonBackendContext, IrModuleFragment) -> Unit) {
        runCatching {
            val allModules = mutableListOf<IrModuleFragment>()
            hookValidateIrBeforeLowering(allModules, traverser)
            hookJsCodeOutliningPhase {
                allModules.forEach { module ->
                    transformer(it, module)
                }
            }
        }
    }

    private fun hookValidateIrBeforeLowering(allModules: MutableList<IrModuleFragment>, transformer: (CommonBackendContext, IrModuleFragment) -> Unit) {
        hookLoweringPhase("validateIrBeforeLowering") { context, irModuleFragment ->
            allModules.add(irModuleFragment)
            transformer(context, irModuleFragment)
        }
    }

    private fun hookJsCodeOutliningPhase(onStart: (context: CommonBackendContext) -> Unit) {
        var hasStart = false
        hookLoweringPhase("jsCodeOutliningPhase") { context, irModuleFragment ->
            if (!hasStart) {
                onStart(context)
                hasStart = true
            }
        }
    }

    private fun hookLoweringPhase(phaseName: String, transformer: (CommonBackendContext, IrModuleFragment) -> Unit) {
        val clazz = Class.forName(NATIVE_LOWERING_PHASES_CLASS)
        val lower = clazz.declaredFields.firstOrNull { it.name == phaseName }?.apply {
            isAccessible = true
        }!!.get(null)
        val opField = lower.javaClass.declaredFields.firstOrNull { it.name == "\$op" }?.apply {
            isAccessible = true
        }!!
        var originOp = opField.get(lower)
        if (!originOp.javaClass.name.startsWith("org.jetbrains.kotlin.backend.common.phaser.PhaseBuildersKt")) {
            // already hooked
            return
        }
        opField.set(lower, {context: CommonBackendContext, irModuleFragment: IrModuleFragment ->
            transformer(context, irModuleFragment)
            // call origin
            (originOp as (Any, IrModuleFragment) -> IrModuleFragment).invoke(context, irModuleFragment)
        })
    }
}