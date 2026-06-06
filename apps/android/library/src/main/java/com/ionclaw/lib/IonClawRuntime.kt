package com.ionclaw.lib

import org.json.JSONObject

class IonClawException(message: String) : Exception(message)

// thin kotlin wrapper over the native runtime exposed by the ionclaw aar
object IonClawRuntime {
    fun initializeProject(path: String) {
        val payload = JSONObject(IonClawNative.nativeProjectInit(path))
        ensureSuccess(payload, "failed to initialize project")
    }

    // starts the server and returns the bound port
    fun startServer(projectPath: String, host: String, port: Int): Int {
        val payload = JSONObject(IonClawNative.nativeServerStart(projectPath, host, port))
        ensureSuccess(payload, "failed to start server")

        return payload.optInt("port", port)
    }

    fun stopServer() {
        val payload = JSONObject(IonClawNative.nativeServerStop())
        ensureSuccess(payload, "failed to stop server")
    }

    private fun ensureSuccess(payload: JSONObject, message: String) {
        if (!payload.optBoolean("success", false)) {
            throw IonClawException(payload.optString("error", message))
        }
    }
}
