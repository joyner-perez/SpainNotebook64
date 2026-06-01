package com.joyner.notebook64.model

data class ParsedField(
    val name: String,
    val value: String,
    val startPos: Int,
    val length: Int,
    val description: String = ""
)
