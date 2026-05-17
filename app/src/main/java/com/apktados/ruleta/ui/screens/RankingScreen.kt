package com.apktados.ruleta.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.apktados.ruleta.R
import com.apktados.ruleta.data.remote.FirebaseGameRepository
import com.apktados.ruleta.data.remote.GlobalPrizeRemote
import com.apktados.ruleta.data.remote.PlayerRemote
import com.apktados.ruleta.ui.bars.RuletaTopBar

@Composable
fun RankingScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { FirebaseGameRepository() }

    var players by remember { mutableStateOf<List<PlayerRemote>>(emptyList()) }
    var globalPrize by remember { mutableStateOf(GlobalPrizeRemote()) }
    var loading by remember { mutableStateOf(false) }
    var loadingPrize by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    fun loadRanking() {
        loading = true
        error = null

        repository.getTopTenPlayers()
            .addOnSuccessListener { ranking ->
                players = ranking
                loading = false
            }
            .addOnFailureListener { throwable ->
                Log.e("Firebase", context.getString(R.string.online_ranking_error), throwable)
                error = context.getString(R.string.online_ranking_error)
                loading = false
            }
    }

    suspend fun loadGlobalPrize() {
        loadingPrize = true
        Log.d("FirebaseREST", "Loading global prize via REST")

        try {
            // Esta lectura es por REST; el resto del juego sigue usando el SDK de Firebase.
            val prize = repository.getGlobalPrizeViaRest()
            globalPrize = prize
            Log.d("FirebaseREST", "Global prize REST loaded amount=${prize.amount}")
        } catch (throwable: Exception) {
            Log.e("FirebaseREST", "Error loading global prize via REST", throwable)
        } finally {
            loadingPrize = false
        }
    }

    LaunchedEffect(refreshKey) {
        loadRanking()
        loadGlobalPrize()
    }

    val gold = Color(0xFFFFD700)
    val darkOverlay = Color(0xCC111111)
    val buttonRed = Color(0xFFC00000)

    Scaffold(
        topBar = {
            RuletaTopBar(
                titulo = stringResource(R.string.online_ranking_title),
                onBack = { navController.popBackStack() },
                onNavigateHome = { navController.navigate("home") },
                onNavigateRanking = { navController.navigate("ranking") },
                onNavigateGame = { navController.navigate("home") },
                onNavigateHelp = { navController.navigate("help") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.home_background),
                contentDescription = stringResource(R.string.casino_background),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .background(
                        color = darkOverlay,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = gold.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.online_ranking_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = gold,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color.Black.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = gold.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.global_prize_ranking_state,
                                    globalPrize.amount
                                ),
                                color = gold,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (loadingPrize) {
                                Text(
                                    text = stringResource(R.string.loading_global_prize),
                                    color = Color.LightGray
                                )
                            }

                            Text(
                                text = stringResource(
                                    R.string.global_prize_last_claimed,
                                    globalPrize.lastClaimedAmount
                                ),
                                color = Color.White
                            )

                            globalPrize.lastWinnerName?.let { winnerName ->
                                Text(
                                    text = stringResource(
                                        R.string.global_prize_last_winner,
                                        winnerName
                                    ),
                                    color = Color.LightGray
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { refreshKey++ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonRed,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(2.dp, gold)
                    ) {
                        Text(
                            text = stringResource(R.string.refresh_ranking),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    when {
                        loading -> {
                            Text(
                                text = stringResource(R.string.loading_online_ranking),
                                color = Color.White
                            )
                        }

                        error != null -> {
                            Text(
                                text = error ?: "",
                                color = Color.Red
                            )
                        }

                        players.isEmpty() -> {
                            Text(
                                text = stringResource(R.string.no_online_scores),
                                color = Color.LightGray
                            )
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(players) { index, player ->
                                    RankingRow(
                                        position = index + 1,
                                        player = player,
                                        gold = gold
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

@Composable
private fun RankingRow(
    position: Int,
    player: PlayerRemote,
    gold: Color
) {
    val playerName = player.displayName ?: player.email ?: player.uid

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xEE1A1A1A))
                .border(
                    width = 1.dp,
                    color = gold.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.online_ranking_player_row, position, playerName),
                    style = MaterialTheme.typography.titleMedium,
                    color = gold,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.online_ranking_best_score, player.bestScore),
                    color = Color.White
                )

                Text(
                    text = stringResource(R.string.online_ranking_victories, player.victories),
                    color = Color.LightGray
                )
            }
        }
    }
}
