package com.wulinpeng.ezhook.runtime

/**
 * author: wulinpeng
 * create: 2024/11/21 22:55
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class EzHook(val targetFunction: String, val inline: Boolean = false)