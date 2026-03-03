package com.apktados.ruleta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.navigation.compose.*
import com.apktados.ruleta.ui.screens.GameScreen
import com.apktados.ruleta.ui.screens.HistoryScreen
import com.apktados.ruleta.ui.screens.HomeScreen

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







