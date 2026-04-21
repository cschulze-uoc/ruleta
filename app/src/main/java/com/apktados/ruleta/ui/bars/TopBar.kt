package com.apktados.ruleta.ui.bars

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.apktados.ruleta.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuletaTopBar(
    titulo: String,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateRanking: () -> Unit,
    onNavigateGame: () -> Unit,
    onNavigateHelp: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.fichas),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 8.dp),
                    tint = Color.Unspecified
                )
                Text(titulo)
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menú"
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Inicio") },
                        onClick = {
                            menuExpanded = false
                            onNavigateHome()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Ranking") },
                        onClick = {
                            menuExpanded = false
                            onNavigateRanking()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Juego") },
                        onClick = {
                            menuExpanded = false
                            onNavigateGame()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Ayuda") },
                        onClick = {
                            menuExpanded = false
                            onNavigateHelp()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            scrolledContainerColor = Color.Unspecified,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}