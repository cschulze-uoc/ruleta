package com.apktados.ruleta.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
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
import com.apktados.ruleta.data.Partida
import com.apktados.ruleta.data.PartidasRepository
import com.apktados.ruleta.notification.NotificationHelper
import com.apktados.ruleta.ui.bars.RuletaTopBar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.GoogleAuthProvider
import com.apktados.ruleta.data.FirestoreManager

@SuppressLint("SuspiciousIndentation")
@Composable
fun HomeScreen(
    onNuevaPartida: (String) -> Unit,
    onHistorial: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val repo = remember { PartidasRepository(context) }
    val firestore = remember { FirestoreManager() }

    val auth = remember { FirebaseAuth.getInstance() }
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleClient = GoogleSignIn.getClient(context, gso)

    var user by remember {
        mutableStateOf(auth.currentUser)
    }

    val app = context.applicationContext as com.apktados.ruleta.RuletaApp
    val musicManager = app.musicManager

    val musicPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            musicManager.setCustomMusic(it)
        }
    }

    var top10 by remember { mutableStateOf<List<Partida>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] == true
        val readCalendarGranted = permissions[Manifest.permission.READ_CALENDAR] == true
        val writeCalendarGranted = permissions[Manifest.permission.WRITE_CALENDAR] == true

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

        if (readCalendarGranted && writeCalendarGranted) {
            Log.d("PERM", "Permisos de calendario concedidos")
        } else {
            Log.e("PERM", "Permisos de calendario denegados")
        }
    }

    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)

            val credential = GoogleAuthProvider.getCredential(
                account.idToken,
                null
            )

            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        user = auth.currentUser
                        Log.d(
                            "LOGIN",
                            auth.currentUser?.displayName ?: "NULL"
                        )
                    } else {
                        Log.e("LOGIN", "Error login")
                    }
                }

        } catch (e: Exception) {
            e.printStackTrace()
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

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permisosPendientes.add(Manifest.permission.READ_CALENDAR)
        }

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permisosPendientes.add(Manifest.permission.WRITE_CALENDAR)
        }

        if (permisosPendientes.isNotEmpty()) {
            permissionLauncher.launch(permisosPendientes.toTypedArray())
        }
    }

    /*val disposables = remember { CompositeDisposable() }

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
    }*/
    LaunchedEffect(Unit) {

        loading = true

        firestore.obtenerTop10 { lista ->

            top10 = lista

            loading = false
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
                onNavigateRanking = { navController.navigate("history") },
                onNavigateGame = { onNuevaPartida(auth.currentUser?.displayName ?: "Jugador") },
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

                    Button(
                        onClick = {
                            val signInIntent = googleClient.signInIntent
                            authLauncher.launch(signInIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4285F4),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(2.dp, gold)
                    ) {
                        Text("Iniciar sesión con Google")
                    }

                    Button(
                        onClick = {

                            auth.signOut()

                            googleClient.signOut().addOnCompleteListener {
                                user = null
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(2.dp, gold)
                    ) {
                        Text("Cerrar sesión")
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
                        value = user?.displayName ?: "",
                        onValueChange = {},
                        enabled = false,
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
                            unfocusedContainerColor = Color.Transparent,
                            disabledTextColor = Color.White,
                            disabledBorderColor = gold.copy(alpha = 0.7f),
                            disabledLabelColor = gold.copy(alpha = 0.8f)
                        )
                    )

                    Button(
                        onClick = { onNuevaPartida(user?.displayName ?: "Jugador") },
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

                        top10.isEmpty() -> {
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
                                top10.forEachIndexed { index, partida ->
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