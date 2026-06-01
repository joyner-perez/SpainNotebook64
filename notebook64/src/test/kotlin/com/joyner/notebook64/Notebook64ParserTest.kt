package com.joyner.notebook64

import com.joyner.notebook64.parser.Notebook64Parser
import com.joyner.notebook64.spec.FormatRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Notebook64ParserTest {
    // Barcode example from Cuaderno 64 PDF page 12
    private val pdfExample = "0051111111330053424083405001000006764"
    private val pdfExampleWithAi = "(90)$pdfExample"

    // ─── GS1-128 prefix stripping ──────────────────────────────────────────────

    @Test
    fun `stripGS1Prefix removes parenthesised AI`() {
        assertEquals(pdfExample, Notebook64Parser.stripGS1Prefix(pdfExampleWithAi))
    }

    @Test
    fun `stripGS1Prefix leaves plain string unchanged`() {
        assertEquals(pdfExample, Notebook64Parser.stripGS1Prefix(pdfExample))
    }

    // ─── Auto-detection ────────────────────────────────────────────────────────

    @Test
    fun `auto-detection with plain string same as with AI prefix`() {
        val payload = "501" + "28" + "123" + "0" + "1" + "50" + "1234567" + "89" + "012345000" + "00"
        val withAi = parse("(90)$payload")
        val plain = parse(payload)
        // tipo is the same; withAi has 1 extra applicationIdentifier field at the front
        assertEquals(withAi.tipo, plain.tipo)
        assertEquals(withAi.fields.size, plain.fields.size + 1)
        assertEquals("applicationIdentifier", withAi.fields.first().name)
        assertEquals("90", withAi.fields.first().value)
        assertEquals(1, withAi.fields.first().startPos)
        // non-AI fields have position offset by 2
        assertEquals(plain.fields[0].startPos + 2, withAi.fields[1].startPos)
    }

    // ─── Tipo 534 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 534 extracts all expected fields`() {
        // 3+13+4+8+9+4+6 = 47 chars (concepto/NIF/anagrama/codigoTerritorial alfanuméricos)
        val payload = "534" + "2024000123456" + "    " + "00123456" + "12345678A" + "JLGR" + "280001"
        assertEquals(47, payload.length)
        val result = parse(payload)
        assertEquals("534", result.tipo)
        assertEquals("2024000123456", result.value("numeroJustificante"))
        assertEquals("    ", result.value("concepto"))
        assertEquals("00123456", result.value("importe"))
        assertEquals("12345678A", result.value("nif"))
        assertEquals("JLGR", result.value("anagrama"))
        assertEquals("280001", result.value("codigoTerritorial"))
    }

    @Test
    fun `tipo 534 total fields count`() {
        val payload = "534" + "2024000123456" + "    " + "00123456" + "12345678A" + "JLGR" + "280001"
        assertEquals(7, parse(payload).fields.size)
    }

    // ─── Explicit tipo override ────────────────────────────────────────────────

    @Test
    fun `explicit tipo overrides auto-detection`() {
        val payload = "501" + "28" + "123" + "0" + "1" + "50" + "1234567" + "89" + "012345000" + "00"
        val result = parse(payload, tipo = "501")
        assertEquals("501", result.tipo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `explicit tipo with wrong length throws`() {
        // tipo 512 expects 42 chars, give 32-char tipo-501 string
        val payload = "501" + "28" + "123" + "0" + "1" + "50" + "1234567" + "89" + "012345000" + "00"
        parse(payload, tipo = "512")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `unknown tipo throws`() {
        val payload = "501" + "28" + "123" + "0" + "1" + "50" + "1234567" + "89" + "012345000" + "00"
        parse(payload, tipo = "999")
    }

    // ─── FormatRegistry ────────────────────────────────────────────────────────

    @Test
    fun `registry has all expected tipos`() {
        val expected =
            listOf(
                "501",
                "502",
                "503",
                "508",
                "521",
                "522",
                "523",
                "579",
                "580",
                "581",
                "582",
                "583",
                "584",
                "585",
                "518",
                "526",
                "527",
                "534",
                "535",
                "564",
                "565",
                "539",
                "550",
                "551",
                "559",
                "512",
                "530",
                "532",
                "558",
            )
        expected.forEach { tipo ->
            assertNotNull("FormatRegistry missing tipo $tipo", FormatRegistry.getSpec(tipo))
        }
    }

    @Test
    fun `registry returns null for unknown tipo`() {
        assertNull(FormatRegistry.getSpec("999"))
    }

    // ─── Tipo 501 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 501 spec has correct length and tipoFieldStart`() {
        val spec = FormatRegistry.getSpec("501")!!
        assertEquals(32, spec.expectedLength)
        assertEquals(1, spec.tipoFieldStart)
    }

    @Test
    fun `tipo 501 spec has all expected fields`() {
        val spec = FormatRegistry.getSpec("501")!!
        val names = spec.fields.map { it.name }
        assertTrue(names.contains("provincia"))
        assertTrue(names.contains("municipio"))
        assertTrue(names.contains("ejercicio"))
        assertTrue(names.contains("remesa"))
        assertTrue(names.contains("tributo"))
        assertTrue(names.contains("notificacion"))
        assertTrue(names.contains("digitosControlClave"))
        assertTrue(names.contains("importe"))
        assertTrue(names.contains("digitosControlImporte"))
        assertEquals(10, spec.fields.size)
    }

    @Test
    fun `tipo 501 parses sample payload correctly`() {
        // Payload (32 chars): tipo(3)+provincia(2)+municipio(3)+ejercicio(1)+remesa(1)
        //                     +tributo(2)+notificacion(7)+dcClave(2)+importe(9)+dcImporte(2)
        val sample = "501281230150012345678901234500"
        //            ^^^--tipo
        //               ^^--provincia=28 (Madrid)
        //                 ^^^--municipio=123
        //                    ^--ejercicio=0
        //                     ^--remesa=1
        //                      ^^--tributo=50
        //                        ^^^^^^^--notificacion=1234567
        //                               ^^--dcClave=89
        //                                 ^^^^^^^^^--importe=012345000
        //                                          ^^--dcImporte=00  ... but only 29 chars above
        // Build exact 32-char payload:
        val payload = "501" + "28" + "123" + "0" + "1" + "50" + "1234567" + "89" + "012345000" + "00"
        assertEquals(32, payload.length)
        val result = parse(payload)
        assertEquals("501", result.tipo)
        assertEquals("501", result.value("tipo"))
        assertEquals("28", result.value("provincia"))
        assertEquals("123", result.value("municipio"))
        assertEquals("0", result.value("ejercicio"))
        assertEquals("1", result.value("remesa"))
        assertEquals("50", result.value("tributo"))
        assertEquals("1234567", result.value("notificacion"))
        assertEquals("89", result.value("digitosControlClave"))
        assertEquals("012345000", result.value("importe"))
        assertEquals("00", result.value("digitosControlImporte"))
    }

    @Test
    fun `tipo 501 auto-detected from 32 char payload`() {
        val payload = "501" + "28" + "123" + "0" + "1" + "50" + "1234567" + "89" + "012345000" + "00"
        val result = parse(payload)
        assertEquals("501", result.tipo)
    }

    @Test
    fun `tipo 501 and tipo 534 do not collide in auto-detection`() {
        val payload501 = "501" + "28" + "123" + "0" + "1" + "50" + "1234567" + "89" + "012345000" + "00"
        val payload534 = "534" + "2024000123456" + "    " + "00123456" + "12345678A" + "JLGR" + "280001"
        assertEquals(32, payload501.length)
        assertEquals(47, payload534.length)
        assertEquals("501", parse(payload501).tipo)
        assertEquals("534", parse(payload534).tipo)
    }

    // ─── Tipo 535 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 535 parses sample payload correctly`() {
        // 3+13+8+9+6 = 39 chars
        val payload = "535" + "2024000123456" + "00123456" + "12345678A" + "280001"
        assertEquals(39, payload.length)
        val result = parse(payload)
        assertEquals("535", result.tipo)
        assertEquals("2024000123456", result.value("numeroJustificante"))
        assertEquals("00123456", result.value("importe"))
        assertEquals("12345678A", result.value("nif"))
        assertEquals("280001", result.value("codigoTerritorial"))
    }

    // ─── Tipo 527 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 527 parses sample payload correctly`() {
        // 3+6+3+6+5+4+3+1+1 = 32 chars
        val payload = "527" + "300001" + "123" + "620001" + "00001" + "2024" + "100" + "7" + "0"
        assertEquals(32, payload.length)
        val result = parse(payload)
        assertEquals("527", result.tipo)
        assertEquals("300001", result.value("unidadGestora"))
        assertEquals("123", result.value("entidadEmisora"))
        assertEquals("620001", result.value("modeloConceptoPresupuestario"))
        assertEquals("00001", result.value("numeroSecuencialImpreso"))
        assertEquals("2024", result.value("ejercicioImpresion"))
        assertEquals("100", result.value("claseEstado"))
        assertEquals("7", result.value("digitoControlN28"))
        assertEquals("0", result.value("digitoParidad"))
    }

    // ─── Tipo 526 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 526 parses sample payload correctly`() {
        // 3+6+3+6+5+4+3+1+1+12 = 44 chars
        val payload = "526" + "300001" + "123" + "620001" + "00001" + "2024" + "100" + "7" + "0" + "000000067890"
        assertEquals(44, payload.length)
        val result = parse(payload)
        assertEquals("526", result.tipo)
        assertEquals("300001", result.value("unidadGestora"))
        assertEquals("123", result.value("entidadEmisora"))
        assertEquals("620001", result.value("modeloConceptoPresupuestario"))
        assertEquals("00001", result.value("numeroSecuencialImpreso"))
        assertEquals("2024", result.value("ejercicioImpresion"))
        assertEquals("100", result.value("claseEstado"))
        assertEquals("7", result.value("digitoControlN28"))
        assertEquals("0", result.value("digitoParidad"))
        assertEquals("000000067890", result.value("importe"))
    }

    // ─── Tipo 518 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 518 parses sample payload correctly`() {
        // 3+5+6+13+15+9+4 = 55 chars
        val payload =
            "518" + "72001" + "280001" +
                "2024000123456" +
                "000000000123456" +
                "12345678A" +
                "JLGR"
        assertEquals(55, payload.length)
        val result = parse(payload)
        assertEquals("518", result.tipo)
        assertEquals("72001", result.value("codigoOrganismo"))
        assertEquals("280001", result.value("codigoTerritorial"))
        assertEquals("2024000123456", result.value("numeroJustificante"))
        assertEquals("000000000123456", result.value("importe"))
        assertEquals("12345678A", result.value("nif"))
        assertEquals("JLGR", result.value("opcional"))
    }

    @Test
    fun `tipo 518 spec has correct length 55`() {
        val spec = FormatRegistry.getSpec("518")!!
        assertEquals(55, spec.expectedLength)
        assertEquals(7, spec.fields.size)
    }

    // ─── Tipo 017 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 017 parses sample payload correctly`() {
        // 3+5+1+3+1+8+1 = 22 chars
        val payload = "017" + "72001" + "0" + "620" + "3" + "12345678" + "9"
        assertEquals(22, payload.length)
        val result = parse(payload)
        assertEquals("017", result.tipo)
        assertEquals("72001", result.value("codigoOrganismo"))
        assertEquals("0", result.value("digitoParidad"))
        assertEquals("620", result.value("modelo"))
        assertEquals("3", result.value("añoImpresionTiradaSerie"))
        assertEquals("12345678", result.value("numeroSerie"))
        assertEquals("9", result.value("digitoControl"))
    }

    // ─── Tipo 016 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 016 parses sample payload correctly`() {
        // 3+5+9+4 = 21 chars (NIF y anagrama alfanuméricos)
        val payload = "016" + "72001" + "12345678A" + "JLGR"
        assertEquals(21, payload.length)
        val result = parse(payload)
        assertEquals("016", result.tipo)
        assertEquals("72001", result.value("codigoOrganismo"))
        assertEquals("12345678A", result.value("nif"))
        assertEquals("JLGR", result.value("anagrama"))
    }

    @Test
    fun `tipo 016 jurídica has spaces in anagrama`() {
        val payload = "016" + "72001" + "A12345678" + "    "
        assertEquals(21, payload.length)
        val result = parse(payload)
        assertEquals("    ", result.value("anagrama"))
    }

    // ─── Tipo 013 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 013 parses sample payload correctly`() {
        // 3+3+1+2+6+1 = 16 chars
        val payload = "013" + "620" + "3" + "72" + "123456" + "7"
        assertEquals(16, payload.length)
        val result = parse(payload)
        assertEquals("013", result.tipo)
        assertEquals("620", result.value("modelo"))
        assertEquals("3", result.value("añoImpresion"))
        assertEquals("72", result.value("codigoComunidad")) // Madrid
        assertEquals("123456", result.value("numeroSerie"))
        assertEquals("7", result.value("digitoControl"))
    }

    @Test
    fun `tipo 013 parses with AI prefix`() {
        val payload = "013" + "620" + "3" + "72" + "123456" + "7"
        assertEquals(parse("(90)$payload").tipo, parse(payload).tipo)
    }

    // ─── Tipo 585 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 585 parses sample payload correctly`() {
        // 3+2+3+1+14+10+8+1 = 42 chars
        val payload = "585" + "28" + "123" + "5" + "12345678901234" + "2601202812" + "00067890" + "0"
        assertEquals(42, payload.length)
        val result = parse(payload)
        assertEquals("585", result.tipo)
        assertEquals("28", result.value("provincia"))
        assertEquals("123", result.value("discriminante"))
        assertEquals("5", result.value("digitoControlEmisora"))
        assertEquals("12345678901234", result.value("identificador"))
        assertEquals("2601202812", result.value("control"))
        assertEquals("00067890", result.value("importe"))
        assertEquals("0", result.value("digitoParidad"))
    }

    // ─── Tipo 584 — spec confirmada (alfanumérico) ─────────────────────────────

    @Test
    fun `tipo 584 parses sample payload correctly`() {
        // 3+2+3+1+9+10+8+11 = 47 chars (matricula es alfanumérico)
        val payload = "584" + "28" + "123" + "5" + "123456789" + "2601202812" + "00067890" + "1234ABC5678"
        assertEquals(47, payload.length)
        val result = parse(payload)
        assertEquals("584", result.tipo)
        assertEquals("28", result.value("provincia"))
        assertEquals("123", result.value("discriminante"))
        assertEquals("5", result.value("digitoControlEmisora"))
        assertEquals("123456789", result.value("numeroBoletín"))
        assertEquals("2601202812", result.value("control"))
        assertEquals("00067890", result.value("importe"))
        assertEquals("1234ABC5678", result.value("matricula"))
    }

    // ─── Tipo 583 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 583 parses sample payload correctly`() {
        // 3+2+3+1+17+10+8 = 44 chars
        val payload = "583" + "28" + "123" + "5" + "12345678901234567" + "2601202812" + "00067890"
        assertEquals(44, payload.length)
        val result = parse(payload)
        assertEquals("583", result.tipo)
        assertEquals("28", result.value("provincia"))
        assertEquals("123", result.value("discriminante"))
        assertEquals("5", result.value("digitoControlEmisora"))
        assertEquals("12345678901234567", result.value("identificador"))
        assertEquals("2601202812", result.value("control"))
        assertEquals("00067890", result.value("importe"))
    }

    // ─── Tipo 582 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 582 parses sample payload correctly`() {
        // 3+2+3+1+17+5+2+1 = 34 chars
        val payload = "582" + "28" + "123" + "5" + "12345678901234567" + "98765" + "12" + "0"
        assertEquals(34, payload.length)
        val result = parse(payload)
        assertEquals("582", result.tipo)
        assertEquals("28", result.value("provincia"))
        assertEquals("123", result.value("discriminante"))
        assertEquals("5", result.value("digitoControlEmisora"))
        assertEquals("12345678901234567", result.value("identificador"))
        assertEquals("98765", result.value("discriminanteGestor"))
        assertEquals("12", result.value("digitosControlGestor"))
        assertEquals("0", result.value("digitoParidad"))
    }

    // ─── Tipo 580 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 580 parses sample payload correctly`() {
        // 3+2+3+1+13+11+10+1 = 44 chars
        val payload =
            "580" + "28" + "123" + "5" +
                "1234567890123" +
                "12345678901" +
                "2601202812" +
                "0"
        assertEquals(44, payload.length)
        val result = parse(payload)
        assertEquals("580", result.tipo)
        assertEquals("28", result.value("provincia"))
        assertEquals("123", result.value("discriminante"))
        assertEquals("5", result.value("digitoControlEmisora"))
        assertEquals("1234567890123", result.value("codigoSujeto"))
        assertEquals("12345678901", result.value("referencia"))
        assertEquals("2601202812", result.value("control"))
        assertEquals("0", result.value("digitoParidad"))
    }

    // ─── Tipo 579 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 579 parses sample payload correctly`() {
        // 3+2+3+1+3+10+2+2+8 = 34 chars
        val payload = "579" + "28" + "123" + "5" + "456" + "1234567890" + "24" + "78" + "00067890"
        assertEquals(34, payload.length)
        val result = parse(payload)
        assertEquals("579", result.tipo)
        assertEquals("28", result.value("provincia"))
        assertEquals("123", result.value("discriminante"))
        assertEquals("5", result.value("digitoControlEmisora"))
        assertEquals("456", result.value("tributo"))
        assertEquals("1234567890", result.value("referencia"))
        assertEquals("24", result.value("año"))
        assertEquals("78", result.value("digitosControlRecibo"))
        assertEquals("00067890", result.value("importe"))
    }

    // ─── Tipo 523 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 523 parses sample payload correctly`() {
        // 3+2+3+1+3+1+8+1 = 22 chars
        val payload = "523" + "28" + "123" + "5" + "456" + "2" + "12345678" + "9"
        assertEquals(22, payload.length)
        val result = parse(payload)
        assertEquals("523", result.tipo)
        assertEquals("28", result.value("provincia"))
        assertEquals("123", result.value("municipio"))
        assertEquals("5", result.value("digitoControlEmisora"))
        assertEquals("456", result.value("codigoTributoModelo"))
        assertEquals("2", result.value("indicadorDatosCapturar"))
        assertEquals("12345678", result.value("numeroSecuencial"))
        assertEquals("9", result.value("digitoControlJustificante"))
    }

    @Test
    fun `tipo 523 spec has correct length 22`() {
        val spec = FormatRegistry.getSpec("523")!!
        assertEquals(22, spec.expectedLength)
        assertEquals(8, spec.fields.size)
    }

    // ─── Tipo 522 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 522 parses sample payload correctly`() {
        // 3+2+3+1+10+2+2+3+2+1+3+8+1+3+8 = 52 chars
        val payload =
            "522" + "28" + "123" + "5" +
                "1234567890" + "12" + "34" +
                "456" + "24" + "3" + "045" +
                "00123456" +
                "4" + "078" +
                "00067890"
        assertEquals(52, payload.length)
        val result = parse(payload)
        assertEquals("522", result.tipo)
        assertEquals("28", result.value("provincia"))
        assertEquals("12", result.value("digitosControlSinRecargo"))
        assertEquals("34", result.value("digitosControlConRecargo"))
        assertEquals("456", result.value("tributo"))
        assertEquals("24", result.value("ejercicioDevengo"))
        assertEquals("3", result.value("anioFechaLimiteSinRecargo"))
        assertEquals("045", result.value("fechaJulianaLimiteSinRecargo"))
        assertEquals("00123456", result.value("importeSinRecargo"))
        assertEquals("4", result.value("anioFechaLimiteConRecargo"))
        assertEquals("078", result.value("fechaJulianaLimiteConRecargo"))
        assertEquals("00067890", result.value("importeConRecargo"))
    }

    @Test
    fun `tipo 522 spec has correct length 52`() {
        val spec = FormatRegistry.getSpec("522")!!
        assertEquals(52, spec.expectedLength)
        assertEquals(15, spec.fields.size)
    }

    // ─── Tipo 521 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 521 parses sample payload correctly`() {
        // 3+2+3+1+10+2+1+3+2+1+3+8+1 = 40 chars
        val payload =
            "521" + "28" + "123" + "5" +
                "1234567890" + "12" +
                "1" + "456" + "24" + "3" + "045" +
                "00123456" + "0"
        assertEquals(40, payload.length)
        val result = parse(payload)
        assertEquals("521", result.tipo)
        assertEquals("28", result.value("provincia"))
        assertEquals("123", result.value("municipio"))
        assertEquals("5", result.value("digitoControlEmisora"))
        assertEquals("1234567890", result.value("identificacionDocumento"))
        assertEquals("12", result.value("digitosControlReferencia"))
        assertEquals("1", result.value("discriminantePeriodo"))
        assertEquals("456", result.value("tributo"))
        assertEquals("24", result.value("ejercicioDevengo"))
        assertEquals("3", result.value("anioFechaLimite"))
        assertEquals("045", result.value("fechaJulianaLimite"))
        assertEquals("00123456", result.value("importe"))
        assertEquals("0", result.value("digitoParidad"))
    }

    @Test
    fun `tipo 521 spec has correct length 40`() {
        val spec = FormatRegistry.getSpec("521")!!
        assertEquals(40, spec.expectedLength)
        assertEquals(13, spec.fields.size)
    }

    // ─── Tipo 508 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 508 parses sample payload correctly`() {
        // 3+4+6+2+3+1+10+2+3+2+2+8 = 46 chars
        val payload =
            "508" + "0051" + "260128" +
                "28" + "123" + "5" +
                "1234567890" + "12" +
                "456" + "24" + "03" +
                "00123456"
        assertEquals(46, payload.length)
        val result = parse(payload)
        assertEquals("508", result.tipo)
        assertEquals("0051", result.value("entidadTesorera"))
        assertEquals("260128", result.value("fechaLimitePago"))
        assertEquals("28", result.value("provincia"))
        assertEquals("123", result.value("municipio"))
        assertEquals("5", result.value("digitoControlEmisora"))
        assertEquals("1234567890", result.value("identificacionDocumento"))
        assertEquals("12", result.value("digitosControlReferencia"))
        assertEquals("456", result.value("tributo"))
        assertEquals("24", result.value("ejercicio"))
        assertEquals("03", result.value("remesa"))
        assertEquals("00123456", result.value("importe"))
    }

    @Test
    fun `tipo 508 spec has correct length 46`() {
        val spec = FormatRegistry.getSpec("508")!!
        assertEquals(46, spec.expectedLength)
        assertEquals(12, spec.fields.size)
    }

    // ─── Tipo 503 — spec confirmada (idéntica a 501) ───────────────────────────

    @Test
    fun `tipo 503 spec has same structure as 501`() {
        val spec501 = FormatRegistry.getSpec("501")!!
        val spec503 = FormatRegistry.getSpec("503")!!
        assertEquals(spec501.expectedLength, spec503.expectedLength)
        assertEquals(spec501.tipoFieldStart, spec503.tipoFieldStart)
        assertEquals(spec501.fields.map { it.name }, spec503.fields.map { it.name })
    }

    @Test
    fun `tipo 503 parses sample payload correctly`() {
        val payload = "503" + "28" + "123" + "0" + "1" + "50" + "1234567" + "89" + "012345000" + "00"
        assertEquals(32, payload.length)
        val result = parse(payload)
        assertEquals("503", result.tipo)
        assertEquals("28", result.value("provincia"))
        assertEquals("503", result.value("tipo"))
        assertEquals("1234567", result.value("notificacion"))
    }

    // ─── Tipo 502 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 502 parses sample payload correctly`() {
        // payload 36 chars: tipo(3)+provincia(2)+municipio(3)+dcEmisora(1)
        //                   +idDoc(10)+dcRef(2)+tributo(3)+ejercicio(2)+remesa(2)+importe(8)
        val payload =
            "502" + "28" + "123" + "5" +
                "1234567890" + "12" +
                "456" + "24" + "03" +
                "00123456"
        assertEquals(36, payload.length)
        val result = parse(payload)
        assertEquals("502", result.tipo)
        assertEquals("28", result.value("provincia"))
        assertEquals("123", result.value("municipio"))
        assertEquals("5", result.value("digitoControlEmisora"))
        assertEquals("1234567890", result.value("identificacionDocumento"))
        assertEquals("12", result.value("digitosControlReferencia"))
        assertEquals("456", result.value("tributo"))
        assertEquals("24", result.value("ejercicio"))
        assertEquals("03", result.value("remesa"))
        assertEquals("00123456", result.value("importe"))
    }

    @Test
    fun `tipo 502 spec has correct length 36`() {
        val spec = FormatRegistry.getSpec("502")!!
        assertEquals(36, spec.expectedLength)
        assertEquals(10, spec.fields.size)
    }

    // ─── Tipo 550 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 550 spec has correct length 58`() {
        assertEquals(58, FormatRegistry.getSpec("550")!!.expectedLength)
    }

    @Test
    fun `tipo 550 parses sample payload correctly`() {
        // 3+11+4+2+13+25 = 58 chars (periodo alfanumérico)
        val payload =
            "550" + "00000067890" + "2024" + "1T" + "2024000000001" +
                "1234567890123456789012345"
        assertEquals(58, payload.length)
        val result = parse(payload)
        assertEquals("550", result.tipo)
        assertEquals("00000067890", result.value("importe"))
        assertEquals("2024", result.value("ejercicio"))
        assertEquals("1T", result.value("periodo"))
        assertEquals("2024000000001", result.value("justificante"))
        assertEquals("1234567890123456789012345", result.value("datosAdicionales"))
    }

    // ─── Tipo 011 — spec confirmada (AI "21") ──────────────────────────────────

    @Test
    fun `tipo 011 parses payload with AI 21 prefix`() {
        // 3+3+1+8+1 = 16 chars payload — AI is "21" not "90"
        val payload = "011" + "111" + "3" + "12345678" + "9"
        assertEquals(16, payload.length)
        val withAi = parse("(21)$payload")
        assertEquals("011", withAi.tipo)
        assertEquals("111", withAi.value("modelo"))
        assertEquals("3", withAi.value("añoImpresion"))
        assertEquals("12345678", withAi.value("numeroSerie"))
        assertEquals("9", withAi.value("digitoControl"))
    }

    @Test
    fun `tipo 011 parses plain payload without prefix`() {
        val payload = "011" + "111" + "3" + "12345678" + "9"
        assertEquals("011", parse(payload).tipo)
    }

    @Test
    fun `stripGS1Prefix handles AI 21`() {
        val payload = "011" + "111" + "3" + "12345678" + "9"
        assertEquals(payload, Notebook64Parser.stripGS1Prefix("(21)$payload"))
    }

    // ─── Tipo 010 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 010 parses sample payload correctly`() {
        // 3+5+9+4 = 21 chars (NIF y anagrama alfanuméricos)
        val payload = "010" + "28001" + "12345678A" + "JLGR"
        assertEquals(21, payload.length)
        val result = parse(payload)
        assertEquals("010", result.tipo)
        assertEquals("28001", result.value("codigoAdministracion"))
        assertEquals("12345678A", result.value("nif"))
        assertEquals("JLGR", result.value("anagrama"))
    }

    @Test
    fun `tipo 010 jurídica has spaces in anagrama`() {
        val payload = "010" + "28001" + "A12345678" + "    "
        assertEquals(21, payload.length)
        assertEquals("    ", parse(payload).value("anagrama"))
    }

    // ─── Tipo 539 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 539 parses sample payload correctly`() {
        // 3+5+13+12+3+2+9+2+4 = 53 chars
        val payload = "539" + "72000" + "2024000123456" + "000000067890" + "600" + "24" + "12345678A" + "4T" + "JLGR"
        assertEquals(53, payload.length)
        val result = parse(payload)
        assertEquals("539", result.tipo)
        assertEquals("72000", result.value("codigoOrganismo"))
        assertEquals("2024000123456", result.value("numeroJustificante"))
        assertEquals("000000067890", result.value("importe"))
        assertEquals("600", result.value("modelo"))
        assertEquals("24", result.value("ejercicio"))
        assertEquals("12345678A", result.value("nif"))
        assertEquals("4T", result.value("periodo"))
        assertEquals("JLGR", result.value("anagrama"))
    }

    // ─── Tipo 564 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 564 parses sample payload correctly`() {
        // 3+13+6+4+8+9 = 43 chars
        val payload = "564" + "2024000123456" + "460001" + "0001" + "00123456" + "12345678A"
        assertEquals(43, payload.length)
        val result = parse(payload)
        assertEquals("564", result.tipo)
        assertEquals("2024000123456", result.value("numeroJustificante"))
        assertEquals("460001", result.value("codigoTerritorial"))
        assertEquals("0001", result.value("concepto"))
        assertEquals("00123456", result.value("importe"))
        assertEquals("12345678A", result.value("nif"))
    }

    // ─── Tipo 559 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 559 parses sample payload correctly`() {
        // 3+11+4+2+25+3 = 48 chars
        val payload = "559" + "00000067890" + "2024" + "1T" + "6001234567890123456789012" + "600"
        assertEquals(48, payload.length)
        val result = parse(payload)
        assertEquals("559", result.tipo)
        assertEquals("00000067890", result.value("importe"))
        assertEquals("2024", result.value("ejercicio"))
        assertEquals("1T", result.value("periodo"))
        assertEquals("6001234567890123456789012", result.value("datosAdicionales"))
        assertEquals("600", result.value("modelo"))
    }

    // ─── Tipo 551 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 551 parses sample payload correctly`() {
        // 3+17+12+9+4+2+3 = 50 chars (justificante y NIF alfanuméricos)
        val payload = "551" + "2024000123456    " + "000000067890" + "12345678A" + "2024" + "00" + "600"
        assertEquals(50, payload.length)
        val result = parse(payload)
        assertEquals("551", result.tipo)
        assertEquals("2024000123456    ", result.value("numeroJustificante"))
        assertEquals("000000067890", result.value("importe"))
        assertEquals("12345678A", result.value("nif"))
        assertEquals("2024", result.value("ejercicio"))
        assertEquals("00", result.value("periodo"))
        assertEquals("600", result.value("modelo"))
    }

    @Test
    fun `tipo 551 spec has correct length 50`() {
        assertEquals(50, FormatRegistry.getSpec("551")!!.expectedLength)
    }

    // ─── Tipo 573 — spec confirmada (idéntica a 572) ───────────────────────────

    @Test
    fun `tipo 573 has same structure as 572`() {
        val spec572 = FormatRegistry.getSpec("572")!!
        val spec573 = FormatRegistry.getSpec("573")!!
        assertEquals(spec572.expectedLength, spec573.expectedLength)
        assertEquals(spec572.fields.map { it.name }, spec573.fields.map { it.name })
    }

    @Test
    fun `tipo 573 parses correctly`() {
        val payload = "573" + "0042" + "123456" + "007" + "12345678" + "9"
        assertEquals(25, payload.length)
        assertEquals("573", parse(payload).tipo)
    }

    // ─── Tipo 572 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 572 parses sample payload correctly`() {
        // 3+4+6+3+8+1 = 25 chars
        val payload = "572" + "0042" + "123456" + "007" + "12345678" + "9"
        assertEquals(25, payload.length)
        val result = parse(payload)
        assertEquals("572", result.tipo)
        assertEquals("0042", result.value("numeroJuego"))
        assertEquals("123456", result.value("numeroLibro"))
        assertEquals("007", result.value("numeroTicket"))
        assertEquals("12345678", result.value("numeroValidacion"))
        assertEquals("9", result.value("digitoControl"))
    }

    // ─── Tipo 571 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 571 parses sample payload correctly`() {
        // 3+2+2+9+3+2 = 21 chars (digitosControl alfanumérico)
        val payload = "571" + "45" + "12" + "202505061430".take(9) + "001" + "AB"
        assertEquals(21, payload.length)
        val result = parse(payload)
        assertEquals("571", result.tipo)
        assertEquals("45", result.value("numeroJuego"))
        assertEquals("12", result.value("ultimosDigitosIP"))
        assertEquals("001", result.value("secuencial"))
        assertEquals("AB", result.value("digitosControl"))
    }

    // ─── Tipo 591 — spec confirmada (idéntica a 589) ───────────────────────────

    @Test
    fun `tipo 591 has same length and field count as 589`() {
        val spec589 = FormatRegistry.getSpec("589")!!
        val spec591 = FormatRegistry.getSpec("591")!!
        assertEquals(spec589.expectedLength, spec591.expectedLength)
        assertEquals(spec589.fields.size, spec591.fields.size)
    }

    @Test
    fun `tipo 591 parses sample payload correctly`() {
        val payload = "591" + "1" + "12" + "45" + "045" + "12345678" + "901" + "0001"
        assertEquals(26, payload.length)
        assertEquals("591", parse(payload).tipo)
    }

    // ─── Tipo 590 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 590 parses sample payload correctly`() {
        // 3+1+2+2+6+5+3 = 22 chars
        val payload = "590" + "1" + "12" + "45" + "260128" + "12345" + "001"
        assertEquals(22, payload.length)
        val result = parse(payload)
        assertEquals("590", result.tipo)
        assertEquals("1", result.value("versionCodigo"))
        assertEquals("12", result.value("digitosControl"))
        assertEquals("45", result.value("identificadorProducto"))
        assertEquals("260128", result.value("fechaSorteo"))
        assertEquals("12345", result.value("numeroApuesta"))
        assertEquals("001", result.value("numeroSerie"))
    }

    // ─── Tipo 589 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 589 parses sample payload correctly`() {
        // 3+1+2+2+3+8+3+4 = 26 chars
        val payload = "589" + "1" + "12" + "45" + "045" + "12345678" + "901" + "0001"
        assertEquals(26, payload.length)
        val result = parse(payload)
        assertEquals("589", result.tipo)
        assertEquals("1", result.value("versionCodigo"))
        assertEquals("12", result.value("digitosControl"))
        assertEquals("45", result.value("identificadorProducto"))
        assertEquals("045", result.value("diaJulianoVenta"))
        assertEquals("12345678", result.value("numeroSerieApuesta"))
        assertEquals("901", result.value("digitosControlApuesta"))
        assertEquals("0001", result.value("contadorDiario"))
    }

    // ─── Tipo 588 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 588 parses sample payload correctly`() {
        // 3+8+7+2 = 20 chars
        val payload = "588" + "12345678" + "1234567" + "89"
        assertEquals(20, payload.length)
        val result = parse(payload)
        assertEquals("588", result.tipo)
        assertEquals("12345678", result.value("codigoVendedor"))
        assertEquals("1234567", result.value("numeroInternoONCE"))
        assertEquals("89", result.value("digitosControl"))
    }

    // ─── Tipo 587 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 587 parses sample payload correctly`() {
        // 3+8+4+4+1+2 = 22 chars
        val payload = "587" + "12345678" + "0051" + "1111" + "0" + "89"
        assertEquals(22, payload.length)
        val result = parse(payload)
        assertEquals("587", result.tipo)
        assertEquals("12345678", result.value("numeroInternoONCE"))
        assertEquals("0051", result.value("entidadFinanciera"))
        assertEquals("1111", result.value("codigoOficina"))
        assertEquals("0", result.value("digitoParidad"))
        assertEquals("89", result.value("digitosControl"))
    }

    // ─── Tipo 586 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 586 parses sample payload correctly`() {
        // 3+12+1 = 16 chars
        val payload = "586" + "123456789012" + "0"
        assertEquals(16, payload.length)
        val result = parse(payload)
        assertEquals("586", result.tipo)
        assertEquals("123456789012", result.value("codigoAdminLoteria"))
        assertEquals("0", result.value("digitoParidad"))
    }

    // ─── Tipo 531 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 531 parses sample payload correctly`() {
        // 3+8+1+3+2+2+3+2 = 24 chars
        val payload = "531" + "12345678" + "4" + "045" + "14" + "30" + "001" + "89"
        assertEquals(24, payload.length)
        val result = parse(payload)
        assertEquals("531", result.tipo)
        assertEquals("12345678", result.value("numeroUnicoVendedor"))
        assertEquals("4", result.value("año"))
        assertEquals("045", result.value("diaJuliano"))
        assertEquals("14", result.value("hora"))
        assertEquals("30", result.value("minutos"))
        assertEquals("001", result.value("numeroSecuencial"))
        assertEquals("89", result.value("digitosControl"))
    }

    // ─── Tipo 020 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 020 parses sample payload correctly`() {
        // 3+12+20+10+1 = 46 chars (todo alfanumérico)
        val payload = "020" + "ES0144580Y14" + "12345678901234567890" + "0000001000" + "1"
        assertEquals(46, payload.length)
        val result = parse(payload)
        assertEquals("020", result.tipo)
        assertEquals("ES0144580Y14", result.value("codigoISIN"))
        assertEquals("12345678901234567890", result.value("ccv"))
        assertEquals("0000001000", result.value("numeroAcciones"))
        assertEquals("1", result.value("tipoRespuesta"))
    }

    // ─── Tipo 504 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 504 parses sample payload correctly`() {
        // 3+5+4+7+2+7+2 = 30 chars
        val payload = "504" + "28001" + "1234" + "1234567" + "89" + "0067890" + "12"
        assertEquals(30, payload.length)
        val result = parse(payload)
        assertEquals("504", result.tipo)
        assertEquals("28001", result.value("emisora"))
        assertEquals("1234", result.value("tipoMatricula"))
        assertEquals("1234567", result.value("numeroNotificacion"))
        assertEquals("89", result.value("digitosControlClave"))
        assertEquals("0067890", result.value("importe"))
        assertEquals("12", result.value("digitosControlImporte"))
    }

    // ─── Tipo 529 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 529 parses sample payload correctly`() {
        // 3+4+6+8+3+13+6+8+1 = 52 chars
        val payload = "529" + "0051" + "260128" + "12345678" + "001" + "2024000123456" + "123456" + "00067890" + "0"
        assertEquals(52, payload.length)
        val result = parse(payload)
        assertEquals("529", result.tipo)
        assertEquals("0051", result.value("entidadTesorera"))
        assertEquals("260128", result.value("fechaLimitePago"))
        assertEquals("12345678", result.value("emisora"))
        assertEquals("001", result.value("sufijo"))
        assertEquals("2024000123456", result.value("referencia"))
        assertEquals("123456", result.value("identificacion"))
        assertEquals("00067890", result.value("importe"))
        assertEquals("0", result.value("digitoParidad"))
    }

    // ─── Tipo 528 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 528 parses sample payload correctly`() {
        // 3+8+3+13+6+10+6+1 = 50 chars
        val payload = "528" + "12345678" + "001" + "2024000123456" + "123456" + "0000001234" + "260128" + "0"
        assertEquals(50, payload.length)
        val result = parse(payload)
        assertEquals("528", result.tipo)
        assertEquals("12345678", result.value("emisora"))
        assertEquals("001", result.value("sufijo"))
        assertEquals("2024000123456", result.value("referencia"))
        assertEquals("123456", result.value("identificacion"))
        assertEquals("0000001234", result.value("importe"))
        assertEquals("260128", result.value("fechaLimitePago"))
        assertEquals("0", result.value("digitoParidad"))
    }

    // ─── Tipo 525 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 525 parses sample payload correctly`() {
        // 3+4+6+8+3+13+6+1 = 44 chars
        val payload = "525" + "0051" + "260128" + "12345678" + "001" + "2024000123456" + "123456" + "0"
        assertEquals(44, payload.length)
        val result = parse(payload)
        assertEquals("525", result.tipo)
        assertEquals("0051", result.value("entidadTesorera"))
        assertEquals("260128", result.value("fechaLimitePago"))
        assertEquals("12345678", result.value("emisora"))
        assertEquals("001", result.value("sufijo"))
        assertEquals("2024000123456", result.value("referencia"))
        assertEquals("123456", result.value("identificacion"))
        assertEquals("0", result.value("digitoParidad"))
    }

    // ─── Tipo 524 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 524 parses sample payload correctly`() {
        // 3+8+3+13+6+1 = 34 chars
        val payload = "524" + "12345678" + "001" + "2024000123456" + "123456" + "0"
        assertEquals(34, payload.length)
        val result = parse(payload)
        assertEquals("524", result.tipo)
        assertEquals("12345678", result.value("emisora"))
        assertEquals("001", result.value("sufijo"))
        assertEquals("2024000123456", result.value("referencia"))
        assertEquals("123456", result.value("identificacion"))
        assertEquals("0", result.value("digitoParidad"))
    }

    // ─── Tipo 515 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 515 parses sample payload correctly`() {
        // 3+4+6+8+3+13+1 = 38 chars
        val payload = "515" + "0051" + "260128" + "12345678" + "001" + "2024000123456" + "0"
        assertEquals(38, payload.length)
        val result = parse(payload)
        assertEquals("515", result.tipo)
        assertEquals("0051", result.value("entidadTesorera"))
        assertEquals("260128", result.value("fechaLimitePago"))
        assertEquals("12345678", result.value("emisora"))
        assertEquals("001", result.value("sufijo"))
        assertEquals("2024000123456", result.value("referencia"))
        assertEquals("0", result.value("digitoParidad"))
    }

    // ─── Tipo 514 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 514 parses sample payload correctly`() {
        // 3+8+3+13+1 = 28 chars
        val payload = "514" + "12345678" + "001" + "2024000123456" + "0"
        assertEquals(28, payload.length)
        val result = parse(payload)
        assertEquals("514", result.tipo)
        assertEquals("12345678", result.value("emisora"))
        assertEquals("001", result.value("sufijo"))
        assertEquals("2024000123456", result.value("referencia"))
        assertEquals("0", result.value("digitoParidad"))
        assertEquals(5, result.fields.size)
    }

    // ─── Tipo 507 — spec confirmada (idéntica a 581) ───────────────────────────

    @Test
    fun `tipo 507 has same structure as 581`() {
        val spec507 = FormatRegistry.getSpec("507")!!
        val spec581 = FormatRegistry.getSpec("581")!!
        assertEquals(spec507.expectedLength, spec581.expectedLength)
        assertEquals(spec507.fields.map { it.name }, spec581.fields.map { it.name })
    }

    @Test
    fun `tipo 507 parses sample payload correctly`() {
        val payload = "507" + "12345678" + "001" + "2024000123456" + "123456" + "0000001234" + "0"
        assertEquals(44, payload.length)
        val result = parse(payload)
        assertEquals("507", result.tipo)
        assertEquals("12345678", result.value("emisora"))
        assertEquals("001", result.value("sufijo"))
        assertEquals("2024000123456", result.value("referencia"))
        assertEquals("0", result.value("digitoParidad"))
    }

    // ─── Tipo 592 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 592 parses sample payload correctly`() {
        // 3+24+9+15+10 = 61 chars (IBAN, NIF y numDoc alfanuméricos)
        val payload = "592" + "ES8021000813610123456789" + "12345678A" + "2024URE000001  " + "0000067890"
        assertEquals(61, payload.length)
        val result = parse(payload)
        assertEquals("592", result.tipo)
        assertEquals("ES8021000813610123456789", result.value("cuentaRestringidaIBAN"))
        assertEquals("12345678A", result.value("nif"))
        assertEquals("2024URE000001  ", result.value("numeroDocumento"))
        assertEquals("0000067890", result.value("importe"))
    }

    // ─── Tipo 581 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 581 parses sample payload correctly`() {
        // 3+8+3+13+6+10+1 = 44 chars
        val payload = "581" + "12345678" + "001" + "2024000123456" + "123456" + "0000001234" + "0"
        assertEquals(44, payload.length)
        val result = parse(payload)
        assertEquals("581", result.tipo)
        assertEquals("12345678", result.value("emisora"))
        assertEquals("001", result.value("sufijo"))
        assertEquals("2024000123456", result.value("referencia"))
        assertEquals("123456", result.value("identificacion"))
        assertEquals("0000001234", result.value("importe"))
        assertEquals("0", result.value("digitoParidad"))
    }

    // ─── Tipo 558 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 558 parses sample payload correctly`() {
        // 3+1+2+24+12+10+2+2 = 56 chars
        val payload =
            "558" + "0" + "01" +
                "ES8021000813610123456789" +
                "000000001234" + "0000001234" +
                "24" + "0A"
        assertEquals(56, payload.length)
        val result = parse(payload)
        assertEquals("558", result.tipo)
        assertEquals("0", result.value("tipoDeclaracion"))
        assertEquals("01", result.value("tipoEspecifico"))
        assertEquals("ES8021000813610123456789", result.value("iban"))
        assertEquals("000000001234", result.value("importeIngresar"))
        assertEquals("0000001234", result.value("importeTotal"))
        assertEquals("24", result.value("ejercicio"))
        assertEquals("0A", result.value("periodo"))
    }

    @Test
    fun `tipo 558 spec has correct length 56`() {
        assertEquals(56, FormatRegistry.getSpec("558")!!.expectedLength)
    }

    // ─── Tipo 533 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 533 parses sample payload correctly`() {
        // 3+12+3+3+6+1+9+1 = 38 chars
        val payload = "533" + "000000001234" + "840" + "053" + "123456" + "7" + "12345678A" + "0"
        assertEquals(38, payload.length)
        val result = parse(payload)
        assertEquals("533", result.tipo)
        assertEquals("000000001234", result.value("importe"))
        assertEquals("840", result.value("modelo"))
        assertEquals("053", result.value("codigoTasa"))
        assertEquals("123456", result.value("numeroSerie"))
        assertEquals("7", result.value("digitoControlJustificante"))
        assertEquals("12345678A", result.value("nif"))
        assertEquals("0", result.value("digitoParidad"))
    }

    // ─── Tipo 530 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 530 parses sample payload correctly`() {
        // 3+13+12+3+2+9+2+4 = 48 chars
        val payload = "530" + "2024000123456" + "000000067890" + "100" + "24" + "12345678A" + "0A" + "JLGR"
        assertEquals(48, payload.length)
        val result = parse(payload)
        assertEquals("530", result.tipo)
        assertEquals("2024000123456", result.value("numeroJustificante"))
        assertEquals("000000067890", result.value("importe"))
        assertEquals("100", result.value("modelo"))
        assertEquals("24", result.value("ejercicio"))
        assertEquals("12345678A", result.value("nif"))
        assertEquals("0A", result.value("periodo"))
        assertEquals("JLGR", result.value("anagrama"))
    }

    // ─── Tipo 512 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 512 parses sample payload correctly`() {
        // 3+3+14+2+2+8+1+9 = 42 chars
        val payload = "512" + "111" + "00000000012345" + "28" + "24" + "12345678" + "9" + "12345678A"
        assertEquals(42, payload.length)
        val result = parse(payload)
        assertEquals("512", result.tipo)
        assertEquals("111", result.value("modelo"))
        assertEquals("00000000012345", result.value("importe"))
        assertEquals("28", result.value("delegacion"))
        assertEquals("24", result.value("añoImpresion"))
        assertEquals("12345678", result.value("numeroSerie"))
        assertEquals("9", result.value("digitoControl"))
        assertEquals("12345678A", result.value("nif"))
    }

    @Test
    fun `tipo 512 spec has correct length 42`() {
        assertEquals(42, FormatRegistry.getSpec("512")!!.expectedLength)
    }

    // ─── Tipo 532 — spec confirmada ────────────────────────────────────────────

    @Test
    fun `tipo 532 spec has correct length 61`() {
        assertEquals(61, FormatRegistry.getSpec("532")!!.expectedLength)
    }

    @Test
    fun `tipo 532 spec contains tipoDeclaracion tipoEspecifico iban fields`() {
        val names = FormatRegistry.getSpec("532")!!.fields.map { it.name }
        assertTrue(names.contains("tipoDeclaracion"))
        assertTrue(names.contains("tipoEspecifico"))
        assertTrue(names.contains("iban"))
    }

    @Test
    fun `tipo 532 parses sample payload correctly`() {
        // 3+1+2+24+12+12+3+2+2 = 61 chars
        val payload =
            "532" + "0" + "01" +
                "ES8021000813610123456789" +
                "000000001234" + "000000012345" +
                "100" + "24" + "0A"
        assertEquals(61, payload.length)
        val result = parse(payload)
        assertEquals("532", result.tipo)
        assertEquals("0", result.value("tipoDeclaracion"))
        assertEquals("01", result.value("tipoEspecifico"))
        assertEquals("ES8021000813610123456789", result.value("iban"))
        assertEquals("000000001234", result.value("importeIngresar"))
        assertEquals("000000012345", result.value("importeTotal"))
        assertEquals("100", result.value("modelo"))
        assertEquals("24", result.value("ejercicio"))
        assertEquals("0A", result.value("periodo"))
    }

    // ─── Notebook64Result helpers ──────────────────────────────────────────────

    @Test
    fun `result field helper returns null for missing field`() {
        val payload = "501" + "28" + "123" + "0" + "1" + "50" + "1234567" + "89" + "012345000" + "00"
        val result = parse(payload)
        assertNull(result.field("nonExistent"))
        assertNull(result.value("nonExistent"))
    }

    @Test
    fun `rawInput preserved in result`() {
        val payload = "501" + "28" + "123" + "0" + "1" + "50" + "1234567" + "89" + "012345000" + "00"
        val result = parse(payload)
        assertEquals(payload, result.rawInput)
    }
}
