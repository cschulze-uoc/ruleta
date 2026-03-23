package com.apktados.ruleta.ui.screens

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.apktados.ruleta.R
import com.apktados.ruleta.game.ApuestaEvaluator
import com.apktados.ruleta.game.ResultadoRuleta
import com.apktados.ruleta.game.RuletaEngine
import com.apktados.ruleta.game.TipoApuesta
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.DisposableEffect
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon

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

    var monedas by remember { mutableStateOf(3) }
    var resultado by remember { mutableStateOf<ResultadoRuleta?>(null) }

    val disposables = remember { CompositeDisposable() }
    DisposableEffect(Unit) {
        onDispose {
            disposables.clear()
        }
    }

    var partidaFinalizada by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val repo = remember { com.apktados.ruleta.data.PartidasRepository(context) }
    val scope = rememberCoroutineScope()

    val gold = Color(0xFFFFD700)
    val darkPanel = Color(0xB3000000)
    val panelInside = Color(0x99000000)
    val selectedRed = Color(0xFFC00000)
    val casinoGreen = Color(0xFF2E7D32)
    val casinoGreenDark = Color(0xFF1B5E20)
    val casinoBrown = Color(0xFF6D4C41)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_casino),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        if (partidaFinalizada) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(darkPanel)
                            .border(
                                BorderStroke(2.dp, gold),
                                RoundedCornerShape(24.dp)
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "PARTIDA FINALIZADA",
                                color = gold,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                "Monedas finales: $monedas",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Button(
                                onClick = {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = selectedRed,
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(2.dp, gold)
                            ) {
                                Text("Volver al menú", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = darkPanel,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = gold.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Jugador: $jugador", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Monedas: $monedas", color = Color.White)
                            Text("Apostado: $apuestaTotal", color = Color.White)

                            if (girando) {
                                Text(
                                    "Girando…",
                                    color = gold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ruleta2),
                            contentDescription = "Ruleta",
                            modifier = Modifier
                                .size(200.dp)
                                .graphicsLayer(rotationZ = rotation.value)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = darkPanel,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = gold.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "APUESTAS",
                                color = gold,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                "Color",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )

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
                                                selectedRed
                                            else
                                                casinoGreen,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp)
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
                                                Color.Black
                                            else
                                                casinoGreen,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) { Text("Negro") }

                                Spacer(modifier = Modifier.width(16.dp))

                                Button(
                                    onClick = {
                                        if (monedasColor > 0) monedasColor--
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = casinoBrown,
                                        contentColor = Color.White
                                    )
                                ) { Text("-") }

                                Text("$monedasColor", color = Color.White, fontWeight = FontWeight.Bold)

                                Button(
                                    onClick = {
                                        if (apuestaTotal < monedas) monedasColor++
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = casinoBrown,
                                        contentColor = Color.White
                                    )
                                ) { Text("+") }
                            }

                            Text(
                                "Paridad",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )

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
                                                selectedRed
                                            else
                                                casinoGreen,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp)
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
                                                selectedRed
                                            else
                                                casinoGreen,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) { Text("Impar") }

                                Spacer(modifier = Modifier.width(16.dp))

                                Button(
                                    onClick = {
                                        if (monedasParidad > 0) monedasParidad--
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = casinoBrown,
                                        contentColor = Color.White
                                    )
                                ) { Text("-") }

                                Text("$monedasParidad", color = Color.White, fontWeight = FontWeight.Bold)

                                Button(
                                    onClick = {
                                        if (apuestaTotal < monedas) monedasParidad++
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = casinoBrown,
                                        contentColor = Color.White
                                    )
                                ) { Text("+") }
                            }

                            Text(
                                "Mitad",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )

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
                                                selectedRed
                                            else
                                                casinoGreen,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp)
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
                                                selectedRed
                                            else
                                                casinoGreen,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) { Text("Passe") }

                                Spacer(modifier = Modifier.width(16.dp))

                                Button(
                                    onClick = {
                                        if (monedasMitad > 0) monedasMitad--
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = casinoBrown,
                                        contentColor = Color.White
                                    )
                                ) { Text("-") }

                                Text("$monedasMitad", color = Color.White, fontWeight = FontWeight.Bold)

                                Button(
                                    onClick = {
                                        if (apuestaTotal < monedas) monedasMitad++
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = casinoBrown,
                                        contentColor = Color.White
                                    )
                                ) { Text("+") }
                            }
                            //

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (girando) return@Button
                                    if (!apuestasValidas) return@Button
                                    if (apuestaTotal == 0) return@Button
                                    if (apuestaTotal > monedas) return@Button


                                    girando = true
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

                                        monedas -= apuestaTotal

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

                                        if (monedas <= 0) {
                                            partidaFinalizada = true
                                        }

                                        girando = false
                                    }
                                },

                                enabled = !girando &&
                                        !partidaFinalizada &&
                                        apuestaTotal > 0 &&
                                        apuestaTotal <= monedas &&
                                        apuestasValidas,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = selectedRed,
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(2.dp, gold)
                            ) {
                                Text("Girar ruleta", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    partidaFinalizada = true
                                    val disposable =
                                        repo.guardarPartida(
                                        jugador = jugador,
                                        monedasFinales = monedas
                                    )
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                            { println("Partida guardada") },
                                            { error -> Log.e("DB", "Error guardando partida", error) }
                                        )
                                    disposables.add(disposable)
                                },
                                enabled = monedas > 0 && !partidaFinalizada,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = casinoGreenDark,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(text = "Retirarse", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

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

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = panelInside,
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = gold.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Resultado: ${it.numero} ($color, $paridad, $mitad)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (monedas <= 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Sin monedas",
                                tint = Color.Red
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                "Te has quedado sin monedas",
                                color = Color.White
                            )
                        }
                        partidaFinalizada = true
                    }
                }
            }
        }
    }
}