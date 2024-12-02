package com.wulinpeng.ezhook.demo.hook

import com.wulinpeng.ezhook.runtime.EzHook
import com.wulinpeng.ezhook.runtime.callOrigin
import kotlin.time.DurationUnit

// TODO support constructor
//@EzHook("com.wulinpeng.ezhook.demov2.NormalTest.<init>")
//fun hookContructor(name: String) {
//    var name = "newName"
//    callOrigin<Unit>()
//}

@EzHook("com.wulinpeng.ezhook.demov2.NormalTest.test", true)
fun newTest(name: String): String {
    var name = "newName"
    return "before hook: ${callOrigin<String>()}, after hook: $name-2"
}

@EzHook("com.wulinpeng.ezhook.demov2.topLevelFunctionTest", true)
fun topLevelFunctionTest(name: String): String {
    var name = "newName"
    return "before hook: ${callOrigin<String>()}, after hook: $name-2"
}

@EzHook("kotlin.time.Duration.toInt", true)
fun toInt(unit: DurationUnit): Int {
    val unit = DurationUnit.HOURS
    println("Hook to int")
    return 10086
}


// TODO support extension function
//@EzHook("com.wulinpeng.ezhook.demov2.getStr")
//fun getStr(): String {
//    return "${callOrigin<String>()}-new"
//}