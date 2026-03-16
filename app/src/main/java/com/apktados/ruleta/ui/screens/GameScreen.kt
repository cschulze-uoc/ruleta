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
import androidx.compose.foundation.layout.width
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
//mport androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.apktados.ruleta.ui.components.ApuestaCheckbox
import com.apktados.ruleta.R
import com.apktados.ruleta.game.ApuestaEvaluator
import com.apktados.ruleta.game.ResultadoRuleta
import com.apktados.ruleta.game.RuletaEngine
import com.apktados.ruleta.game.TipoApuesta
import com.apktados.ruleta.ui.components.toggleApuesta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

@Composable
public fun GameScreen(
    jugador: String,
    navController: NavController
) {
    val rotation = remember { Animatable(0f) }
    var girando by remember { mutableStateOf(false) }

    val engine = remember { RuletaEngine() }

    //
    var betColor by remember { mutableStateOf<TipoApuesta?>(null) }
    var betParidad by remember { mutableStateOf<TipoApuesta?>(null) }
    var betMitad by remember { mutableStateOf<TipoApuesta?>(null) }

    var monedasColor by remember { mutableStateOf(0) }
    var monedasParidad by remember { mutableStateOf(0) }
    var monedasMitad by remember { mutableStateOf(0) }

    val apuestaTotal = monedasColor + monedasParidad + monedasMitad

    val apuestasValidas =
        (monedasColor == 0 || betColor != null) &&
                (monedasParidad == 0 || betParidad != null) &&
                (monedasMitad == 0 || betMitad != null)
    //

    var monedas by remember { mutableStateOf(3) }
    var resultado by remember { mutableStateOf<ResultadoRuleta?>(null) }
    //var cantidadApuesta by remember { mutableStateOf(1) }
    /*var apuestasSeleccionadas by remember {
        mutableStateOf(setOf<TipoApuesta>())
    }*/
    //
    /*var apuestas by remember {
        mutableStateOf(
            mutableMapOf<TipoApuesta, Int>()
        )
    }*/
    //val totalApostado = apuestas.values.sum()
    //

    var partidaFinalizada by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val repo = remember { com.apktados.ruleta.data.PartidasRepository(context) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // 🔹 Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_casino),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        if (partidaFinalizada) {
            Spacer(modifier = Modifier.height(16.dp))

            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🏁 Partida finalizada",color = androidx.compose.ui.graphics.Color.White)
                    Text("Monedas finales: $monedas",color = androidx.compose.ui.graphics.Color.White)

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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text("Jugador: $jugador",color = androidx.compose.ui.graphics.Color.White)
                Text("Monedas: $monedas",color = androidx.compose.ui.graphics.Color.White)
                Text("Apostado: $apuestaTotal",color = androidx.compose.ui.graphics.Color.White)
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
                /*Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Cantidad:",color = androidx.compose.ui.graphics.Color.White)

                    Button(
                        onClick = { if (cantidadApuesta > 1) cantidadApuesta-- }
                    ) {
                        Text("-")
                    }

                    Text("$cantidadApuesta",color = androidx.compose.ui.graphics.Color.White)

                    Button(
                        onClick = {
                            if (cantidadApuesta < monedas) cantidadApuesta++
                        }
                    ) {
                        Text("+")
                    }
                }*/

                Spacer(modifier = Modifier.height(16.dp))

                //
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            betColor =
                                if (betColor == TipoApuesta.ROJO) null
                                else TipoApuesta.ROJO
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                if (betColor == TipoApuesta.ROJO)
                                    Color.Red
                                else
                                    MaterialTheme.colorScheme.secondary
                        )
                    ) { Text("Rojo") }
                    Button(
                        onClick = {
                            betColor =
                                if (betColor == TipoApuesta.NEGRO) null
                                else TipoApuesta.NEGRO
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                if (betColor == TipoApuesta.NEGRO)
                                    Color.Red
                                else
                                    MaterialTheme.colorScheme.secondary
                        )
                    ) { Text("Negro") }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        if (monedasColor > 0) monedasColor--
                    }) { Text("-") }
                    Text("$monedasColor", color = androidx.compose.ui.graphics.Color.White)
                    Button(onClick = {
                        if (apuestaTotal < monedas) monedasColor++
                    }) { Text("+") }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            betParidad =
                                if (betParidad == TipoApuesta.PAR) null
                                else TipoApuesta.PAR
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                if (betParidad == TipoApuesta.PAR)
                                    Color.Red
                                else
                                    MaterialTheme.colorScheme.secondary
                        )
                    ) { Text("Par") }
                    Button(
                        onClick = {
                            betParidad =
                                if (betParidad == TipoApuesta.IMPAR) null
                                else TipoApuesta.IMPAR
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                if (betParidad == TipoApuesta.IMPAR)
                                    Color.Red
                                else
                                    MaterialTheme.colorScheme.secondary
                        )
                    ) { Text("Impar") }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        if (monedasParidad > 0) monedasParidad--
                    }) { Text("-") }
                    Text("$monedasParidad", color = androidx.compose.ui.graphics.Color.White)
                    Button(onClick = {
                        if (apuestaTotal < monedas) monedasParidad++
                    }) { Text("+") }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            betMitad =
                                if (betMitad == TipoApuesta.MANQUE) null
                                else TipoApuesta.MANQUE
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                if (betMitad == TipoApuesta.MANQUE)
                                    Color.Red
                                else
                                    MaterialTheme.colorScheme.secondary
                        )
                    ) { Text("Manque") }
                    Button(
                        onClick = {
                            betMitad =
                                if (betMitad == TipoApuesta.PASSE) null
                                else TipoApuesta.PASSE
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                if (betMitad == TipoApuesta.PASSE)
                                    Color.Red
                                else
                                    MaterialTheme.colorScheme.secondary
                        )
                    ) { Text("Passe") }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        if (monedasMitad > 0) monedasMitad--
                    }) { Text("-") }
                    Text("$monedasMitad", color = androidx.compose.ui.graphics.Color.White)
                    Button(onClick = {
                        if (apuestaTotal < monedas) monedasMitad++
                    }) { Text("+") }
                }
                //

                /*Text("Selecciona tus apuestas:",color = androidx.compose.ui.graphics.Color.White)

                ApuestaCheckbox(
                    "Rojo", TipoApuesta.ROJO, apuestasSeleccionadas,
                    onCheckedChange = { checked ->
                        apuestasSeleccionadas =
                            toggleApuesta(apuestasSeleccionadas, TipoApuesta.ROJO, checked)
                    }
                )
                ApuestaCheckbox("Negro", TipoApuesta.NEGRO, apuestasSeleccionadas) { checked ->
                    apuestasSeleccionadas =
                        toggleApuesta(apuestasSeleccionadas, TipoApuesta.NEGRO, checked)
                }

                ApuestaCheckbox("Par", TipoApuesta.PAR, apuestasSeleccionadas) { checked ->
                    apuestasSeleccionadas =
                        toggleApuesta(apuestasSeleccionadas, TipoApuesta.PAR, checked)
                }

                ApuestaCheckbox("Impar", TipoApuesta.IMPAR, apuestasSeleccionadas) { checked ->
                    apuestasSeleccionadas =
                        toggleApuesta(apuestasSeleccionadas, TipoApuesta.IMPAR, checked)
                }

                ApuestaCheckbox(
                    "Passe (19-36)",
                    TipoApuesta.PASSE,
                    apuestasSeleccionadas
                ) { checked ->
                    apuestasSeleccionadas =
                        toggleApuesta(apuestasSeleccionadas, TipoApuesta.PASSE, checked)
                }

                ApuestaCheckbox(
                    "Manque (1-18)",
                    TipoApuesta.MANQUE,
                    apuestasSeleccionadas
                ) { checked ->
                    apuestasSeleccionadas =
                        toggleApuesta(apuestasSeleccionadas, TipoApuesta.MANQUE, checked)
                }*/

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (girando) return@Button
                        /*if (apuestasSeleccionadas.isEmpty()) return@Button
                        if (cantidadApuesta > monedas) return@Button*/

                        if (!apuestasValidas) return@Button
                        if (apuestaTotal == 0) return@Button
                        if(apuestaTotal > monedas) return@Button

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

                            /*monedas -= cantidadApuesta

                            apuestasSeleccionadas.forEach { tipo ->
                                val gano = ApuestaEvaluator.esGanadora(tipo, nuevoResultado)
                                if (gano) {
                                    monedas += cantidadApuesta * 2
                                }
                            }*/
                            //
                            //val totalApostado = apuestas.values.sum()
                            monedas -= apuestaTotal

                            /*apuestas.forEach { (tipo, cantidad) ->
                                val gano = ApuestaEvaluator.esGanadora(tipo, nuevoResultado)
                                if (gano) {
                                    monedas += cantidad * 2
                                }
                            }*/



                            betColor?.let {
                                if (ApuestaEvaluator.esGanadora(it, nuevoResultado)) {
                                    monedas += monedasColor * 2
                                }
                            }
                            betParidad?.let {
                                if (ApuestaEvaluator.esGanadora(it, nuevoResultado)) {
                                    monedas += monedasParidad * 2
                                }
                            }
                            betMitad?.let {
                                if (ApuestaEvaluator.esGanadora(it, nuevoResultado)) {
                                    monedas += monedasMitad * 2
                                }
                            }
                            //

                            /*if (cantidadApuesta > monedas) {
                                cantidadApuesta = monedas.coerceAtLeast(1)
                            }*/

                            if (monedas <= 0) {
                                partidaFinalizada = true
                            }

                            // Desbloqueamos UI
                            girando = false
                        }
                    },
                    /*enabled = !girando &&
                            !partidaFinalizada &&
                            apuestasSeleccionadas.isNotEmpty() &&
                            monedas >= cantidadApuesta &&
                            monedas > 0*/

                    enabled = !girando &&
                            !partidaFinalizada &&
                            apuestaTotal > 0 &&
                            apuestaTotal <= monedas &&
                            apuestasValidas
                ) {
                    Text("Girar ruleta",color = androidx.compose.ui.graphics.Color.White)
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
                    Text( text = "Retirarse" )
                }

                resultado?.let {

                    val color = if (ApuestaEvaluator.esGanadora(TipoApuesta.ROJO, it))
                        "rojo"
                    else if (ApuestaEvaluator.esGanadora(TipoApuesta.NEGRO, it))
                        "negro"
                    else
                        "verde"

                    val paridad = if (ApuestaEvaluator.esGanadora(TipoApuesta.PAR, it))
                        "par"
                    else
                        "impar"

                    val mitad = if (ApuestaEvaluator.esGanadora(TipoApuesta.PASSE, it))
                        "passe"
                    else
                        "manque"

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Resultado: ${it.numero} ($color, $paridad, $mitad)",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (monedas <= 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("❌ Te has quedado sin monedas")
                    partidaFinalizada = true;
                }
            }
        }

    }
}