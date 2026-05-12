package com.apktados.ruleta.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.apktados.ruleta.firebase.FirebaseRepository
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.navigation.NavController
import com.apktados.ruleta.R
import com.apktados.ruleta.calendar.CalendarHelper
import com.apktados.ruleta.data.PartidasRepository
import com.apktados.ruleta.game.ApuestaEvaluator
import com.apktados.ruleta.game.ResultadoRuleta
import com.apktados.ruleta.game.RuletaEngine
import com.apktados.ruleta.game.TipoApuesta
import com.apktados.ruleta.location.locationHelper
import com.apktados.ruleta.notification.NotificationHelper
import com.apktados.ruleta.ui.bars.RuletaTopBar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.OutputStream

@Composable
public fun GameScreen(
    jugador: String,
    navController: NavController
) {
    val view = LocalView.current

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

    var partidaFinalizada by remember { mutableStateOf(false) }
    var retiradaVoluntaria by remember { mutableStateOf(false) }
    var showCoinsEffect by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val repo = remember { PartidasRepository(context) }
    val scope = rememberCoroutineScope()
    val calendarHelper = remember { CalendarHelper(context) }

    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    val rouletteSoundId = remember {
        soundPool.load(context, R.raw.roulette_casino_realistic, 1)
    }

    val coinsSoundId = remember {
        soundPool.load(context, R.raw.coins_casino_realistic, 1)
    }

    val disposables = remember { CompositeDisposable() }
    DisposableEffect(Unit) {
        onDispose {
            disposables.clear()
            soundPool.release()
        }
    }

    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = view.drawToBitmap()
            val stream: OutputStream? =
                context.contentResolver.openOutputStream(uri)

            stream?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }

    val gold = Color(0xFFFFD700)
    val darkPanel = Color(0xB3000000)
    val panelInside = Color(0x99000000)
    val selectedRed = Color(0xFFC00000)
    val casinoGreen = Color(0xFF2E7D32)
    val casinoGreenDark = Color(0xFF1B5E20)
    val casinoBrown = Color(0xFF6D4C41)

    val inicioPartida = remember { System.currentTimeMillis() }

    val locationHelper = locationHelper(context)

    Scaffold(
        topBar = {

            RuletaTopBar(
                titulo = stringResource(R.string.roulette_title),
                onBack = { navController.popBackStack() },
                onNavigateHome = { navController.navigate("home") },
                onNavigateRanking = {
                    navController.navigate("history")
                },
                onNavigateGame = {},
                onNavigateHelp = { navController.navigate("help") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // Imagen de fondo
            Image(
                painter = painterResource(id = R.drawable.fondo_casino),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (showCoinsEffect) {
                FallingCoinsOverlay(modifier = Modifier.fillMaxSize())
            }

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
                                    stringResource(R.string.game_finished),
                                    color = gold,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    stringResource(R.string.final_coins, monedas),
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
                                    Text(
                                        stringResource(R.string.back_to_menu),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (retiradaVoluntaria) {
                                    Button(
                                        onClick = {
                                            saveLauncher.launch("ruleta_resultado.png")
                                        }
                                    ) {
                                        Text("Guardar captura")
                                    }
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
                                Text(
                                    stringResource(R.string.player_label, jugador),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    stringResource(R.string.coins_label, monedas),
                                    color = Color.White
                                )
                                Text(
                                    stringResource(R.string.bet_total_label, apuestaTotal),
                                    color = Color.White
                                )

                                if (girando) {
                                    Text(
                                        stringResource(R.string.spinning),
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
                                contentDescription = stringResource(R.string.roulette_image),
                                modifier = Modifier
                                    .size(200.dp)
                                    .graphicsLayer(rotationZ = rotation.value)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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
                                    stringResource(R.string.bets_title),
                                    color = gold,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    stringResource(R.string.bet_color),
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
                                    ) { Text(stringResource(R.string.red)) }

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
                                    ) { Text(stringResource(R.string.black)) }

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

                                    Text(
                                        "$monedasColor",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )

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
                                    stringResource(R.string.bet_parity),
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
                                    ) { Text(stringResource(R.string.even)) }

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
                                    ) { Text(stringResource(R.string.odd)) }

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

                                    Text(
                                        "$monedasParidad",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )

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
                                    stringResource(R.string.bet_half),
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
                                    ) { Text(stringResource(R.string.manque)) }

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
                                    ) { Text(stringResource(R.string.passe)) }

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

                                    Text(
                                        "$monedasMitad",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )

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

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (girando) return@Button
                                        if (!apuestasValidas) return@Button
                                        if (apuestaTotal == 0) return@Button
                                        if (apuestaTotal > monedas) return@Button

                                        girando = true
                                        scope.launch {

                                            soundPool.play(
                                                rouletteSoundId,
                                                1f,
                                                1f,
                                                1,
                                                0,
                                                1f
                                            )

                                            // Gira entre 3 y 6 vueltas
                                            val vueltas = (3..6).random()
                                            val extra = (0..359).random()
                                            val target = rotation.value + (vueltas * 360f) + extra

                                            // Animación
                                            rotation.animateTo(
                                                targetValue = target,
                                                animationSpec = tween(durationMillis = 2200)
                                            )

                                            // Pequeña pausa para "efecto"
                                            delay(150)

                                            // Calculamos resultado y aplicamos lógica
                                            val nuevoResultado = engine.girar()
                                            resultado = nuevoResultado

                                            monedas -= apuestaTotal

                                            betColor?.let {
                                                if (ApuestaEvaluator.esGanadora(
                                                        it,
                                                        nuevoResultado
                                                    )
                                                ) {
                                                    monedas += monedasColor * 2
                                                }
                                            }
                                            betParidad?.let {
                                                if (ApuestaEvaluator.esGanadora(
                                                        it,
                                                        nuevoResultado
                                                    )
                                                ) {
                                                    monedas += monedasParidad * 2
                                                }
                                            }
                                            betMitad?.let {
                                                if (ApuestaEvaluator.esGanadora(
                                                        it,
                                                        nuevoResultado
                                                    )
                                                ) {
                                                    monedas += monedasMitad * 2
                                                }
                                            }

                                            soundPool.play(
                                                coinsSoundId,
                                                1f,
                                                1f,
                                                1,
                                                0,
                                                1f
                                            )

                                            showCoinsEffect = true
                                            delay(2200)
                                            showCoinsEffect = false

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
                                    Text(
                                        stringResource(R.string.spin_roulette),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = {
                                        retiradaVoluntaria = true
                                        partidaFinalizada = true

                                        val fineGranted = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED

                                        val coarseGranted = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED

                                        if (fineGranted || coarseGranted) {
                                            scope.launch {
                                                val location = locationHelper.obtenerUbicacionActual()
                                                val tiempoResolucionMs =
                                                    System.currentTimeMillis() - inicioPartida
                                                val fechaVictoria = System.currentTimeMillis()

                                                FirebaseRepository.guardarPuntuacion(
                                                    nombre = jugador,
                                                    monedas = monedas,
                                                    onOk = {
                                                        Log.d("FIREBASE", "Puntuación subida correctamente")
                                                    },
                                                    onError = { error ->
                                                        Log.e("FIREBASE", "Error subiendo puntuación: $error")
                                                    }
                                                )

                                                val disposable = repo.guardarPartida(
                                                    jugador = jugador,
                                                    monedasFinales = monedas,
                                                    lat = location?.latitude,
                                                    lon = location?.longitude,
                                                    tiempo = tiempoResolucionMs
                                                )
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(
                                                        {
                                                            val readCalendarGranted =
                                                                ContextCompat.checkSelfPermission(
                                                                    context,
                                                                    Manifest.permission.READ_CALENDAR
                                                                ) == PackageManager.PERMISSION_GRANTED

                                                            val writeCalendarGranted =
                                                                ContextCompat.checkSelfPermission(
                                                                    context,
                                                                    Manifest.permission.WRITE_CALENDAR
                                                                ) == PackageManager.PERMISSION_GRANTED

                                                            if (readCalendarGranted && writeCalendarGranted) {
                                                                val disposableCalendar =
                                                                    calendarHelper.guardarVictoria(
                                                                        jugador = jugador,
                                                                        monedasFinales = monedas,
                                                                        fechaMillis = fechaVictoria
                                                                    )
                                                                        .subscribeOn(Schedulers.io())
                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                        .subscribe(
                                                                            {
                                                                                NotificationHelper.mostrarVictoria(
                                                                                    context,
                                                                                    tiempoResolucionMs
                                                                                )
                                                                            },
                                                                            { error ->
                                                                                Log.e(
                                                                                    "CAL",
                                                                                    "Error guardando evento en calendario",
                                                                                    error
                                                                                )
                                                                                NotificationHelper.mostrarVictoria(
                                                                                    context,
                                                                                    tiempoResolucionMs
                                                                                )
                                                                            }
                                                                        )

                                                                disposables.add(disposableCalendar)
                                                            } else {
                                                                NotificationHelper.mostrarVictoria(
                                                                    context,
                                                                    tiempoResolucionMs
                                                                )
                                                            }
                                                        },
                                                        { error ->
                                                            Log.e(
                                                                "DB",
                                                                context.getString(R.string.db_save_error),
                                                                error
                                                            )
                                                        }
                                                    )

                                                disposables.add(disposable)
                                            }
                                        }
                                    },
                                    enabled = monedas > 0 && !partidaFinalizada,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = casinoGreenDark,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.retire),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        resultado?.let {

                            val color = if (ApuestaEvaluator.esGanadora(TipoApuesta.ROJO, it))
                                stringResource(R.string.red_lower)
                            else if (ApuestaEvaluator.esGanadora(TipoApuesta.NEGRO, it))
                                stringResource(R.string.black_lower)
                            else
                                stringResource(R.string.green)

                            val paridad = if (ApuestaEvaluator.esGanadora(TipoApuesta.PAR, it))
                                stringResource(R.string.even_lower)
                            else
                                stringResource(R.string.odd_lower)

                            val mitad = if (ApuestaEvaluator.esGanadora(TipoApuesta.PASSE, it))
                                stringResource(R.string.passe_lower)
                            else
                                stringResource(R.string.manque_lower)

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
                                    text = stringResource(
                                        R.string.result_text,
                                        it.numero,
                                        color,
                                        paridad,
                                        mitad
                                    ),
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
                                    contentDescription = stringResource(R.string.no_coins_cd),
                                    tint = Color.Red
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    stringResource(R.string.no_coins_message),
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
}

data class CoinItem(
    val x: Dp,
    val size: Dp,
    val rotation: Float,
    val startY: Float,
    val endY: Float,
    val duration: Int
)

@Composable
fun FallingCoinsOverlay(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "coins")

    val coins = listOf(
        CoinItem(10.dp, 70.dp, 12f, -100f, 1550f, 3000),
        CoinItem(40.dp, 72.dp, -10f, -220f, 1500f, 3250),
        CoinItem(70.dp, 80.dp, 20f, -160f, 1650f, 3500),
        CoinItem(100.dp, 82.dp, -18f, -260f, 1580f, 3725),
        CoinItem(130.dp, 89.dp, 8f, -140f, 1700f, 4000),
        CoinItem(160.dp, 90.dp, -24f, -300f, 1600f, 4250),
        CoinItem(190.dp, 92.dp, 16f, -180f, 1680f, 4500),
        CoinItem(220.dp, 99.dp, -14f, -240f, 1540f, 4725),
        CoinItem(250.dp, 100.dp, 24f, -120f, 1620f, 5000),
        CoinItem(280.dp, 110.dp, -8f, -280f, 1720f, 5250),
        CoinItem(310.dp, 120.dp, 14f, -170f, 1660f, 5500),
        CoinItem(340.dp, 130.dp, -20f, -250f, 1590f, 5275)
    )

    Box(modifier = modifier) {
        coins.forEachIndexed { index, coin ->
            val y by transition.animateFloat(
                initialValue = coin.startY,
                targetValue = coin.endY,
                animationSpec = infiniteRepeatable(
                    animation = tween(coin.duration),
                    repeatMode = RepeatMode.Restart
                ),
                label = "coinY$index"
            )

            Image(
                painter = painterResource(id = R.drawable.coin_casino),
                contentDescription = "Moneda",
                modifier = Modifier
                    .size(coin.size)
                    .offset(x = coin.x, y = y.dp)
                    .graphicsLayer(rotationZ = coin.rotation)
            )
        }
    }
}