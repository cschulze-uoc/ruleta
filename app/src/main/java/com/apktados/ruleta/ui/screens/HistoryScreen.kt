package com.apktados.ruleta.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.apktados.ruleta.R
import com.apktados.ruleta.data.Partida
import com.apktados.ruleta.data.PartidasRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val repo = remember { PartidasRepository(context) }

    var items by remember { mutableStateOf<List<Partida>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val gold = Color(0xFFFFD700)
    val darkOverlay = Color(0xCC111111)
    val buttonRed = Color(0xFFC00000)

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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.home_background),
            contentDescription = "Fondo casino",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .background(
                    color = darkOverlay,
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 2.dp,
                    color = gold.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HISTORIAL",
                    style = MaterialTheme.typography.headlineLarge,
                    color = gold,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonRed,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(2.dp, gold)
                ) {
                    Text(
                        text = "Volver",
                        fontWeight = FontWeight.Bold
                    )
                }

                when {
                    loading -> {
                        Text(
                            text = "Cargando historial...",
                            color = Color.White
                        )
                    }

                    error != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0x66000000),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Error: $error",
                                color = Color.Red
                            )
                        }
                    }

                    items.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0x66000000),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Aún no hay partidas guardadas.",
                                color = Color.White
                            )
                        }
                    }

                    else -> {
                        Text(
                            text = "Partidas guardadas: ${items.size}",
                            color = gold,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(items) { partida ->
                                PartidaRow(
                                    p = partida,
                                    gold = gold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PartidaRow(
    p: Partida,
    gold: Color
) {
    val sdf = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xEE1A1A1A))
                .border(
                    width = 1.dp,
                    color = gold.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Jugador: ${p.jugador}",
                    style = MaterialTheme.typography.titleMedium,
                    color = gold,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Monedas finales: ${p.monedasFinales}",
                    color = Color.White
                )

                Text(
                    text = "Fecha: ${sdf.format(Date(p.fecha))}",
                    color = Color.LightGray
                )

                if (p.latitud != null && p.longitud != null) {
                    Text(
                        text = "Ubicación: ${"%.5f".format(p.latitud)}, ${"%.5f".format(p.longitud)}",
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}