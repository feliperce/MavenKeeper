package io.github.feliperce.mavenkeeper.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.feliperce.mavenkeeper.ui.theme.MavenKeeperTheme
import mavenkeeper.composeapp.generated.resources.Res
import mavenkeeper.composeapp.generated.resources.library_search_placeholder
import mavenkeeper.composeapp.generated.resources.search_clear_cd
import mavenkeeper.composeapp.generated.resources.search_default_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(Res.string.search_default_placeholder),
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(Res.string.search_clear_cd),
                    )
                }
            }
        },
        singleLine = true,
    )
}

@Preview
@Composable
private fun SearchFieldPreview() {
    MavenKeeperTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SearchField(
                    query = "",
                    onQueryChange = {},
                    placeholder = stringResource(Res.string.library_search_placeholder),
                )
                SearchField(
                    query = "com.worldpackers",
                    onQueryChange = {},
                )
            }
        }
    }
}
