package com.wulinpeng.ezhook.demov2

class NormalTest(name: String) {

    init {
        println("NormalTest init $name")
    }

    fun test(name: String): String {
        return "$name-1"
    }
}

fun topLevelFunctionTest(name: String): String {
    return "$name-1"
}

fun Int.getStr(): String {
    return "Int value: $this"
}