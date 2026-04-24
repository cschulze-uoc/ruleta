package com.apktados.ruleta.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.apktados.ruleta.R
import com.apktados.ruleta.ui.bars.RuletaTopBar
import com.apktados.ruleta.ui.help.buildHelpHtml

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val html = remember(context) { buildHelpHtml(context) }

    Scaffold(
        topBar = {

            RuletaTopBar(
                titulo = stringResource(R.string.help),
                onBack = { navController.popBackStack() },
                onNavigateHome = { navController.navigate("home") },
                onNavigateRanking = { navController.navigate("history") },
                onNavigateGame = { navController.navigate("home") },
                onNavigateHelp = {}
            )
        }
    ) { paddingValues ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = false
                    settings.domStorageEnabled = false

                    loadDataWithBaseURL(
                        "file:///android_res/drawable/",
                        html,
                        "text/html",
                        "utf-8",
                        null
                    )
                }
            }
        )
    }
}