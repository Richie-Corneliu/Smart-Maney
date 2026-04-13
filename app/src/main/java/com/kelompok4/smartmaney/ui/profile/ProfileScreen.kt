package com.kelompok4.smartmaney.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kelompok4.smartmaney.R
import com.kelompok4.smartmaney.ui.theme.SmartManeyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onLogoutClick: () -> Unit = {}
) {
    var uiState by remember { mutableStateOf(ProfileUiState()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            stringResource(R.string.profile_section_account),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = uiState.fullName,
                            onValueChange = {
                                uiState = reduceProfileState(uiState, ProfileAction.UpdateFullName(it))
                            },
                            label = { Text(stringResource(R.string.profile_full_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = {
                                uiState = reduceProfileState(uiState, ProfileAction.UpdateEmail(it))
                            },
                            label = { Text(stringResource(R.string.profile_email)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            stringResource(R.string.profile_section_preferences),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        PreferenceRow(
                            title = stringResource(R.string.profile_enable_notifications),
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = {
                                uiState = reduceProfileState(uiState, ProfileAction.ToggleNotifications(it))
                            }
                        )
                        PreferenceRow(
                            title = stringResource(R.string.profile_dark_mode),
                            checked = uiState.darkModeEnabled,
                            onCheckedChange = {
                                uiState = reduceProfileState(uiState, ProfileAction.ToggleDarkMode(it))
                            }
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { uiState = reduceProfileState(uiState, ProfileAction.SaveChanges) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.profile_save_changes))
                }
            }

            uiState.status?.let { status ->
                val messageRes = when (status) {
                    ProfileStatus.Updated -> R.string.profile_status_updated
                    ProfileStatus.EmptyName -> R.string.profile_status_empty_name
                    ProfileStatus.InvalidEmail -> R.string.profile_status_invalid_email
                }
                item {
                    Text(
                        text = stringResource(messageRes),
                        color = if (status == ProfileStatus.Updated) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            item {
                TextButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.profile_logout))
                }
            }
        }
    }
}

@Composable
private fun PreferenceRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewProfileScreen() {
    SmartManeyTheme {
        ProfileScreen()
    }
}


