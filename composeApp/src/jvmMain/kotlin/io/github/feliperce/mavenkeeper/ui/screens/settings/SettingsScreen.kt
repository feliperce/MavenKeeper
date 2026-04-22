package io.github.feliperce.mavenkeeper.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mavenkeeper.composeapp.generated.resources.Res
import mavenkeeper.composeapp.generated.resources.settings_about_description
import mavenkeeper.composeapp.generated.resources.settings_about_section
import mavenkeeper.composeapp.generated.resources.settings_about_version
import mavenkeeper.composeapp.generated.resources.settings_apply
import mavenkeeper.composeapp.generated.resources.settings_currently_resolved
import mavenkeeper.composeapp.generated.resources.settings_custom_path_label
import mavenkeeper.composeapp.generated.resources.settings_custom_path_placeholder
import mavenkeeper.composeapp.generated.resources.settings_invalid_path
import mavenkeeper.composeapp.generated.resources.settings_repo_path_section
import mavenkeeper.composeapp.generated.resources.settings_restore_default
import mavenkeeper.composeapp.generated.resources.settings_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text(stringResource(Res.string.settings_title)) })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.settings_repo_path_section),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(Res.string.settings_currently_resolved, state.resolvedPath),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = state.draftPath,
                        onValueChange = viewModel::onPathChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.settings_custom_path_label)) },
                        placeholder = { Text(stringResource(Res.string.settings_custom_path_placeholder)) },
                        isError = state.validationError != null,
                        supportingText = state.validationError?.let { err ->
                            {
                                Text(
                                    text = err.resolve(),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                        singleLine = true,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = viewModel::onApplyClick) {
                            Text(stringResource(Res.string.settings_apply))
                        }
                        OutlinedButton(onClick = viewModel::onRestoreDefaultClick) {
                            Text(stringResource(Res.string.settings_restore_default))
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.settings_about_section),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(Res.string.settings_about_version),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(Res.string.settings_about_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsValidationError.resolve(): String = when (this) {
    SettingsValidationError.INVALID_PATH -> stringResource(Res.string.settings_invalid_path)
}
