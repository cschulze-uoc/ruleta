package com.apktados.ruleta.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.apktados.ruleta.R
import com.apktados.ruleta.auth.AuthManager
import com.apktados.ruleta.auth.AuthState
import com.apktados.ruleta.data.Partida
import com.apktados.ruleta.data.PartidasRepository
import com.apktados.ruleta.notification.NotificationHelper
import com.apktados.ruleta.ui.bars.RuletaTopBar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

@SuppressLint("SuspiciousIndentation")
@Composable
fun HomeScreen(
    onNuevaPartida: (String) -> Unit,
    onHistorial: () -> Unit,
    navController: NavController
) {
    var jugador by remember { mutableStateOf("Carlos") }

    val context = LocalContext.current
    val repo = remember { PartidasRepository(context) }
    val authManager = remember { AuthManager(context) }
    var authState by remember { mutableStateOf(authManager.currentState()) }

    val app = context.applicationContext as com.apktados.ruleta.RuletaApp
    val musicManager = app.musicManager

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            authManager.signInWithGoogleResult(result.data)
                .addOnFailureListener { error ->
                    Log.e("AUTH", context.getString(R.string.google_sign_in_error), error)
                }
        } catch (error: Exception) {
            Log.e("AUTH", context.getString(R.string.google_sign_in_error), error)
        }
    }

    val musicPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            musicManager.setCustomMusic(it)
        }
    }

    var top3 by remember { mutableStateOf<List<Partida>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] == true

        if (fineGranted || coarseGranted) {
            Log.d("PERM", context.getString(R.string.permission_location_granted))
        } else {
            Log.e("PERM", context.getString(R.string.permission_location_denied))
        }

        if (notificationGranted) {
            Log.d("PERM", context.getString(R.string.permission_notifications_granted))
        } else {
            Log.e("PERM", context.getString(R.string.permission_notifications_denied))
        }
    }

    LaunchedEffect(Unit) {
        NotificationHelper.crearCanal(context)

        val permisosPendientes = mutableListOf<String>()

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permisosPendientes.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permisosPendientes.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permisosPendientes.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permisosPendientes.isNotEmpty()) {
            permissionLauncher.launch(permisosPendientes.toTypedArray())
        }
    }

    val disposables = remember { CompositeDisposable() }

    DisposableEffect(authManager) {
        val listener = authManager.addAuthStateListener { state ->
            authState = state
        }

        onDispose {
            authManager.removeAuthStateListener(listener)
        }
    }

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

    Scaffold(
        topBar = {
            RuletaTopBar(
                titulo = stringResource(R.string.topbar_title),
                onBack = { navController.popBackStack() },
                onNavigateHome = { navController.navigate("home") },
                onNavigateRanking = { navController.navigate("ranking") },
                onNavigateGame = { onNuevaPartida(jugador) },
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
                contentDescription = stringResource(R.string.background_casino),
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
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = gold,
                        fontWeight = FontWeight.ExtraBold
                    )

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
                        when (val state = authState) {
                            is AuthState.Authenticated -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stringResource(
                                            R.string.signed_in_as,
                                            state.user.name ?: state.user.email ?: state.user.uid
                                        ),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )

                                    state.user.email?.let { email ->
                                        Text(
                                            text = email,
                                            color = Color.LightGray,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { authManager.signOut() },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, gold),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = gold
                                        )
                                    ) {
                                        Text(stringResource(R.string.sign_out))
                                    }
                                }
                            }

                            AuthState.NotAuthenticated -> {
                                Button(
                                    onClick = {
                                        googleSignInLauncher.launch(authManager.signInIntent())
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.sign_in_with_google),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { musicManager.toggleMusic() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (musicManager.musicaActiva)
                                Color(0xFF2E7D32)
                            else
                                Color(0xFF6D6D6D),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(2.dp, gold)
                    ) {
                        Text(
                            text = if (musicManager.musicaActiva)
                                stringResource(R.string.music_on)
                            else
                                stringResource(R.string.music_off),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            musicPicker.launch(arrayOf("audio/*"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B5E20),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(2.dp, gold)
                    ) {
                        Text(
                            text = stringResource(R.string.choose_music),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = jugador,
                        onValueChange = { jugador = it },
                        label = {
                            Text(
                                text = stringResource(R.string.player_name),
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
                            text = stringResource(R.string.new_game),
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
                            text = stringResource(R.string.history),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(R.string.cd_best_scores),
                            tint = gold
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = stringResource(R.string.best_scores),
                            color = gold,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    when {
                        loading -> {
                            Text(
                                text = stringResource(R.string.loading),
                                color = Color.White
                            )
                        }

                        top3.isEmpty() -> {
                            Text(
                                text = stringResource(R.string.no_games_yet),
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
                                            text = stringResource(
                                                R.string.score_row,
                                                index + 1,
                                                partida.jugador,
                                                partida.monedasFinales
                                            ),
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
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            onNuevaPartida = {},
            onHistorial = {},
            navController = {} as NavController
        )
    }
}
