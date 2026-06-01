package com.joyner.notebook64.model

data class Notebook64Result(val tipo: String, val rawInput: String, val fields: List<ParsedField>) {
    fun field(name: String): ParsedField? = fields.firstOrNull { it.name == name }

    fun value(name: String): String? = field(name)?.value
}
