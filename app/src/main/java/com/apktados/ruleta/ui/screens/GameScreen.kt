package com.apktados.ruleta.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.apktados.ruleta.ApuestaCheckbox
import com.apktados.ruleta.R
import com.apktados.ruleta.game.ApuestaEvaluator
import com.apktados.ruleta.game.ResultadoRuleta
import com.apktados.ruleta.game.RuletaEngine
import com.apktados.ruleta.game.TipoApuesta
import com.apktados.ruleta.toggleApuesta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
public fun GameScreen(
    jugador: String,
    navController: NavController
) {
    val rotation = remember { Animatable(0f) }
    var girando by remember { mutableStateOf(false) }

    val engine = remember { RuletaEngine() }

    var monedas by remember { mutableStateOf(3) }
    var resultado by remember { mutableStateOf<ResultadoRuleta?>(null) }
    var cantidadApuesta by remember { mutableStateOf(1) }
    var apuestasSeleccionadas by remember {
        mutableStateOf(setOf<TipoApuesta>())
    }
    var partidaFinalizada by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val repo = remember { com.apktados.ruleta.data.PartidasRepository(context) }
    val scope = rememberCoroutineScope()

    if (partidaFinalizada) {
        Spacer(modifier = Modifier.height(16.dp))

        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🏁 Partida finalizada")
                Text("Monedas finales: $monedas")

                Button(
                    onClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                ) {
                    Text("Volver al menú")
                }
            }
        }
    }
    else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text("Jugador: $jugador")
            Text("Monedas: $monedas")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ruleta),
                    contentDescription = "Ruleta",
                    modifier = Modifier
                        .size(200.dp)
                        .graphicsLayer(rotationZ = rotation.value)
                )
            }
            if (girando) {
                Text("Girando…")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 SELECTOR DE CANTIDAD
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Cantidad:")

                Button(
                    onClick = { if (cantidadApuesta > 1) cantidadApuesta-- }
                ) {
                    Text("-")
                }

                Text("$cantidadApuesta")

                Button(
                    onClick = {
                        if (cantidadApuesta < monedas) cantidadApuesta++
                    }
                ) {
                    Text("+")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Selecciona tus apuestas:")

            ApuestaCheckbox(
                "Rojo", TipoApuesta.ROJO, apuestasSeleccionadas,
                onCheckedChange = { checked ->
                    apuestasSeleccionadas =
                        toggleApuesta(apuestasSeleccionadas, TipoApuesta.ROJO, checked)
                }
            )
            ApuestaCheckbox("Negro", TipoApuesta.NEGRO, apuestasSeleccionadas) { checked ->
                apuestasSeleccionadas = toggleApuesta(apuestasSeleccionadas, TipoApuesta.NEGRO, checked)
            }

            ApuestaCheckbox("Par", TipoApuesta.PAR, apuestasSeleccionadas) { checked ->
                apuestasSeleccionadas = toggleApuesta(apuestasSeleccionadas, TipoApuesta.PAR, checked)
            }

            ApuestaCheckbox("Impar", TipoApuesta.IMPAR, apuestasSeleccionadas) { checked ->
                apuestasSeleccionadas = toggleApuesta(apuestasSeleccionadas, TipoApuesta.IMPAR, checked)
            }

            ApuestaCheckbox("Passe (19-36)", TipoApuesta.PASSE, apuestasSeleccionadas) { checked ->
                apuestasSeleccionadas = toggleApuesta(apuestasSeleccionadas, TipoApuesta.PASSE, checked)
            }

            ApuestaCheckbox("Manque (1-18)", TipoApuesta.MANQUE, apuestasSeleccionadas) { checked ->
                apuestasSeleccionadas =
                    toggleApuesta(apuestasSeleccionadas, TipoApuesta.MANQUE, checked)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (girando) return@Button
                    if (apuestasSeleccionadas.isEmpty()) return@Button
                    if (cantidadApuesta > monedas) return@Button

                    // Bloqueamos UI
                    girando = true

                    // Lanzamos animación + resolución
                    scope.launch {

                        // Gira entre 3 y 6 vueltas
                        val vueltas = (3..6).random()
                        val extra = (0..359).random()
                        val target = rotation.value + (vueltas * 360f) + extra

                        // Animación
                        rotation.animateTo(
                            targetValue = target,
                            animationSpec = tween(durationMillis = 1500)
                        )

                        // Pequeña pausa para "efecto"
                        delay(150)

                        // Calculamos resultado y aplicamos lógica
                        val nuevoResultado = engine.girar()
                        resultado = nuevoResultado

                        monedas -= cantidadApuesta

                        apuestasSeleccionadas.forEach { tipo ->
                            val gano = ApuestaEvaluator.esGanadora(tipo, nuevoResultado)
                            if (gano) {
                                monedas += cantidadApuesta * 2
                            }
                        }

                        if (cantidadApuesta > monedas) {
                            cantidadApuesta = monedas.coerceAtLeast(1)
                        }

                        if (monedas <= 0) {
                            partidaFinalizada = true
                        }

                        // Desbloqueamos UI
                        girando = false
                    }
                },
                enabled = !girando &&
                        !partidaFinalizada &&
                        apuestasSeleccionadas.isNotEmpty() &&
                        monedas >= cantidadApuesta &&
                        monedas > 0
            ) {
                Text("Girar ruleta")
            }

            Button(
                onClick = {
                    partidaFinalizada = true
                    scope.launch(Dispatchers.IO) {
                        repo.guardarPartida(jugador = jugador, monedasFinales = monedas)
                    }
                },
                enabled = monedas > 0 && !partidaFinalizada,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Retirarse")
            }

            resultado?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Número: ${it.numero}")
            }

            if (monedas <= 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("❌ Te has quedado sin monedas")
                partidaFinalizada = true;
            }
        }
    }


}