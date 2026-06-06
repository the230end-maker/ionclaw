package com.ionclaw.app

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ionclaw.app.server.ServerViewModel
import com.ionclaw.app.ui.PanelScreen
import com.ionclaw.app.ui.ServerScreen
import com.ionclaw.app.ui.theme.IonClawTheme

class MainActivity : ComponentActivity() {
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // light status bar icons over the dark navy header
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        requestNotificationPermission()

        setContent {
            IonClawTheme {
                IonClawApp()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun IonClawApp() {
    val viewModel: ServerViewModel = viewModel()
    var showPanel by remember { mutableStateOf(false) }

    if (showPanel && viewModel.isRunning) {
        // mirror the server: a 0.0.0.0 bind is reached as localhost, which the webview treats as a
        // distinct, working origin (the literal 127.0.0.1 misbehaves behind some device network stacks)
        val panelHost = if (viewModel.config.host == "0.0.0.0") "localhost" else viewModel.config.host
        PanelScreen(url = "http://$panelHost:${viewModel.port}/app/", onBack = { showPanel = false })
    } else {
        ServerScreen(
            viewModel = viewModel,
            onOpenPanel = { showPanel = true },
            modifier = Modifier.fillMaxSize()
        )
    }
}
