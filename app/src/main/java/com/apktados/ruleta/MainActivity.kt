package com.apktados.ruleta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.apktados.ruleta.ui.theme.RuletaTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.apktados.ruleta.game.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.apktados.ruleta.data.Partida
import com.apktados.ruleta.data.PartidasRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                RuletaApp()
            }
        }
    }
}

@Composable
fun RuletaApp() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("home") {
            HomeScreen(
                onNuevaPartida = { jugador -> navController.navigate("game/$jugador") },
                onHistorial = { navController.navigate("history") }
            )
        }

        composable("game/{jugador}") { backStackEntry ->
            val jugador = backStackEntry.arguments?.getString("jugador") ?: ""
            GameScreen(
                jugador = jugador,
                navController = navController
            )
        }

        composable("history") {
            HistoryScreen(navController = navController)
        }
    }
}

@Composable
private fun HomeScreen(
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

@Composable
fun GameScreen(
    jugador: String,
    navController: NavController
) {

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

                if (apuestasSeleccionadas.isEmpty()) return@Button
                if (cantidadApuesta > monedas) return@Button

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


            },
            enabled = apuestasSeleccionadas.isNotEmpty() &&
                    monedas >= cantidadApuesta &&
                    monedas > 0 &&
                    !partidaFinalizada
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

