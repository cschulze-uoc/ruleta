package com.apktados.ruleta.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apktados.ruleta.R
import com.apktados.ruleta.data.Partida
import com.apktados.ruleta.data.PartidasRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

@Composable
fun HomeScreen(
    onNuevaPartida: (String) -> Unit,
    onHistorial: () -> Unit
) {
    var jugador by remember { mutableStateOf("Carlos") }

    val context = LocalContext.current
    val repo = remember { PartidasRepository(context) }

    var top3 by remember { mutableStateOf<List<Partida>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }


    val disposables = remember { CompositeDisposable() }

    DisposableEffect(Unit) {

        loading = true

        val disposable = repo.top10()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { lista ->
                    top3 = lista.take(3)
                    loading = false
                },
                { error ->
                    error.printStackTrace()
                    loading = false
                }
            )
            disposables.add(disposable)
            onDispose {
                disposables.clear()
            }
        }


    val gold = Color(0xFFFFD700)
    val darkOverlay = Color(0xCC111111)

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
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .align(Alignment.Center)
                .background(
                    color = darkOverlay,
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 2.dp,
                    color = gold.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ruleta",
                    style = MaterialTheme.typography.headlineLarge,
                    color = gold,
                    fontWeight =  FontWeight.ExtraBold
                )

                OutlinedTextField(
                    value = jugador,
                    onValueChange = { jugador = it },
                    label = {
                        Text(
                            text = "Nombre del jugador",
                            color = gold
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = gold,
                        unfocusedBorderColor = gold.copy(alpha = 0.7f),
                        focusedLabelColor = gold,
                        unfocusedLabelColor = gold.copy(alpha = 0.8f),
                        cursorColor = gold,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                Button(
                    onClick = { onNuevaPartida(jugador) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC00000),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(2.dp, gold)
                ) {
                    Text(
                        text = "Nueva partida",
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onHistorial,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(2.dp, gold),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = gold
                    )
                ) {
                    Text(
                        text = "Historial",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🏆 Mejores puntuaciones",
                    color = gold,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                when {
                    loading -> {
                        Text(
                            text = "Cargando...",
                            color = Color.White
                        )
                    }

                    top3.isEmpty() -> {
                        Text(
                            text = "Aún no hay partidas.",
                            color = Color.LightGray
                        )
                    }

                    else -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            top3.forEachIndexed { index, partida ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = Color.Black.copy(alpha = 0.35f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = gold.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}. ${partida.jugador} - ${partida.monedasFinales} monedas",
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            onNuevaPartida = {},
            onHistorial = {}
        )
    }
}