package com.kelompok4.smartmaney.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.kelompok4.smartmaney.R
import com.kelompok4.smartmaney.data.local.preferences.CurrencyOption
import com.kelompok4.smartmaney.data.local.preferences.ThemeMode
import com.kelompok4.smartmaney.ui.theme.SmartManeyTheme
import com.kelompok4.smartmaney.viewmodel.ExportEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState,
    exportEvents: Flow<ExportEvent> = emptyFlow(),
    onBackClick: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onCurrencySelected: (CurrencyOption) -> Unit = {},
    onDeleteAccountRequest: () -> Unit = {},
    onDeleteAccountConfirm: () -> Unit = {},
    onDeleteAccountCancel: () -> Unit = {},
    onDeleteAccountResultAcknowledged: () -> Unit = {},
    onExportCsvClick: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteState = uiState.deleteState
    val context = LocalContext.current

    val reauthMessage = stringResource(R.string.settings_delete_requires_reauth)
    LaunchedEffect(deleteState) {
        when (deleteState) {
            is DeleteAccountState.RequiresReauth -> {
                snackbarHostState.showSnackbar(reauthMessage)
                onDeleteAccountResultAcknowledged()
            }
            is DeleteAccountState.Error -> {
                snackbarHostState.showSnackbar(deleteState.message)
                onDeleteAccountResultAcknowledged()
            }
            else -> Unit
        }
    }

    val shareTitle = stringResource(R.string.settings_export_share_title)
    val emptyMessage = stringResource(R.string.settings_export_empty)
    val noShareAppMessage = stringResource(R.string.settings_export_no_share_app)
    val failedTemplate = stringResource(R.string.settings_export_failed)
    LaunchedEffect(exportEvents) {
        exportEvents.collect { event ->
            when (event) {
                is ExportEvent.Ready -> {
                    val result = runCatching {
                        withContext(Dispatchers.IO) {
                            val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
                            val file = File(exportsDir, event.filename)
                            file.writeText(event.content)
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                        }
                    }
                    result.onSuccess { uri ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, event.filename)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        val chooser = Intent.createChooser(shareIntent, shareTitle).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(chooser)
                        } catch (_: ActivityNotFoundException) {
                            snackbarHostState.showSnackbar(noShareAppMessage)
                        }
                    }.onFailure { error ->
                        snackbarHostState.showSnackbar(
                            failedTemplate.format(error.message ?: "unknown error")
                        )
                    }
                }
                ExportEvent.Empty -> snackbarHostState.showSnackbar(emptyMessage)
                is ExportEvent.Failed -> snackbarHostState.showSnackbar(
                    failedTemplate.format(event.message)
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ThemeSection(
                    selected = uiState.themeMode,
                    onSelected = onThemeModeSelected
                )
            }
            item {
                CurrencySection(
                    selected = uiState.currency,
                    onSelected = onCurrencySelected
                )
            }
            item {
                DataSection(
                    isExporting = uiState.isExporting,
                    onExportCsvClick = onExportCsvClick
                )
            }
            item {
                AccountSection(
                    isDeleting = deleteState == DeleteAccountState.InProgress,
                    onDeleteClick = onDeleteAccountRequest
                )
            }
        }
    }

    if (deleteState == DeleteAccountState.Confirming) {
        DeleteAccountDialog(
            onConfirm = onDeleteAccountConfirm,
            onDismiss = onDeleteAccountCancel
        )
    }
}

@Composable
private fun ThemeSection(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(stringResource(R.string.settings_section_appearance))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.selectableGroup()) {
                ThemeMode.entries.forEach { mode ->
                    ThemeOptionRow(
                        mode = mode,
                        selected = mode == selected,
                        onSelected = { onSelected(mode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    mode: ThemeMode,
    selected: Boolean,
    onSelected: () -> Unit
) {
    val labelRes = when (mode) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelected,
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun CurrencySection(
    selected: CurrencyOption,
    onSelected: (CurrencyOption) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(stringResource(R.string.settings_section_currency))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.selectableGroup()) {
                CurrencyOption.entries.forEach { option ->
                    CurrencyOptionRow(
                        option = option,
                        selected = option == selected,
                        onSelected = { onSelected(option) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrencyOptionRow(
    option: CurrencyOption,
    selected: Boolean,
    onSelected: () -> Unit
) {
    val labelRes = when (option) {
        CurrencyOption.IDR -> R.string.settings_currency_idr
        CurrencyOption.USD -> R.string.settings_currency_usd
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelected,
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun DataSection(
    isExporting: Boolean,
    onExportCsvClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(stringResource(R.string.settings_section_data))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_export_csv),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                supportingContent = {
                    Text(
                        text = if (isExporting) {
                            stringResource(R.string.settings_export_in_progress)
                        } else {
                            stringResource(R.string.settings_export_csv_subtitle)
                        }
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = {
                    if (isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = false,
                        enabled = !isExporting,
                        onClick = onExportCsvClick
                    )
            )
        }
    }
}

@Composable
private fun AccountSection(
    isDeleting: Boolean,
    onDeleteClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(stringResource(R.string.settings_section_account))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_delete_account),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                supportingContent = {
                    Text(
                        text = if (isDeleting) {
                            stringResource(R.string.settings_delete_in_progress)
                        } else {
                            stringResource(R.string.settings_delete_account_subtitle)
                        }
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                trailingContent = {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = false,
                        enabled = !isDeleting,
                        onClick = onDeleteClick
                    )
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun DeleteAccountDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_delete_dialog_title)) },
        text = { Text(stringResource(R.string.settings_delete_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.settings_delete_dialog_confirm),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_delete_dialog_cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewSettingsScreen() {
    SmartManeyTheme {
        SettingsScreen(
            uiState = SettingsUiState(themeMode = ThemeMode.SYSTEM),
            onBackClick = {},
            onThemeModeSelected = {}
        )
    }
}
