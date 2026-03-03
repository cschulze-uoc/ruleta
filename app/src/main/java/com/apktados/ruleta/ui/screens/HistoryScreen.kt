package com.apktados.ruleta.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.apktados.ruleta.data.Partida
import com.apktados.ruleta.data.PartidasRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {

    val context = LocalContext.current
    val repo = remember { PartidasRepository(context) }

    var items by remember { mutableStateOf<List<Partida>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        try {
            val data = withContext(Dispatchers.IO) {
                repo.historial()
            }
            items = data
        } catch (e: Exception) {
            error = e.message ?: "Error leyendo historial"
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Atrás")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            when {
                loading -> {
                    CircularProgressIndicator()
                    Text("Cargando…")
                }

                error != null -> {
                    Text("❌ $error")
                }

                items.isEmpty() -> {
                    Text("Aún no hay partidas guardadas.")
                }

                else -> {
                    Text("Partidas: ${items.size}")

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items) { p ->
                            PartidaRow(p)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PartidaRow(p: Partida) {

    val sdf = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    Card {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Jugador: ${p.jugador}", style = MaterialTheme.typography.titleMedium)
            Text("Monedas finales: ${p.monedasFinales}")
            Text("Fecha: ${sdf.format(Date(p.fecha))}")

            // (Producto 2) Mostrar ubicación si existe
            if (p.latitud != null && p.longitud != null) {
                Text("Ubicación: ${"%.5f".format(p.latitud)}, ${"%.5f".format(p.longitud)}")
            }
        }
    }
}