package com.joyner.notebook64

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.joyner.notebook64.model.FormatInfo
import com.joyner.notebook64.model.Notebook64Result
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanCustomCode
import io.github.g00fy2.quickie.config.BarcodeFormat
import io.github.g00fy2.quickie.config.ScannerConfig

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ParseScreen(modifier: Modifier = Modifier) {
    var inputText by rememberSaveable { mutableStateOf("") }
    var filterQuery by rememberSaveable { mutableStateOf("") }
    var selectedTipo by remember { mutableStateOf<FormatInfo?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var parseResult by remember { mutableStateOf<Notebook64Result?>(null) }
    var parseError by remember { mutableStateOf<String?>(null) }

    val allFormats = remember { availableFormats() }
    val filteredFormats = remember(filterQuery) {
        if (filterQuery.isBlank()) allFormats
        else allFormats.filter {
            it.tipo.contains(filterQuery, ignoreCase = true) ||
            it.description.contains(filterQuery, ignoreCase = true)
        }
    }

    val context = LocalContext.current
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val scanBarcodeLauncher = rememberLauncherForActivityResult(
        contract = ScanCustomCode()
    ) { result ->
        when (result) {
            is QRResult.QRSuccess -> {
                Log.d("ParseScreen", "QR scan result: ${result.content.rawValue}")
                inputText = result.content.rawValue ?: ""
            }
            is QRResult.QRUserCanceled -> Unit
            is QRResult.QRMissingPermission -> permissionState.launchPermissionRequest()
            is QRResult.QRError -> Toast.makeText(
                context,
                "Error leyendo código: ${result.exception.message ?: "Error desconocido"}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RectangleShape,
                onClick = {
                    if (permissionState.status.isGranted) {
                        scanBarcodeLauncher.launch(
                            ScannerConfig.build {
                                setBarcodeFormats(listOf(BarcodeFormat.FORMAT_CODE_128))
                                setOverlayStringRes(R.string.text_read_barcode)
                                setOverlayDrawableRes(R.drawable.ic_barcode)
                                setShowTorchToggle(true)
                                setShowCloseButton(true)
                                setKeepScreenOn(true)
                            }
                        )
                    } else {
                        permissionState.launchPermissionRequest()
                    }
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_barcode),
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Escanear código de barras")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Cuaderno 64 Parser", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Cadena del código de barras") },
                placeholder = { Text("(90)0051111111330053424083405001000006764") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = {
                        filterQuery = it
                        selectedTipo = null
                        dropdownExpanded = true
                    },
                    label = { Text("Tipo (opcional)") },
                    placeholder = { Text("Auto-detectar") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Auto-detectar") },
                        onClick = {
                            selectedTipo = null
                            filterQuery = ""
                            dropdownExpanded = false
                        }
                    )
                    HorizontalDivider()
                    filteredFormats.forEach { format ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(format.tipo, style = MaterialTheme.typography.labelLarge)
                                    Text(
                                        format.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                selectedTipo = format
                                filterQuery = format.tipo
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    parseError = null
                    parseResult = null
                    try {
                        parseResult = parse(inputText.trim(), selectedTipo?.tipo)
                    } catch (e: Exception) {
                        parseError = e.message ?: "Error desconocido"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = inputText.isNotBlank()
            ) {
                Text("Parsear")
            }

            parseError?.let { error ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            parseResult?.let { result ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Tipo detectado: ${result.tipo}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        result.fields.forEach { field ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    field.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "\"${field.value}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "p${field.startPos} l${field.length}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
