package com.apktados.ruleta.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.apktados.ruleta.ui.screens.*

@Composable
fun RuletaNavGraph() {

    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                onNuevaPartida = { jugador ->
                    navController.navigate("game/$jugador")
                },
                onHistorial = {
                    navController.navigate("history")
                },
                navController
            )
        }

        composable("game/{jugador}") { backStackEntry ->
            val jugador = backStackEntry.arguments?.getString("jugador") ?: ""
            GameScreen(jugador, navController)
        }

        composable("history") {
            HistoryScreen(navController)
        }

        composable("ranking") {
            RankingScreen(navController)
        }

        composable("help") {
            HelpScreen(navController)
        }
    }
}
