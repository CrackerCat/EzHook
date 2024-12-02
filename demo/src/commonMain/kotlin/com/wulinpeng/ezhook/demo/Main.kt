package com.wulinpeng.ezhook.demo

import com.wulinpeng.ezhook.demov2.NormalTest
import com.wulinpeng.ezhook.demov2.getStr
import com.wulinpeng.ezhook.demov2.topLevelFunctionTest
import kotlin.time.Duration
import kotlin.time.DurationUnit

fun main() {
    testCase("NormalCase", NormalTest("origin name").test("origin name"))
    testCase("TopLevelCase", topLevelFunctionTest("origin name"))
    testCase("ExtendFunctionCase", 10.getStr())
    testCase("DurationHook", "${Duration.ZERO.toInt(DurationUnit.SECONDS)}")
}

fun testCase(caseName: String, result: String) {
    println("Test case $caseName: $result")
}