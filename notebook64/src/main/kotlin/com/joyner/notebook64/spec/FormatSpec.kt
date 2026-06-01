package com.joyner.notebook64.spec

/**
 * Complete specification for a Cuaderno 64 barcode format type.
 * @param tipo the 3-digit type code embedded in the barcode (e.g. "501", "550")
 * @param description human-readable format name
 * @param expectedLength expected total payload length after stripping the GS1-128 AI prefix
 * @param tipoFieldStart 1-indexed position where the tipo indicator appears in the payload
 * @param fields ordered list of field definitions
 */
data class FormatSpec(
    val tipo: String,
    val description: String,
    val expectedLength: Int,
    val tipoFieldStart: Int,
    val fields: List<FieldSpec>
) {
    init {
        require(tipo.length == TIPO_LENGTH) {
            "tipo must be exactly $TIPO_LENGTH characters, got: '$tipo'"
        }
        require(expectedLength > 0) { "expectedLength must be positive" }
        require(tipoFieldStart >= 1) { "tipoFieldStart must be >= 1" }
    }

    companion object {
        /** Length of the tipo indicator code embedded in every payload. */
        const val TIPO_LENGTH = 3
    }
}
