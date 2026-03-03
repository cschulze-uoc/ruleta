package com.apktados.ruleta.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apktados.ruleta.data.Partida
import com.apktados.ruleta.data.PartidasRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
public fun HomeScreen(
    onNuevaPartida: (String) -> Unit,
    onHistorial: () -> Unit
) {
    var jugador by remember { mutableStateOf("Carlos") }

    val context = LocalContext.current
    val repo = remember { PartidasRepository(context) }

    var top3 by remember { mutableStateOf<List<Partida>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        top3 = withContext(Dispatchers.IO) {
            repo.top10().take(3)
        }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎰 Ruleta", style = MaterialTheme.typography.headlineLarge)

        OutlinedTextField(
            value = jugador,
            onValueChange = { jugador = it },
            label = { Text("Nombre del jugador") },
            singleLine = true
        )

        Button(
            onClick = { onNuevaPartida(jugador) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nueva partida")
        }

        OutlinedButton(
            onClick = onHistorial,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Historial")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("🏆 Mejores puntuaciones")

        when {
            loading -> Text("Cargando...")
            top3.isEmpty() -> Text("Aún no hay partidas.")
            else -> {
                top3.forEachIndexed { index, partida ->
                    Text("${index + 1}. ${partida.jugador} - ${partida.monedasFinales} monedas")
                }
            }
        }
    }
}