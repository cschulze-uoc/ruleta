package com.apktados.ruleta.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.apktados.ruleta.ui.bars.RuletaTopBar
import com.apktados.ruleta.R

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HelpScreen(navController: NavController) {
    Scaffold(
        topBar = {
            RuletaTopBar(

                titulo = stringResource(R.string.help),
                onBack = { navController.popBackStack() },
                onNavigateHome = { navController.navigate("home") },
                onNavigateRanking = { navController.navigate("history") },
                onNavigateGame = {navController.navigate("home") },
                onNavigateHelp = {}
            )
        }
    ) { paddingValues ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = false
                    settings.domStorageEnabled = true
                    loadUrl("file:///android_asset/ayuda.html")
                }
            }
        )
    }
}