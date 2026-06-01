package com.joyner.notebook64

import com.joyner.notebook64.model.FormatInfo
import com.joyner.notebook64.model.Notebook64Result
import com.joyner.notebook64.parser.Notebook64Parser
import com.joyner.notebook64.spec.FormatRegistry

/**
 * Parses a CECA Cuaderno 64 barcode payload string.
 *
 * @param input the barcode string, with or without the GS1-128 AI(90) prefix:
 *              "(90)0051111111330053424083405001000006764" and
 *              "0051111111330053424083405001000006764" are both accepted.
 * @param tipo  optional explicit format type (e.g. "534", "550"). When null the tipo
 *              is auto-detected from the embedded tipo indicator field.
 * @return [Notebook64Result] with the detected tipo and all extracted fields.
 * @throws IllegalArgumentException if the tipo is unknown or the string length does not
 *         match the expected length for the given (or detected) tipo.
 */
fun parse(
    input: String,
    tipo: String? = null,
): Notebook64Result = Notebook64Parser.parse(input, tipo)

fun availableFormats(): List<FormatInfo> =
    FormatRegistry
        .allSpecs()
        .map { FormatInfo(it.tipo, it.description) }
        .sortedBy { it.tipo }
