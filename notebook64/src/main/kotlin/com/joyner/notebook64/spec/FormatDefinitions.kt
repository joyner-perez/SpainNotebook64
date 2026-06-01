package com.joyner.notebook64.spec

/**
 * Format specifications for CECA Cuaderno 64 (June 2016).
 *
 * Field positions are 1-indexed and based on:
 *   - The barcode example from the PDF: (90)0051111111330053424083405001000006764
 *     → payload "0051111111330053424083405001000006764" (37 chars), tipo "534" at positions 15-17
 *   - CECA/AEB Norma 64 standard knowledge
 *
 * IMPORTANT: positions should be verified against real barcode samples for each tipo.
 *
 * Groupings:
 *   - APPENDIX A: tipo 501 CONFIRMADO (32 chars, tributos admin. local). Tipos 502-585 PENDIENTES.
 *   - APPENDIX B (tributarios en ventanilla): tipos 518-565, tipo field at pos 15-17, 37 chars
 *   - APPENDIX B/C (tributarios con período): tipos 539/550/551/559, tipo at pos 15-17, 39 chars
 *   - APPENDIX C (declaraciones AEAT): tipos 512/530/532/558, tipo at pos 1-3, 36 chars
 *   - APPENDIX D: tipo 581, tipo at pos 1-3, 37 chars
 */
