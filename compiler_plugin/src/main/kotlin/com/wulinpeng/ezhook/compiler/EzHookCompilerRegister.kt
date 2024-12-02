package com.wulinpeng.ezhook.compiler

import com.google.auto.service.AutoService
import com.wulinpeng.ezhook.compiler.hook.IrLoweringHookExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(CompilerPluginRegistrar::class)
@OptIn(ExperimentalCompilerApi::class)
class EzHookCompilerRegister: CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        IrLoweringHookExtension.registerExtension(EzHookExtension())
    }
}