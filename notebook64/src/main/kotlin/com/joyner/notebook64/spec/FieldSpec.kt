package com.joyner.notebook64.spec

/**
 * Defines a single field within a Cuaderno 64 barcode format.
 * @param name field identifier used in ParsedField
 * @param startPos 1-indexed start position within the cleaned payload string
 * @param length number of characters
 * @param description human-readable description
 */
data class FieldSpec(
    val name: String,
    val startPos: Int,
    val length: Int,
    val description: String = "",
)