object FormatDefinitions {
    // ─── Tipo 504 — Centros Docentes ───────────────────────────────────────────
    // Tipo 504 spec confirmed. Full barcode 32 chars (AI "90" + payload 30 chars).
    // DC algorithm "Igual a Formato 501".
    val TIPO_504 =
        FormatSpec(
            tipo = "504",
            description = "Centros docentes - matrículas y tasas",
            expectedLength = 30,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("emisora", 4, 5, "Emisora"),
                    FieldSpec("tipoMatricula", 9, 4, "Tipo de matrícula"),
                    FieldSpec("numeroNotificacion", 13, 7, "Número de notificación"),
                    FieldSpec("digitosControlClave", 20, 2, "Dígitos de control de la clave"),
                    FieldSpec("importe", 22, 7, "Importe en céntimos"),
                    FieldSpec("digitosControlImporte", 29, 2, "Dígitos de control del importe"),
                ),
        )

    // ─── Appendix A: Clave de Cobro Municipal (501, 503 y similares) ───────────
    // Full barcode = 34 chars (AI "90" pos 1-2 + payload 32 chars).
    // 501 = Tributos Admin. Local | 503 = Multas Ayuntamiento
    // Same structure, same DC algorithm (Formato 001). DC "Igual a Formato 501".

    private fun claveCobroMunicipal(
        tipo: String,
        description: String,
    ) = FormatSpec(
        tipo = tipo,
        description = description,
        expectedLength = 32,
        tipoFieldStart = 1,
        fields =
            listOf(
                FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                FieldSpec("provincia", 4, 2, "Código provincia según Hacienda"),
                FieldSpec("municipio", 6, 3, "Código municipio"),
                FieldSpec("ejercicio", 9, 1, "Última cifra del año"),
                FieldSpec("remesa", 10, 1, "Número de remesa dentro del ejercicio"),
                FieldSpec("tributo", 11, 2, "Código tributo"),
                FieldSpec("notificacion", 13, 7, "Número de notificación"),
                FieldSpec("digitosControlClave", 20, 2, "Dígitos de control de la clave de cobro"),
                FieldSpec("importe", 22, 9, "Importe en céntimos"),
                FieldSpec("digitosControlImporte", 31, 2, "Dígitos de control del importe"),
            ),
    )

    val TIPO_501 = claveCobroMunicipal("501", "Tributos Administración Local")
    val TIPO_503 = claveCobroMunicipal("503", "Multas Ayuntamiento")

    // Tipos 502-585 (Apéndice A): posiciones pendientes de confirmar con el documento.
    private fun domiciliacionPendiente(
        tipo: String,
        description: String,
    ) = FormatSpec(
        tipo = tipo,
        description = description,
        expectedLength = 37,
        tipoFieldStart = 1,
        fields =
            listOf(
                FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                FieldSpec("codigoPresentador", 4, 10, "Código del presentador/emisor [PENDIENTE CONFIRMAR]"),
                FieldSpec("referenciaRecibo", 14, 12, "Referencia del recibo [PENDIENTE CONFIRMAR]"),
                FieldSpec("importe", 26, 10, "Importe [PENDIENTE CONFIRMAR]"),
                FieldSpec("indicadorOpcional", 36, 2, "Indicador opcional [PENDIENTE CONFIRMAR]"),
            ),
    )

    // Tipo 502 spec confirmed. Full barcode 38 chars (AI "90" + payload 36 chars).
    // Sub-fields of emisora(6), referencia(12) and identificacion(7) are exposed flat.
    val TIPO_502 =
        FormatSpec(
            tipo = "502",
            description = "Tributos y otros ingresos municipales - formato corto modalidad 1",
            expectedLength = 36,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("provincia", 4, 2, "Código provincia"),
                    FieldSpec("municipio", 6, 3, "Código municipio"),
                    FieldSpec("digitoControlEmisora", 9, 1, "Dígito de control de emisora"),
                    FieldSpec("identificacionDocumento", 10, 10, "Identificación del documento"),
                    FieldSpec("digitosControlReferencia", 20, 2, "Dígitos de control de referencia"),
                    FieldSpec("tributo", 22, 3, "Código tributo (Anexo 6 Cuadernos 34/60)"),
                    FieldSpec("ejercicio", 25, 2, "Ejercicio fiscal"),
                    FieldSpec("remesa", 27, 2, "Número de remesa"),
                    FieldSpec("importe", 29, 8, "Importe en céntimos"),
                ),
        )

    // Tipo 508 spec confirmed. Full barcode 48 chars (AI "90" + payload 46 chars).
    // Formato largo — adds entidadTesorera and fechaLimitePago vs 502.
    val TIPO_508 =
        FormatSpec(
            tipo = "508",
            description = "Tributos y otros ingresos municipales - formato largo",
            expectedLength = 46,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("entidadTesorera", 4, 4, "Entidad tesorera"),
                    FieldSpec("fechaLimitePago", 8, 6, "Fecha límite de pago"),
                    FieldSpec("provincia", 14, 2, "Código provincia"),
                    FieldSpec("municipio", 16, 3, "Código municipio"),
                    FieldSpec("digitoControlEmisora", 19, 1, "Dígito de control de emisora"),
                    FieldSpec("identificacionDocumento", 20, 10, "Identificación del documento"),
                    FieldSpec("digitosControlReferencia", 30, 2, "Dígitos de control de referencia"),
                    FieldSpec("tributo", 32, 3, "Código tributo"),
                    FieldSpec("ejercicio", 35, 2, "Ejercicio fiscal"),
                    FieldSpec("remesa", 37, 2, "Número de remesa"),
                    FieldSpec("importe", 39, 8, "Importe en céntimos"),
                ),
        )

    // Tipo 521 spec confirmed. Full barcode 42 chars (AI "90" + payload 40 chars).
    val TIPO_521 =
        FormatSpec(
            tipo = "521",
            description = "Tributos y otros ingresos municipales - modalidad 2 sin recargo",
            expectedLength = 40,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("provincia", 4, 2, "Código provincia"),
                    FieldSpec("municipio", 6, 3, "Código municipio"),
                    FieldSpec("digitoControlEmisora", 9, 1, "Dígito de control de emisora"),
                    FieldSpec("identificacionDocumento", 10, 10, "Identificación del documento"),
                    FieldSpec("digitosControlReferencia", 20, 2, "Dígitos de control de referencia"),
                    FieldSpec("discriminantePeriodo", 22, 1, "Discriminante del período (fijo '1')"),
                    FieldSpec("tributo", 23, 3, "Código tributo (Anexo 6 Cuaderno 60)"),
                    FieldSpec("ejercicioDevengo", 26, 2, "Ejercicio de devengo"),
                    FieldSpec("anioFechaLimite", 28, 1, "Último dígito del año de fecha límite"),
                    FieldSpec("fechaJulianaLimite", 29, 3, "Fecha juliana límite de pago"),
                    FieldSpec("importe", 32, 8, "Importe en céntimos"),
                    FieldSpec("digitoParidad", 40, 1, "Dígito de paridad (= '0')"),
                ),
        )

    // Tipo 522 spec confirmed. Full barcode 54 chars (AI "90" + payload 52 chars).
    // Note: discriminantePeriodo=5, tributo and ejercicioDevengo for con-recargo block
    //       are implicit (not encoded in barcode — taken from sin-recargo block).
    val TIPO_522 =
        FormatSpec(
            tipo = "522",
            description = "Tributos y otros ingresos municipales - modalidad 2 con recargo",
            expectedLength = 52,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("provincia", 4, 2, "Código provincia"),
                    FieldSpec("municipio", 6, 3, "Código municipio"),
                    FieldSpec("digitoControlEmisora", 9, 1, "Dígito de control de emisora"),
                    FieldSpec("identificacionDocumento", 10, 10, "Identificación del documento"),
                    FieldSpec("digitosControlSinRecargo", 20, 2, "DC para importes sin recargo"),
                    FieldSpec("digitosControlConRecargo", 22, 2, "DC para importes con recargo"),
                    FieldSpec("tributo", 24, 3, "Código tributo (Anexo Común II Cuaderno 60)"),
                    FieldSpec("ejercicioDevengo", 27, 2, "Ejercicio de devengo"),
                    FieldSpec("anioFechaLimiteSinRecargo", 29, 1, "Último dígito año fecha límite sin recargo"),
                    FieldSpec("fechaJulianaLimiteSinRecargo", 30, 3, "Fecha juliana límite sin recargo"),
                    FieldSpec("importeSinRecargo", 33, 8, "Importe sin recargo en céntimos"),
                    FieldSpec("anioFechaLimiteConRecargo", 41, 1, "Último dígito año fecha límite con recargo"),
                    FieldSpec("fechaJulianaLimiteConRecargo", 42, 3, "Fecha juliana límite con recargo"),
                    FieldSpec("importeConRecargo", 45, 8, "Importe con recargo en céntimos"),
                ),
        )

    // Tipo 523 spec confirmed. Full barcode 24 chars (AI "90" + payload 22 chars).
    val TIPO_523 =
        FormatSpec(
            tipo = "523",
            description = "Documentos de autoliquidación municipales - modalidad 3",
            expectedLength = 22,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("provincia", 4, 2, "Código provincia"),
                    FieldSpec("municipio", 6, 3, "Código municipio"),
                    FieldSpec("digitoControlEmisora", 9, 1, "Dígito de control de emisora"),
                    FieldSpec("codigoTributoModelo", 10, 3, "Código de tributo o modelo"),
                    FieldSpec("indicadorDatosCapturar", 13, 1, "Indicador de datos a capturar"),
                    FieldSpec("numeroSecuencial", 14, 8, "Número secuencial"),
                    FieldSpec("digitoControlJustificante", 22, 1, "Dígito de control del justificante"),
                ),
        )

    // Tipo 579 spec confirmed. Full barcode 36 chars (AI "90" + payload 34 chars).
    // Convenio Ayuntamiento de Madrid.
    val TIPO_579 =
        FormatSpec(
            tipo = "579",
            description = "Recibos periódicos Ayuntamiento de Madrid",
            expectedLength = 34,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("provincia", 4, 2, "Código provincia"),
                    FieldSpec("discriminante", 6, 3, "Discriminante del emisor"),
                    FieldSpec("digitoControlEmisora", 9, 1, "Dígito de control de emisora"),
                    FieldSpec("tributo", 10, 3, "Código tributo"),
                    FieldSpec("referencia", 13, 10, "Referencia del recibo"),
                    FieldSpec("año", 23, 2, "Año"),
                    FieldSpec("digitosControlRecibo", 25, 2, "Dígitos de control del recibo"),
                    FieldSpec("importe", 27, 8, "Importe en céntimos"),
                ),
        )

    // Tipo 580 spec confirmed. Full barcode 46 chars (AI "90" + payload 44 chars).
    // Convenio Ayuntamiento de Madrid — liquidación ejecutiva.
    // control field = DDMMAAAA (8) + 2 dígitos = 10 chars total.
    val TIPO_580 =
        FormatSpec(
            tipo = "580",
            description = "Liquidación ejecutiva Ayuntamiento de Madrid",
            expectedLength = 44,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("provincia", 4, 2, "Código provincia"),
                    FieldSpec("discriminante", 6, 3, "Discriminante del emisor"),
                    FieldSpec("digitoControlEmisora", 9, 1, "Dígito de control de emisora"),
                    FieldSpec("codigoSujeto", 10, 13, "Código/identificación del sujeto"),
                    FieldSpec("referencia", 23, 11, "Referencia abonaré"),
                    FieldSpec("control", 34, 10, "Control: DDMMAAAA + 2 dígitos de control"),
                    FieldSpec("digitoParidad", 44, 1, "Dígito de paridad (= '0')"),
                ),
        )

    // Tipo 582 spec confirmed. Full barcode 36 chars (AI "90" + payload 34 chars).
    // Convenio Ayuntamiento de Madrid — autoliquidación.
    val TIPO_582 =
        FormatSpec(
            tipo = "582",
            description = "Autoliquidación Ayuntamiento de Madrid",
            expectedLength = 34,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("provincia", 4, 2, "Código provincia"),
                    FieldSpec("discriminante", 6, 3, "Discriminante del emisor"),
                    FieldSpec("digitoControlEmisora", 9, 1, "Dígito de control de emisora"),
                    FieldSpec("identificador", 10, 17, "Identificador del recibo"),
                    FieldSpec("discriminanteGestor", 27, 5, "Discriminante del gestor"),
                    FieldSpec("digitosControlGestor", 32, 2, "Dígitos de control del gestor"),
                    FieldSpec("digitoParidad", 34, 1, "Dígito de paridad (= '0')"),
                ),
        )

    // Tipo 583 spec confirmed. Full barcode 46 chars (AI "90" + payload 44 chars).
    // Convenio Ayuntamiento de Madrid — liquidación.
    val TIPO_583 =
        FormatSpec(
            tipo = "583",
            description = "Liquidación Ayuntamiento de Madrid",
            expectedLength = 44,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("provincia", 4, 2, "Código provincia"),
                    FieldSpec("discriminante", 6, 3, "Discriminante del emisor"),
                    FieldSpec("digitoControlEmisora", 9, 1, "Dígito de control de emisora"),
                    FieldSpec("identificador", 10, 17, "Identificador del recibo"),
                    FieldSpec("control", 27, 10, "Control: DDMMAAAA + 2 dígitos de control"),
                    FieldSpec("importe", 37, 8, "Importe en céntimos"),
                ),
        )

    // Tipo 584 spec confirmed. Full barcode 49 chars (AI "90" + payload 47 chars).
    // Convenio Ayuntamiento de Madrid — multas. Uses EAN 128-B for matricula (alphanumeric).
    val TIPO_584 =
        FormatSpec(
            tipo = "584",
            description = "Multas Ayuntamiento de Madrid",
            expectedLength = 47,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("provincia", 4, 2, "Código provincia"),
                    FieldSpec("discriminante", 6, 3, "Discriminante del emisor"),
                    FieldSpec("digitoControlEmisora", 9, 1, "Dígito de control de emisora"),
                    FieldSpec("numeroBoletín", 10, 9, "Número de boletín"),
                    FieldSpec("control", 19, 10, "Control: DDMMAAAA + 2 dígitos de control"),
                    FieldSpec("importe", 29, 8, "Importe en céntimos"),
                    FieldSpec("matricula", 37, 11, "Matrícula del vehículo (alfanumérico)"),
                ),
        )

    // Tipo 585 spec confirmed. Full barcode 44 chars (AI "90" + payload 42 chars).
    // Convenio Ayuntamiento de Madrid — otras multas.
    val TIPO_585 =
        FormatSpec(
            tipo = "585",
            description = "Otras multas Ayuntamiento de Madrid",
            expectedLength = 42,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("provincia", 4, 2, "Código provincia"),
                    FieldSpec("discriminante", 6, 3, "Discriminante del emisor"),
                    FieldSpec("digitoControlEmisora", 9, 1, "Dígito de control de emisora"),
                    FieldSpec("identificador", 10, 14, "Identificador del expediente"),
                    FieldSpec("control", 24, 10, "Control: DDMMAAAA + 2 dígitos de control"),
                    FieldSpec("importe", 34, 8, "Importe en céntimos"),
                    FieldSpec("digitoParidad", 42, 1, "Dígito de paridad (= '0')"),
                ),
        )

    // ─── Appendix B: Tributarios en ventanilla bancaria ────────────────────────
    // Structure (37 chars): nif(9) + modelo(3) + ejercicio(2) + tipo(3) + periodo(2) + justificante(10) + importe(8)
    // Barcode example (tipo 534): 0051111111330053424083405001000006764

    private fun tributarioVentanilla(
        tipo: String,
        description: String,
    ) = FormatSpec(
        tipo = tipo,
        description = description,
        expectedLength = 37,
        tipoFieldStart = 15,
        fields =
            listOf(
                FieldSpec("nif", 1, 9, "NIF del contribuyente"),
                FieldSpec("modelo", 10, 3, "Número de modelo tributario"),
                FieldSpec("ejercicio", 13, 2, "Ejercicio fiscal (YY)"),
                FieldSpec("tipo", 15, 3, "Indicador de tipo"),
                FieldSpec("periodo", 18, 2, "Período de la declaración"),
                FieldSpec("numeroJustificante", 20, 10, "Número de justificante/documento"),
                FieldSpec("importe", 30, 8, "Importe (2 decimales implícitos)"),
            ),
    )

    // Tipo 518 spec confirmed. Full barcode 57 chars (AI "90" + payload 55 chars).
    // Cuaderno 65 AEB/CECA — Liquidaciones Comunidades Autónomas.
    // NIF and opcional fields are alphanumeric (EAN 128-B).
    val TIPO_518 =
        FormatSpec(
            tipo = "518",
            description = "Liquidaciones Comunidades Autónomas",
            expectedLength = 55,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("codigoOrganismo", 4, 5, "Código de organismo/emisor (con DC módulo 11)"),
                    FieldSpec("codigoTerritorial", 9, 6, "Código territorial"),
                    FieldSpec("numeroJustificante", 15, 13, "Número de justificante"),
                    FieldSpec("importe", 28, 15, "Importe en céntimos"),
                    FieldSpec("nif", 43, 9, "NIF del contribuyente (alfanumérico)"),
                    FieldSpec("opcional", 52, 4, "Anagrama o espacios (alfanumérico)"),
                ),
        )

    // Tipo 526 spec confirmed. Full barcode 46 chars (AI "90" + payload 44 chars).
    // Cuaderno propio CARM — Liquidaciones administrativas C.A. Región de Murcia.
    val TIPO_526 =
        FormatSpec(
            tipo = "526",
            description = "Liquidaciones administrativas C.A. Región de Murcia",
            expectedLength = 44,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("unidadGestora", 4, 6, "Unidad gestora"),
                    FieldSpec("entidadEmisora", 10, 3, "Entidad emisora"),
                    FieldSpec("modeloConceptoPresupuestario", 13, 6, "Modelo/concepto presupuestario"),
                    FieldSpec("numeroSecuencialImpreso", 19, 5, "Número secuencial impreso"),
                    FieldSpec("ejercicioImpresion", 24, 4, "Ejercicio de impresión o expedición"),
                    FieldSpec("claseEstado", 28, 3, "Clase y estado"),
                    FieldSpec("digitoControlN28", 31, 1, "Dígito de control del N-28"),
                    FieldSpec("digitoParidad", 32, 1, "Dígito de paridad (= '0')"),
                    FieldSpec("importe", 33, 12, "Importe en céntimos"),
                ),
        )

    // Tipo 527 spec confirmed. Full barcode 34 chars (AI "90" + payload 32 chars).
    // Cuaderno propio CARM — Autoliquidaciones C.A. Región de Murcia.
    // Same N-28 structure as 526 but without importe.
    val TIPO_527 =
        FormatSpec(
            tipo = "527",
            description = "Autoliquidaciones administrativas C.A. Región de Murcia",
            expectedLength = 32,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("unidadGestora", 4, 6, "Unidad gestora"),
                    FieldSpec("entidadEmisora", 10, 3, "Entidad emisora"),
                    FieldSpec("modeloConceptoPresupuestario", 13, 6, "Modelo/concepto presupuestario"),
                    FieldSpec("numeroSecuencialImpreso", 19, 5, "Número secuencial impreso"),
                    FieldSpec("ejercicioImpresion", 24, 4, "Ejercicio de impresión o expedición"),
                    FieldSpec("claseEstado", 28, 3, "Clase y estado"),
                    FieldSpec("digitoControlN28", 31, 1, "Dígito de control del N-28"),
                    FieldSpec("digitoParidad", 32, 1, "Dígito de paridad (= '0')"),
                ),
        )

    // Tipo 534 spec confirmed. Full barcode 49 chars (AI "90" + payload 47 chars).
    // Cuaderno 65 AEB/CECA — Autoliquidaciones Junta de Andalucía.
    // concepto/NIF/anagrama/codigoTerritorial are alphanumeric (EAN 128-B).
    val TIPO_534 =
        FormatSpec(
            tipo = "534",
            description = "Autoliquidaciones Junta de Andalucía",
            expectedLength = 47,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("numeroJustificante", 4, 13, "Número de justificante"),
                    FieldSpec("concepto", 17, 4, "Concepto del modelo (alfanumérico; blancos si no aplica)"),
                    FieldSpec("importe", 21, 8, "Importe en céntimos"),
                    FieldSpec("nif", 29, 9, "NIF del contribuyente (alfanumérico)"),
                    FieldSpec("anagrama", 38, 4, "Anagrama (alfanumérico; blancos para jurídicas)"),
                    FieldSpec("codigoTerritorial", 42, 6, "Código territorial (alfanumérico)"),
                ),
        )

    // Tipo 535 spec confirmed. Full barcode 41 chars (AI "90" + payload 39 chars).
    // Cuaderno 65 AEB/CECA — Liquidaciones Junta de Andalucía. NIF/codigoTerritorial alphanumeric.
    val TIPO_535 =
        FormatSpec(
            tipo = "535",
            description = "Liquidaciones Junta de Andalucía",
            expectedLength = 39,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("numeroJustificante", 4, 13, "Número de justificante"),
                    FieldSpec("importe", 17, 8, "Importe en céntimos"),
                    FieldSpec("nif", 25, 9, "NIF del contribuyente (alfanumérico)"),
                    FieldSpec("codigoTerritorial", 34, 6, "Código territorial (alfanumérico)"),
                ),
        )

    // Tipo 564 spec confirmed. Full barcode 45 chars (AI "90" + payload 43 chars).
    // Cuaderno 65 AEB/CECA — Autoliquidaciones Comunidad Valenciana. NIF alphanumeric.
    val TIPO_564 =
        FormatSpec(
            tipo = "564",
            description = "Autoliquidaciones Comunidad Valenciana",
            expectedLength = 43,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("numeroJustificante", 4, 13, "Número de justificante"),
                    FieldSpec("codigoTerritorial", 17, 6, "Código territorial"),
                    FieldSpec("concepto", 23, 4, "Concepto del modelo"),
                    FieldSpec("importe", 27, 8, "Importe en céntimos"),
                    FieldSpec("nif", 35, 9, "NIF del contribuyente (alfanumérico)"),
                ),
        )
    val TIPO_565 = tributarioVentanilla("565", "Pago tributario en ventanilla - variante 565")

    // ─── Appendix B/C: Tributarios con período alfanumérico ────────────────────
    // Structure (39 chars): nif(9) + modelo(3) + ejercicio(2) + tipo(3) + periodo(2) + justificante(10) + importe(10)
    // Período values: "00" aperiódico, "A0" anual, "1T"-"4T" trimestral, "01"-"12" mensual

    private fun tributarioPeriodo(
        tipo: String,
        description: String,
    ) = FormatSpec(
        tipo = tipo,
        description = description,
        expectedLength = 39,
        tipoFieldStart = 15,
        fields =
            listOf(
                FieldSpec("nif", 1, 9, "NIF del contribuyente"),
                FieldSpec("modelo", 10, 3, "Número de modelo tributario"),
                FieldSpec("ejercicio", 13, 2, "Ejercicio fiscal (YY)"),
                FieldSpec("tipo", 15, 3, "Indicador de tipo"),
                FieldSpec("periodo", 18, 2, "Período (00/A0/1T-4T/01-12)"),
                FieldSpec("numeroJustificante", 20, 10, "Número de justificante"),
                FieldSpec("importe", 30, 10, "Importe (2 decimales implícitos)"),
            ),
    )

    // Tipo 539 spec confirmed. Full barcode 55 chars (AI "90" + payload 53 chars).
    // Cuaderno 65 AEB/CECA/UNACC — Autoliquidaciones CCAA con programas de ayuda.
    // codigoOrganismo: 61001=Andalucía, 62005=Aragón, ... 79005=Melilla (see PDF for full list).
    // periodo alphanumeric: "0A" anual, "1T"-"4T" trimestral, "01"-"12" mensual.
    val TIPO_539 =
        FormatSpec(
            tipo = "539",
            description = "Autoliquidaciones CCAA con programas de ayuda",
            expectedLength = 53,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("codigoOrganismo", 4, 5, "Código organismo (61001=Andalucía … 79005=Melilla)"),
                    FieldSpec("numeroJustificante", 9, 13, "Número de justificante"),
                    FieldSpec("importe", 22, 12, "Importe total de la declaración"),
                    FieldSpec("modelo", 34, 3, "Número de modelo"),
                    FieldSpec("ejercicio", 37, 2, "Ejercicio (2 últimas cifras del año de devengo)"),
                    FieldSpec("nif", 39, 9, "NIF del sujeto pasivo"),
                    FieldSpec("periodo", 48, 2, "Período (0A/1T-4T/01-12, alfanumérico)"),
                    FieldSpec("anagrama", 50, 4, "Anagrama del sujeto pasivo (alfanumérico)"),
                ),
        )

    // Tipo 550 spec confirmed. Full barcode 60 chars (AI "90" + payload 58 chars).
    // Cuaderno 65 AEB/CECA/UNACC — Autoliquidaciones Generalitat de Catalunya.
    // periodo is alphanumeric: "00" aperiódico, "A0" anual, "1T"-"4T" trimestral, "01"-"12" mensual.
    // datosAdicionales: 25 chars, free use by organismo (model-specific breakdown in PDF comments).
    val TIPO_550 =
        FormatSpec(
            tipo = "550",
            description = "Autoliquidaciones Generalitat de Catalunya",
            expectedLength = 58,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("importe", 4, 11, "Importe (2 decimales implícitos)"),
                    FieldSpec("ejercicio", 15, 4, "Ejercicio fiscal (YYYY)"),
                    FieldSpec("periodo", 19, 2, "Período (00/A0/1T-4T/01-12)"),
                    FieldSpec("justificante", 21, 13, "Número de justificante"),
                    FieldSpec("datosAdicionales", 34, 25, "Datos adicionales libres por modelo (alfanumérico)"),
                ),
        )

    // Tipo 551 spec confirmed. Full barcode 52 chars (AI "90" + payload 50 chars).
    // Cuaderno 65 AEB/CECA/UNACC — Liquidaciones Generalitat de Catalunya.
    // periodo always "00". modelo optional (may need to be entered manually if not in barcode).
    val TIPO_551 =
        FormatSpec(
            tipo = "551",
            description = "Liquidaciones Generalitat de Catalunya",
            expectedLength = 50,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("numeroJustificante", 4, 17, "Número de justificante (alfanumérico; blancos para completar)"),
                    FieldSpec("importe", 21, 12, "Importe (2 decimales implícitos)"),
                    FieldSpec("nif", 33, 9, "NIF del sujeto pasivo (alfanumérico)"),
                    FieldSpec("ejercicio", 42, 4, "Año del ejercicio (YYYY)"),
                    FieldSpec("periodo", 46, 2, "Período (siempre '00')"),
                    FieldSpec("modelo", 48, 3, "Número de modelo (numérico, opcional)"),
                ),
        )

    // Tipo 559 spec confirmed. Full barcode 50 chars (AI "90" + payload 48 chars).
    // Cuaderno 65 AEB/CECA — Autoliquidaciones Generalitat de Catalunya con programas de ayuda.
    // ejercicio/periodo = zeros if not applicable.
    val TIPO_559 =
        FormatSpec(
            tipo = "559",
            description = "Autoliquidaciones Generalitat de Catalunya con programas de ayuda",
            expectedLength = 48,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("importe", 4, 11, "Importe (2 decimales implícitos)"),
                    FieldSpec("ejercicio", 15, 4, "Ejercicio (ceros si no aplica)"),
                    FieldSpec("periodo", 19, 2, "Período (ceros si no aplica)"),
                    FieldSpec("datosAdicionales", 21, 25, "Datos adicionales por modelo"),
                    FieldSpec("modelo", 46, 3, "3 primeras posiciones del número de justificante"),
                ),
        )

    // ─── Appendix C: Declaraciones AEAT complejas ──────────────────────────────
    // Structure (36 chars): tipo(3) + nif(9) + modelo(5) + resultado(1) + modoPago(2) + ejercicio(4) + periodo(2) + importe(10)
    // resultado: 0=Ingreso, 1=Devolución, 2=Negativa compensación cónyuges, 3=Negativa, 4=Renuncia devolución,
    //            5=Compensar, 6=Sociedades CCT, 7=Negativa con resultado a deducir
    // modoPago: 00=No fraccionable/no domiciliable, 01-12=varios modos (ver PDF)

    private fun declaracionAeat(
        tipo: String,
        description: String,
    ) = FormatSpec(
        tipo = tipo,
        description = description,
        expectedLength = 36,
        tipoFieldStart = 1,
        fields =
            listOf(
                FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                FieldSpec("nif", 4, 9, "NIF del declarante"),
                FieldSpec("modelo", 13, 5, "Número de modelo AEAT"),
                FieldSpec("resultado", 18, 1, "Tipo de resultado (0-7)"),
                FieldSpec("modoPago", 19, 2, "Modo de pago/fraccionamiento (00-12)"),
                FieldSpec("ejercicio", 21, 4, "Ejercicio fiscal (YYYY)"),
                FieldSpec("periodo", 25, 2, "Período de la declaración"),
                FieldSpec("importe", 27, 10, "Importe (2 decimales implícitos)"),
            ),
    )

    // Tipo 512 spec confirmed. Full barcode 44 chars (AI "90" + payload 42 chars).
    // AEAT liquidaciones. Sub-fields of numeroJustificante exposed flat.
    val TIPO_512 =
        FormatSpec(
            tipo = "512",
            description = "Liquidaciones de la AEAT",
            expectedLength = 42,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("modelo", 4, 3, "Número de modelo"),
                    FieldSpec("importe", 7, 14, "Importe en céntimos"),
                    FieldSpec("delegacion", 21, 2, "Delegación"),
                    FieldSpec("añoImpresion", 23, 2, "Año de impresión"),
                    FieldSpec("numeroSerie", 25, 8, "Número de serie"),
                    FieldSpec("digitoControl", 33, 1, "Dígito de control del justificante"),
                    FieldSpec("nif", 34, 9, "NIF del contribuyente"),
                ),
        )

    // Tipo 530 spec confirmed. Full barcode 50 chars (AI "90" + payload 48 chars).
    // AEAT borrador de declaración de IRPF. modelo always "100", periodo always "0A".
    val TIPO_530 =
        FormatSpec(
            tipo = "530",
            description = "AEAT borrador de declaración de IRPF",
            expectedLength = 48,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("numeroJustificante", 4, 13, "Número de justificante del borrador"),
                    FieldSpec("importe", 17, 12, "Importe total de la declaración"),
                    FieldSpec("modelo", 29, 3, "Número de modelo (fijo '100')"),
                    FieldSpec("ejercicio", 32, 2, "Ejercicio de devengo (2 últimas cifras)"),
                    FieldSpec("nif", 34, 9, "NIF del sujeto pasivo (alfanumérico)"),
                    FieldSpec("periodo", 43, 2, "Período (fijo '0A')"),
                    FieldSpec("anagrama", 45, 4, "Anagrama del sujeto pasivo (alfanumérico)"),
                ),
        )

    // Tipo 532 spec confirmed. Full barcode 63 chars (AI "90" + payload 61 chars).
    // AEAT declaraciones con programas de ayuda o módulos de impresión.
    // IBAN field is alphanumeric (ES + check digits + bank/account).
    // tipoDeclaracion: 0=Ingreso, 1=Devolución, 2=Neg.comp.cónyuges, 3=Negativa,
    //                  4=Renuncia devolución, 5=Compensar, 6=Soc.CCT
    // tipoEspecifico: 00=No frac/no dom, 01=Frac.pago único, 02-23=varios, 04-06=dom, 09-12=varios
    val TIPO_532 =
        FormatSpec(
            tipo = "532",
            description = "AEAT declaraciones con programas de ayuda o módulos de impresión",
            expectedLength = 61,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("tipoDeclaracion", 4, 1, "Tipo de declaración (0-6)"),
                    FieldSpec("tipoEspecifico", 5, 2, "Tipo específico (00-12)"),
                    FieldSpec("iban", 7, 24, "IBAN cta. devolución/cargo/domiciliación (ESXXBBBBDCCCCCCCCCCC)"),
                    FieldSpec("importeIngresar", 31, 12, "Importe a ingresar/devolver"),
                    FieldSpec("importeTotal", 43, 12, "Importe total de la declaración"),
                    FieldSpec("modelo", 55, 3, "Código de modelo"),
                    FieldSpec("ejercicio", 58, 2, "Ejercicio (2 últimas cifras)"),
                    FieldSpec("periodo", 60, 2, "Período (0A/1T-4T/01-12, alfanumérico)"),
                ),
        )

    // Tipo 533 spec confirmed. Full barcode 40 chars (AI "90" + payload 38 chars).
    // AEAT tasas. Sub-fields of numeroJustificante exposed flat.
    val TIPO_533 =
        FormatSpec(
            tipo = "533",
            description = "AEAT tasas",
            expectedLength = 38,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("importe", 4, 12, "Importe en céntimos"),
                    FieldSpec("modelo", 16, 3, "Número de modelo"),
                    FieldSpec("codigoTasa", 19, 3, "Código de tasa"),
                    FieldSpec("numeroSerie", 22, 6, "Número de serie"),
                    FieldSpec("digitoControlJustificante", 28, 1, "Dígito de control del justificante"),
                    FieldSpec("nif", 29, 9, "NIF del contribuyente"),
                    FieldSpec("digitoParidad", 38, 1, "Dígito de paridad (= '0')"),
                ),
        )

    // Tipo 558 spec confirmed. Full barcode 58 chars (AI "90" + payload 56 chars).
    // "Formato corto" of 532 — no modelo field. tipoDeclaracion adds "7"=Neg.con resultado a deducir.
    // Position inconsistency in PDF: trusting position columns (33-44=12, 45-54=10) over LONG column.
    val TIPO_558 =
        FormatSpec(
            tipo = "558",
            description = "AEAT declaraciones con programas de ayuda (formato corto)",
            expectedLength = 56,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("tipoDeclaracion", 4, 1, "Tipo de declaración (0-7)"),
                    FieldSpec("tipoEspecifico", 5, 2, "Tipo específico (00-12)"),
                    FieldSpec("iban", 7, 24, "IBAN cta. devolución/cargo/domiciliación (ESXXBBBBDCCCCCCCCCCC)"),
                    FieldSpec("importeIngresar", 31, 12, "Importe a ingresar/devolver en céntimos"),
                    FieldSpec("importeTotal", 43, 10, "Importe total de la declaración en céntimos"),
                    FieldSpec("ejercicio", 53, 2, "Ejercicio (2 últimas cifras)"),
                    FieldSpec("periodo", 55, 2, "Período (0A/1T-4T/01-12, alfanumérico)"),
                ),
        )

    // ─── Cuaderno 57 AEB/CECA/UNACC ────────────────────────────────────────────
    // 507 and 581 share identical structure (46 total / 44 payload).
    // 507 = Recibos y otros (ventanilla/autoservicio)
    // 581 = Tesorería General de la Seguridad Social

    private fun cuaderno57(
        tipo: String,
        description: String,
    ) = FormatSpec(
        tipo = tipo,
        description = description,
        expectedLength = 44,
        tipoFieldStart = 1,
        fields =
            listOf(
                FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                FieldSpec("emisora", 4, 8, "Emisora"),
                FieldSpec("sufijo", 12, 3, "Sufijo"),
                FieldSpec("referencia", 15, 13, "Referencia"),
                FieldSpec("identificacion", 28, 6, "Identificación"),
                FieldSpec("importe", 34, 10, "Importe en céntimos"),
                FieldSpec("digitoParidad", 44, 1, "Dígito de paridad (= '0')"),
            ),
    )

    val TIPO_507 = cuaderno57("507", "Recibos y otros - cobros por ventanilla o autoservicio")
    val TIPO_581 = cuaderno57("581", "Tesorería General de la Seguridad Social")

    // Tipo 529 spec confirmed. Full barcode 54 chars (AI "90" + payload 52 chars).
    // Cuaderno 57 — completo con entidad tesorera + fecha límite. Importe 8 chars (not 10).
    val TIPO_529 =
        FormatSpec(
            tipo = "529",
            description = "Recibos y otros con entidad tesorera y fecha límite - cobros por ventanilla",
            expectedLength = 52,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("entidadTesorera", 4, 4, "Entidad tesorera"),
                    FieldSpec("fechaLimitePago", 8, 6, "Fecha límite de pago (DDMMAA)"),
                    FieldSpec("emisora", 14, 8, "Emisora"),
                    FieldSpec("sufijo", 22, 3, "Sufijo"),
                    FieldSpec("referencia", 25, 13, "Referencia"),
                    FieldSpec("identificacion", 38, 6, "Identificación"),
                    FieldSpec("importe", 44, 8, "Importe en céntimos"),
                    FieldSpec("digitoParidad", 52, 1, "Dígito de paridad (= '0')"),
                ),
        )

    // Tipo 528 spec confirmed. Full barcode 52 chars (AI "90" + payload 50 chars).
    // Cuaderno 57 — completo (emisora+sufijo+referencia+id+importe) + fecha límite al final.
    val TIPO_528 =
        FormatSpec(
            tipo = "528",
            description = "Recibos y otros con fecha límite de pago - cobros por ventanilla",
            expectedLength = 50,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("emisora", 4, 8, "Emisora"),
                    FieldSpec("sufijo", 12, 3, "Sufijo"),
                    FieldSpec("referencia", 15, 13, "Referencia"),
                    FieldSpec("identificacion", 28, 6, "Identificación"),
                    FieldSpec("importe", 34, 10, "Importe en céntimos"),
                    FieldSpec("fechaLimitePago", 44, 6, "Fecha límite de pago (DDMMAA)"),
                    FieldSpec("digitoParidad", 50, 1, "Dígito de paridad (= '0')"),
                ),
        )

    // Tipo 525 spec confirmed. Full barcode 46 chars (AI "90" + payload 44 chars).
    // Cuaderno 57 — sin importe, con identificacion + entidad tesorera + fecha límite.
    val TIPO_525 =
        FormatSpec(
            tipo = "525",
            description = "Recibos y otros sin importe, con entidad tesorera y fecha límite",
            expectedLength = 44,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("entidadTesorera", 4, 4, "Entidad tesorera"),
                    FieldSpec("fechaLimitePago", 8, 6, "Fecha límite de pago (DDMMAA)"),
                    FieldSpec("emisora", 14, 8, "Emisora"),
                    FieldSpec("sufijo", 22, 3, "Sufijo"),
                    FieldSpec("referencia", 25, 13, "Referencia"),
                    FieldSpec("identificacion", 38, 6, "Identificación"),
                    FieldSpec("digitoParidad", 44, 1, "Dígito de paridad (= '0')"),
                ),
        )

    // Tipo 524 spec confirmed. Full barcode 36 chars (AI "90" + payload 34 chars).
    // Cuaderno 57 — variante sin importe (con identificacion).
    val TIPO_524 =
        FormatSpec(
            tipo = "524",
            description = "Recibos y otros sin importe - cobros por ventanilla",
            expectedLength = 34,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("emisora", 4, 8, "Emisora"),
                    FieldSpec("sufijo", 12, 3, "Sufijo"),
                    FieldSpec("referencia", 15, 13, "Referencia"),
                    FieldSpec("identificacion", 28, 6, "Identificación"),
                    FieldSpec("digitoParidad", 34, 1, "Dígito de paridad (= '0')"),
                ),
        )

    // Tipo 515 spec confirmed. Full barcode 40 chars (AI "90" + payload 38 chars).
    // Cuaderno 57 — variante sin id/importe pero con entidad tesorera y fecha límite (DDMMAA).
    val TIPO_515 =
        FormatSpec(
            tipo = "515",
            description = "Recibos y otros sin id/importe, con entidad tesorera y fecha límite",
            expectedLength = 38,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("entidadTesorera", 4, 4, "Entidad tesorera"),
                    FieldSpec("fechaLimitePago", 8, 6, "Fecha límite de pago (DDMMAA)"),
                    FieldSpec("emisora", 14, 8, "Emisora"),
                    FieldSpec("sufijo", 22, 3, "Sufijo"),
                    FieldSpec("referencia", 25, 13, "Referencia"),
                    FieldSpec("digitoParidad", 38, 1, "Dígito de paridad (= '0')"),
                ),
        )

    // Tipo 514 spec confirmed. Full barcode 30 chars (AI "90" + payload 28 chars).
    // Cuaderno 57 — variante sin identificacion ni importe.
    val TIPO_514 =
        FormatSpec(
            tipo = "514",
            description = "Recibos y otros sin identificación ni importe - cobros por ventanilla",
            expectedLength = 28,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("emisora", 4, 8, "Emisora"),
                    FieldSpec("sufijo", 12, 3, "Sufijo"),
                    FieldSpec("referencia", 15, 13, "Referencia"),
                    FieldSpec("digitoParidad", 28, 1, "Dígito de paridad (= '0')"),
                ),
        )

    // ─── Lotería de Catalunya ──────────────────────────────────────────────────

    // Tipo 571 spec confirmed. Full barcode 23 chars (AI "90" + payload 21 chars).
    // Lotería de Catalunya — juegos on-line. digitosControl is alphanumeric.
    val TIPO_571 =
        FormatSpec(
            tipo = "571",
            description = "Lotería de Catalunya - juegos on-line",
            expectedLength = 21,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("numeroJuego", 4, 2, "Número de juego"),
                    FieldSpec("ultimosDigitosIP", 6, 2, "Últimos dígitos IP"),
                    FieldSpec("marcaTiempo", 8, 9, "Marca de tiempo"),
                    FieldSpec("secuencial", 17, 3, "Número secuencial"),
                    FieldSpec("digitosControl", 20, 2, "Dígitos de control (alfanumérico)"),
                ),
        )

    // Tipo 573 spec confirmed. Full barcode 27 chars (AI "90" + payload 25 chars).
    // Lotería de Catalunya — juegos pasivos pre-impresos (La Grossa). Same structure as 572.
    val TIPO_573 =
        FormatSpec(
            tipo = "573",
            description = "Lotería de Catalunya - juegos pasivos pre-impresos (La Grossa)",
            expectedLength = 25,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("numeroJuego", 4, 4, "Número de juego"),
                    FieldSpec("numeroLibro", 8, 6, "Número de libro"),
                    FieldSpec("numeroTicket", 14, 3, "Número de ticket"),
                    FieldSpec("numeroValidacion", 17, 8, "Número de validación encriptado"),
                    FieldSpec("digitoControl", 25, 1, "Dígito de control"),
                ),
        )

    // Tipo 572 spec confirmed. Full barcode 27 chars (AI "90" + payload 25 chars).
    // Lotería de Catalunya — juegos instant (rascas).
    val TIPO_572 =
        FormatSpec(
            tipo = "572",
            description = "Lotería de Catalunya - juegos instant",
            expectedLength = 25,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("numeroJuego", 4, 4, "Número de juego"),
                    FieldSpec("numeroLibro", 8, 6, "Número de libro"),
                    FieldSpec("numeroTicket", 14, 3, "Número de ticket"),
                    FieldSpec("numeroValidacion", 17, 8, "Número de validación encriptado"),
                    FieldSpec("digitoControl", 25, 1, "Dígito de control"),
                ),
        )

    // ─── Loterías y Apuestas del Estado ────────────────────────────────────────

    // Tipo 586 spec confirmed. Full barcode 18 chars (AI "90" + payload 16 chars).
    val TIPO_586 =
        FormatSpec(
            tipo = "586",
            description = "Loterías y Apuestas del Estado - identificación administración de lotería",
            expectedLength = 16,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("codigoAdminLoteria", 4, 12, "Código de administración de lotería"),
                    FieldSpec("digitoParidad", 16, 1, "Dígito de paridad (= '0')"),
                ),
        )

    // ─── ONCE ──────────────────────────────────────────────────────────────────

    // Tipo 591 spec confirmed. Full barcode 28 chars (AI "90" + payload 26 chars).
    // ONCE juego pasivo electrónico. Same structure as 589.
    val TIPO_591 =
        FormatSpec(
            tipo = "591",
            description = "ONCE - juego pasivo electrónico",
            expectedLength = 26,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("versionCodigo", 4, 1, "Versión del código"),
                    FieldSpec("digitosControl", 5, 2, "Dígitos de control"),
                    FieldSpec("identificadorProducto", 7, 2, "Identificador de producto pasivos TPV"),
                    FieldSpec("diaJulianoVenta", 9, 3, "Día juliano de venta"),
                    FieldSpec("numeroApuesta", 12, 8, "Número de apuesta"),
                    FieldSpec("digitosControlApuesta", 20, 3, "Dígitos de control de la apuesta"),
                    FieldSpec("contadorDiario", 23, 4, "Código interno ONCE - Gols"),
                ),
        )

    // Tipo 590 spec confirmed. Full barcode 24 chars (AI "90" + payload 22 chars).
    // ONCE juego pasivo preimpreso. versionCodigo always "1".
    val TIPO_590 =
        FormatSpec(
            tipo = "590",
            description = "ONCE - juego pasivo preimpreso",
            expectedLength = 22,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("versionCodigo", 4, 1, "Versión del código (= '1')"),
                    FieldSpec("digitosControl", 5, 2, "Dígitos de control"),
                    FieldSpec("identificadorProducto", 7, 2, "Identificador de producto"),
                    FieldSpec("fechaSorteo", 9, 6, "Fecha del sorteo (ddmmaa)"),
                    FieldSpec("numeroApuesta", 15, 5, "Número de apuesta"),
                    FieldSpec("numeroSerie", 20, 3, "Número de serie"),
                ),
        )

    // Tipo 589 spec confirmed. Full barcode 28 chars (AI "90" + payload 26 chars).
    // ONCE — emisión pago documentos activos TPV (cupón electrónico).
    // contadorDiario = Gols (número secuencial irrepetible por premio).
    val TIPO_589 =
        FormatSpec(
            tipo = "589",
            description = "ONCE - emisión pago documentos activos TPV",
            expectedLength = 26,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("versionCodigo", 4, 1, "Versión del código"),
                    FieldSpec("digitosControl", 5, 2, "Dígitos de control"),
                    FieldSpec("identificadorProducto", 7, 2, "Identificador de producto LT1"),
                    FieldSpec("diaJulianoVenta", 9, 3, "Día juliano de venta"),
                    FieldSpec("numeroSerieApuesta", 12, 8, "Número de serie de la apuesta"),
                    FieldSpec("digitosControlApuesta", 20, 3, "Dígitos de control de la apuesta"),
                    FieldSpec("contadorDiario", 23, 4, "Contador diario continuo Gols"),
                ),
        )

    // Tipo 588 spec confirmed. Full barcode 22 chars (AI "90" + payload 20 chars).
    // ONCE paquete semanal.
    val TIPO_588 =
        FormatSpec(
            tipo = "588",
            description = "ONCE - paquete semanal",
            expectedLength = 20,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("codigoVendedor", 4, 8, "Código de vendedor"),
                    FieldSpec("numeroInternoONCE", 12, 7, "Número interno ONCE"),
                    FieldSpec("digitosControl", 19, 2, "Dígitos de control"),
                ),
        )

    // Tipo 587 spec confirmed. Full barcode 24 chars (AI "90" + payload 22 chars).
    // ONCE albarán de distribución.
    val TIPO_587 =
        FormatSpec(
            tipo = "587",
            description = "ONCE - albarán de distribución",
            expectedLength = 22,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("numeroInternoONCE", 4, 8, "Número interno ONCE"),
                    FieldSpec("entidadFinanciera", 12, 4, "Código Banco España"),
                    FieldSpec("codigoOficina", 16, 4, "Código de oficina"),
                    FieldSpec("digitoParidad", 20, 1, "Dígito de paridad (= '0')"),
                    FieldSpec("digitosControl", 21, 2, "Dígitos de control"),
                ),
        )

    // Tipo 531 spec confirmed. Full barcode 26 chars (AI "90" + payload 24 chars).
    // ONCE — sobres de vendedores. Timestamp encoded as año(1)+diaJuliano(3)+hora(2)+minutos(2).
    val TIPO_531 =
        FormatSpec(
            tipo = "531",
            description = "ONCE - etiqueta sobres de vendedores",
            expectedLength = 24,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("numeroUnicoVendedor", 4, 8, "Número único de vendedor"),
                    FieldSpec("año", 12, 1, "Año (último dígito)"),
                    FieldSpec("diaJuliano", 13, 3, "Día juliano"),
                    FieldSpec("hora", 16, 2, "Hora"),
                    FieldSpec("minutos", 18, 2, "Minutos"),
                    FieldSpec("numeroSecuencial", 20, 3, "Número secuencial"),
                    FieldSpec("digitosControl", 23, 2, "Dígitos de control"),
                ),
        )

    // ─── Entidades Depositarias ────────────────────────────────────────────────

    // Tipo 020 spec confirmed. Full barcode 48 chars (AI "90" + payload 46 chars).
    // All fields alphanumeric (EAN 128-B). ISIN + CCV = securities account data.
    // tipoRespuesta: "1"=delegación, "2"=voto a distancia o asistencia.
    val TIPO_020 =
        FormatSpec(
            tipo = "020",
            description = "Entidades depositarias - tarjeta de asistencia a juntas",
            expectedLength = 46,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("codigoISIN", 4, 12, "Código ISIN (alfanumérico)"),
                    FieldSpec("ccv", 16, 20, "Código Cuenta de Valores (alfanumérico)"),
                    FieldSpec("numeroAcciones", 36, 10, "Número de acciones"),
                    FieldSpec("tipoRespuesta", 46, 1, "'1'=Delegación, '2'=Voto a distancia/Asistencia"),
                ),
        )

    // ─── Seguridad Social adicionales ──────────────────────────────────────────

    // Tipo 592 spec confirmed. Full barcode 63 chars (AI "90" + payload 61 chars).
    // Convenio URE's TGSS — Pago deudas vía ejecutiva y voluntaria fuera de plazo.
    // IBAN, NIF and numeroDocumento are alphanumeric (EAN 128-B).
    val TIPO_592 =
        FormatSpec(
            tipo = "592",
            description = "TGSS pago deudas en vía ejecutiva/voluntaria fuera de plazo",
            expectedLength = 61,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("cuentaRestringidaIBAN", 4, 24, "Cuenta restringida en formato IBAN"),
                    FieldSpec("nif", 28, 9, "NIF del deudor (alfanumérico)"),
                    FieldSpec("numeroDocumento", 37, 15, "Número de documento (alfanumérico; espacios a la derecha)"),
                    FieldSpec("importe", 52, 10, "Importe en céntimos"),
                ),
        )

    // ─── Comunidades Autónomas ──────────────────────────────────────────────────
    // Tipo 013 spec confirmed. Full barcode 18 chars (AI "90" + payload 16 chars).
    // codigoComunidad values: 61=Andalucía, 62=Aragón, 63=Asturias, 64=Baleares,
    //   65=Canarias, 66=Cantabria, 67=Cast.LaMancha, 68=CastillaLeón, 69=Cataluña,
    //   70=Extremadura, 71=Galicia, 72=Madrid, 73=Murcia, 74=Navarra, 75=PaísVasco,
    //   76=LaRioja, 77=Valencia, 78=Ceuta, 79=Melilla

    // Tipo 016 spec confirmed. Full barcode 23 chars (AI "90" + payload 21 chars).
    // Uses EAN 128-B for NIF (alfanumérico) and Anagrama (alfanumérico).
    // Anagrama = blancos para personas jurídicas.
    // Tipo 011 spec confirmed. Full barcode 18 chars (AI "21" + payload 16 chars).
    // IMPORTANT: uses AI "21" (not "90") — AEAT requirement, analogy with serial number AI.
    // Same payload structure as tipo 013.
    val TIPO_011 =
        FormatSpec(
            tipo = "011",
            description = "Documentos de autoliquidación de la AEAT",
            expectedLength = 16,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("modelo", 4, 3, "Número de modelo"),
                    FieldSpec("añoImpresion", 7, 1, "Año de impresión (último dígito)"),
                    FieldSpec("numeroSerie", 8, 8, "Número de serie"),
                    FieldSpec("digitoControl", 16, 1, "Dígito de control"),
                ),
        )

    // Tipo 010 spec confirmed. Full barcode 23 chars (AI "90" + payload 21 chars).
    // AEAT fiscal label. Same structure as 016 (CCAA) but for AEAT administración.
    // NIF and anagrama are alphanumeric (EAN 128-B).
    val TIPO_010 =
        FormatSpec(
            tipo = "010",
            description = "Etiqueta fiscal de la AEAT",
            expectedLength = 21,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("codigoAdministracion", 4, 5, "Código de administración"),
                    FieldSpec("nif", 9, 9, "NIF del contribuyente (alfanumérico)"),
                    FieldSpec("anagrama", 18, 4, "Anagrama (alfanumérico; blancos para jurídicas)"),
                ),
        )

    // Tipo 017 spec confirmed. Full barcode 24 chars (AI "90" + payload 22 chars).
    val TIPO_017 =
        FormatSpec(
            tipo = "017",
            description = "Documentos de autoliquidación Comunidades Autónomas (con dígito paridad)",
            expectedLength = 22,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("codigoOrganismo", 4, 5, "Código de organismo/emisor (con DC módulo 11)"),
                    FieldSpec("digitoParidad", 9, 1, "Dígito complementario paridad par (= '0')"),
                    FieldSpec("modelo", 10, 3, "Número de modelo"),
                    FieldSpec("añoImpresionTiradaSerie", 13, 1, "Año impresión/tirada/serie (a decidir por organismo)"),
                    FieldSpec("numeroSerie", 14, 8, "Número de serie"),
                    FieldSpec("digitoControl", 22, 1, "Dígito de control"),
                ),
        )

    val TIPO_016 =
        FormatSpec(
            tipo = "016",
            description = "Etiqueta identificación Comunidades Autónomas",
            expectedLength = 21,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("codigoOrganismo", 4, 5, "Código de organismo/emisor (con DC módulo 11)"),
                    FieldSpec("nif", 9, 9, "NIF del contribuyente (alfanumérico)"),
                    FieldSpec("anagrama", 18, 4, "Anagrama (alfanumérico; blancos para jurídicas)"),
                ),
        )

    val TIPO_013 =
        FormatSpec(
            tipo = "013",
            description = "Documentos de autoliquidación Comunidades Autónomas",
            expectedLength = 16,
            tipoFieldStart = 1,
            fields =
                listOf(
                    FieldSpec("tipo", 1, 3, "Indicador de tipo"),
                    FieldSpec("modelo", 4, 3, "Número de modelo"),
                    FieldSpec("añoImpresion", 7, 1, "Año de impresión (último dígito)"),
                    FieldSpec("codigoComunidad", 8, 2, "Código de Comunidad Autónoma (61-79)"),
                    FieldSpec("numeroSerie", 10, 6, "Número de serie"),
                    FieldSpec("digitoControl", 16, 1, "Dígito de control"),
                ),
        )

    // ─── All specs ─────────────────────────────────────────────────────────────

    val all: List<FormatSpec> =
        listOf(
            // Appendix A
            TIPO_504,
            TIPO_501,
            TIPO_502,
            TIPO_503,
            TIPO_508,
            TIPO_521,
            TIPO_522,
            TIPO_523,
            TIPO_507,
            TIPO_514,
            TIPO_515,
            TIPO_524,
            TIPO_525,
            TIPO_528,
            TIPO_529,
            TIPO_579,
            TIPO_580,
            TIPO_581,
            TIPO_582,
            TIPO_583,
            TIPO_584,
            TIPO_585,
            // Appendix B ventanilla
            TIPO_518,
            TIPO_526,
            TIPO_527,
            TIPO_534,
            TIPO_535,
            TIPO_564,
            TIPO_565,
            // Appendix B/C periodo
            TIPO_539,
            TIPO_550,
            TIPO_551,
            TIPO_559,
            // Appendix C declaraciones
            TIPO_512,
            TIPO_530,
            TIPO_532,
            TIPO_533,
            TIPO_558,
            // AEAT / Comunidades Autónomas
            TIPO_010,
            TIPO_011,
            TIPO_013,
            TIPO_016,
            TIPO_017,
            // Lotería Catalunya
            TIPO_571,
            TIPO_572,
            TIPO_573,
            // Loterías
            TIPO_586,
            // ONCE
            TIPO_531,
            TIPO_587,
            TIPO_588,
            TIPO_589,
            TIPO_590,
            TIPO_591,
            // Entidades depositarias
            TIPO_020,
            // Seguridad Social adicionales
            TIPO_592,
        )
}
