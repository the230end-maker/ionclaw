package com.ionclaw.app.config

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// persisted server configuration shared across every screen
class AppConfig(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var host by mutableStateOf(prefs.getString(KEY_HOST, DEFAULT_HOST)!!)
        private set

    var port by mutableStateOf(prefs.getInt(KEY_PORT, DEFAULT_PORT))
        private set

    // persists every edit so the configuration survives process death
    fun update(host: String, port: Int) {
        this.host = host
        this.port = port

        prefs.edit()
            .putString(KEY_HOST, host)
            .putInt(KEY_PORT, port)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "ionclaw_server"
        const val KEY_HOST = "server_host"
        const val KEY_PORT = "server_port"
        const val DEFAULT_HOST = "0.0.0.0"
        const val DEFAULT_PORT = 8080
    }
}
