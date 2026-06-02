# SpainNotebook64

[![Maven Central](https://img.shields.io/maven-central/v/io.github.joyner-perez/SpainNotebook64?color=blue&label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.joyner-perez/SpainNotebook64)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)

Kotlin library to read and decode the barcodes printed on Spanish financial documents — the
**CECA/AEB Cuaderno 64** standard (June 2016). Given a barcode payload string it auto-detects the
document format (*tipo*) and extracts every field with its name, value, position and description.

Pure Kotlin/JVM, zero runtime dependencies.

## Installation

Gradle (Kotlin DSL):

```kotlin
dependencies {
    implementation("io.github.joyner-perez:SpainNotebook64:1.0.0")
}
```

Gradle (Groovy):

```groovy
implementation 'io.github.joyner-perez:SpainNotebook64:1.0.0'
```

Version catalog (`gradle/libs.versions.toml`):

```toml
[versions]
spainNotebook64 = "1.0.0"

[libraries]
spain-notebook64 = { module = "io.github.joyner-perez:SpainNotebook64", version.ref = "spainNotebook64" }
```

Then reference it in `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.spain.notebook64)
}
```

Maven:

```xml
<dependency>
    <groupId>io.github.joyner-perez</groupId>
    <artifactId>SpainNotebook64</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

The whole public API is two top-level functions in the `com.joyner.notebook64` package.

### Parse a barcode

```kotlin
import com.joyner.notebook64.parse

// Scanned barcode payload (tipo 534). The GS1-128 AI(90) prefix is optional.
val result = parse("5342024000123456    0012345612345678AJLGR280001")

println(result.tipo)                       // "534"
println(result.value("numeroJustificante"))// "2024000123456"
println(result.value("importe"))           // "00123456"
println(result.value("nif"))               // "12345678A"
```

The GS1-128 Application Identifier prefix is accepted in both forms and stripped automatically:

```kotlin
parse("(90)5342024000123456    0012345612345678AJLGR280001") // parenthesised AI
parse("905342024000123456    0012345612345678AJLGR280001")   // bare AI
parse("5342024000123456    0012345612345678AJLGR280001")     // no AI
```

### Force a specific format

By default the *tipo* is auto-detected from the embedded indicator field. Pass it explicitly to
skip detection (and to validate the input against that format's expected length):

```kotlin
val result = parse(input = "50128123015...", tipo = "501")
```

### List supported formats

```kotlin
import com.joyner.notebook64.availableFormats

availableFormats().forEach { format ->
    println("${format.tipo} — ${format.description}")
}
// 501 — Tributos Administración Local
// 502 — Tributos y otros ingresos municipales - formato corto modalidad 1
// ...
```

## Interpreting the result

`parse(...)` returns a `Notebook64Result`:

| Property   | Type                | Description                                                          |
|------------|---------------------|----------------------------------------------------------------------|
| `tipo`     | `String`            | Detected (or forced) 3-digit format code, e.g. `"534"`.              |
| `rawInput` | `String`            | The payload that was parsed (after stripping any GS1-128 AI prefix). |
| `fields`   | `List<ParsedField>` | Every field extracted from the payload, in barcode order.            |

Each `ParsedField` describes one segment of the barcode:

| Property      | Type     | Description                                                            |
|---------------|----------|------------------------------------------------------------------------|
| `name`        | `String` | Field identifier, e.g. `"importe"`, `"nif"`, `"numeroJustificante"`.   |
| `value`       | `String` | Raw characters of the field exactly as they appear in the barcode.     |
| `startPos`    | `Int`    | 1-indexed start position within the **full** barcode (AI prefix included). |
| `length`      | `Int`    | Number of characters in the field.                                     |
| `description` | `String` | Human-readable description of the field (in Spanish).                  |

### Convenience accessors

```kotlin
val result = parse("5342024000123456    0012345612345678AJLGR280001")

// Look up the whole field by name (null if absent):
val importe: ParsedField? = result.field("importe")

// Or just the value (null if absent):
val nif: String? = result.value("nif")

// Or iterate everything:
result.fields.forEach { f ->
    println("${f.name} = '${f.value}'  (pos ${f.startPos}, len ${f.length}) — ${f.description}")
}
```

### Reading values correctly

- **Values are raw strings, never converted.** Leading zeros, padding spaces and check digits are
  preserved as printed. Convert yourself when needed.
- **Amounts (`importe`)** are integers in céntimos with two implicit decimals. Divide by 100 to get
  euros — e.g. `"00123456"` → `1234.56 €`.
- **Alphanumeric fields** (NIF, anagrama, concepto…) may be space-padded to a fixed width; trim if
  your use case requires it.
- **`applicationIdentifier`** — when the input carries a GS1-128 AI prefix, the first field is the
  AI itself (`"90"` or `"21"`); subsequent field `startPos` values account for that offset.

### Errors

`parse(...)` throws `IllegalArgumentException` when:

- an explicit `tipo` is not a registered format, or
- no *tipo* can be auto-detected from the input, or
- the input length does not match the expected length for the resolved format.

```kotlin
try {
    parse(rawScan)
} catch (e: IllegalArgumentException) {
    // e.g. "No se puede detectar el tipo. Longitud=30, primeros 3 chars='999'"
    println("Invalid barcode: ${e.message}")
}
```

## License

[MIT](https://opensource.org/licenses/MIT) © Joyner Pérez Echevarría
