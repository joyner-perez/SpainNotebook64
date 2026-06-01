package com.joyner.notebook64.parser

import com.joyner.notebook64.model.Notebook64Result
import com.joyner.notebook64.model.ParsedField
import com.joyner.notebook64.spec.FormatRegistry
import com.joyner.notebook64.spec.FormatSpec

internal object Notebook64Parser {

    fun parse(input: String, tipo: String? = null): Notebook64Result {
        val (cleaned, ai) = detectAndStrip(input)
        val spec = when {
            tipo != null -> FormatRegistry.getSpec(tipo)
                ?: throw IllegalArgumentException("Tipo desconocido: '$tipo'")
            else -> FormatRegistry.detect(cleaned)
                ?: throw IllegalArgumentException(
                    "No se puede detectar el tipo. Longitud=${cleaned.length}, " +
                    "primeros 3 chars='${cleaned.take(3)}'"
                )
        }
        if (cleaned.length != spec.expectedLength) {
            throw IllegalArgumentException(
                "Tipo '${spec.tipo}' espera longitud ${spec.expectedLength} " +
                "pero recibió ${cleaned.length}"
            )
        }
        return parseWithSpec(cleaned, ai, spec)
    }

    /**
     * Strips known GS1-128 Application Identifier prefixes and returns (payload, AI):
     *   AI "90" — used by most Cuaderno 64 formats
     *   AI "21" — used by tipo 011 (AEAT autoliquidaciones, analogía número de serie)
     * Parenthesised form "(90)"/"(21)" is stripped unconditionally.
     * Bare form "90"/"21" is only stripped when the remainder length matches a registered format.
     * Returns ("", payload) when no AI prefix is detected.
     */
    internal fun detectAndStrip(input: String): Pair<String, String> {
        for (ai in listOf("(90)", "(21)")) {
            if (input.startsWith(ai)) {
                return input.removePrefix(ai) to ai.removeSurrounding("(", ")")
            }
        }
        for (ai in listOf("90", "21")) {
            if (input.startsWith(ai)) {
                val candidate = input.removePrefix(ai)
                if (FormatRegistry.allSpecs().any { it.expectedLength == candidate.length }) {
                    return candidate to ai
                }
            }
        }
        return input to ""
    }

    internal fun stripGS1Prefix(input: String): String = detectAndStrip(input).first

    private fun parseWithSpec(input: String, ai: String, spec: FormatSpec): Notebook64Result {
        val offset = ai.length  // positions in full barcode = payload position + offset
        val fields = buildList {
            if (offset > 0) {
                add(ParsedField(
                    name = "applicationIdentifier",
                    value = ai,
                    startPos = 1,
                    length = offset,
                    description = "Identificador de Aplicación GS1-128"
                ))
            }
            addAll(spec.fields.map { fieldSpec ->
                val start = fieldSpec.startPos - 1
                ParsedField(
                    name = fieldSpec.name,
                    value = input.substring(start, start + fieldSpec.length),
                    startPos = fieldSpec.startPos + offset,
                    length = fieldSpec.length,
                    description = fieldSpec.description
                )
            })
        }
        return Notebook64Result(
            tipo = spec.tipo,
            rawInput = input,
            fields = fields
        )
    }
}
