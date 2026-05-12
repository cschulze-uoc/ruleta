package com.apktados.ruleta.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apktados.ruleta.firebase.FirebaseRepository
import com.apktados.ruleta.firebase.OnlineScore

@Composable
fun OnlineRankingScreen(
    onVolver: () -> Unit
) {
    val scores = remember { mutableStateListOf<OnlineScore>() }
    val cargando = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }

    val background = Color(0xFF0E0E0E)
    val panel = Color(0xFF1C1C1C)
    val gold = Color(0xFFD4AF37)
    val green = Color(0xFF1E5F3A)

    LaunchedEffect(Unit) {
        FirebaseRepository.obtenerTop10(
            onOk = { lista ->
                scores.clear()
                scores.addAll(lista)
                cargando.value = false
            },
            onError = { mensaje ->
                error.value = mensaje
                cargando.value = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Top 10 Online",
                style = MaterialTheme.typography.headlineMedium,
                color = gold,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                cargando.value -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = gold)
                    }
                }

                error.value != null -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${error.value}",
                            color = Color.Red
                        )
                    }
                }

                scores.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay puntuaciones online todavía",
                            color = Color.White
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        itemsIndexed(scores) { index, score ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(panel, RoundedCornerShape(16.dp))
                                    .border(1.dp, gold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "${index + 1}. ${score.nombre}",
                                        color = gold,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Monedas: ${score.monedas}",
                                        color = Color.White
                                    )

                                    Text(
                                        text = "Fecha: ${score.fecha}",
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = green,
                    contentColor = Color.White
                )
            ) {
                Text("Volver")
            }
        }
    }
}