package com.apktados.ruleta

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apktados.ruleta.notification.NotificationHelper
import com.apktados.ruleta.ui.screens.GameScreen
import com.apktados.ruleta.ui.screens.HelpScreen
import com.apktados.ruleta.ui.screens.HistoryScreen
import com.apktados.ruleta.ui.screens.HomeScreen

class MainActivity : AppCompatActivity() {

    private val mostrarTiempoState = mutableStateOf(false)
    private val tiempoMsState = mutableLongStateOf(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        procesarIntent(intent)

        setContent {
            MaterialTheme {
                RuletaMainApp(
                    mostrarDialogoTiempo = mostrarTiempoState.value,
                    tiempoResolucionMs = tiempoMsState.longValue,
                    onCerrarDialogoTiempo = {
                        mostrarTiempoState.value = false
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        procesarIntent(intent)
    }

    private fun procesarIntent(intent: Intent?) {
        val mostrar =
            intent?.getBooleanExtra(NotificationHelper.EXTRA_MOSTRAR_TIEMPO, false) ?: false

        val tiempo =
            intent?.getLongExtra(NotificationHelper.EXTRA_TIEMPO_MS, 0L) ?: 0L

        mostrarTiempoState.value = mostrar
        tiempoMsState.longValue = tiempo
    }
}

@Composable
fun RuletaMainApp(
    mostrarDialogoTiempo: Boolean = false,
    tiempoResolucionMs: Long = 0L,
    onCerrarDialogoTiempo: () -> Unit = {}
) {
    val navController = rememberNavController()

    if (mostrarDialogoTiempo) {
        AlertDialog(
            onDismissRequest = onCerrarDialogoTiempo,
            confirmButton = {
                TextButton(onClick = onCerrarDialogoTiempo) {
                    Text(stringResource(R.string.accept))
                }
            },
            title = {
                Text(stringResource(R.string.resolution_time))
            },
            text = {
                Text(
                    stringResource(
                        R.string.game_duration,
                        formatearTiempo(tiempoResolucionMs)
                    )
                )
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNuevaPartida = { jugador -> navController.navigate("game/$jugador") },
                onHistorial = { navController.navigate("history") },
                navController = navController
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

        composable("help") {
            HelpScreen(navController = navController)
        }
    }
}

fun formatearTiempo(ms: Long): String {
    val totalSegundos = ms / 1000
    val minutos = totalSegundos / 60
    val segundos = totalSegundos % 60
    val milisegundos = ms % 1000

    return String.format(
        java.util.Locale.US,
        "%02d:%02d.%03d",
        minutos,
        segundos,
        milisegundos
    )
}