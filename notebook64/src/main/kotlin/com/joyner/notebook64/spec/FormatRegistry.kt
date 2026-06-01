package com.joyner.notebook64.spec

internal object FormatRegistry {

    private val specs: Map<String, FormatSpec> =
        FormatDefinitions.all.associateBy { it.tipo }

    fun getSpec(tipo: String): FormatSpec? = specs[tipo]

    /**
     * Auto-detects the format by checking, for each registered spec with matching length,
     * whether the tipo indicator field at [FormatSpec.tipoFieldStart] matches [FormatSpec.tipo].
     * Returns null if no match found.
     */
    fun detect(input: String): FormatSpec? =
        specs.values
            .filter { it.expectedLength == input.length }
            .firstOrNull { spec ->
                val end = spec.tipoFieldStart - 1 + 3
                if (end > input.length) return@firstOrNull false
                input.substring(spec.tipoFieldStart - 1, end) == spec.tipo
            }

    fun allSpecs(): Collection<FormatSpec> = specs.values
}
