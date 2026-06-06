package com.ionclaw.lib

// raw bindings to the public C ABI shipped in libionclaw.so, bridged through libionclaw_jni.so
internal object IonClawNative {
    init {
        // openmp runtime the engine links against, loaded first so the engine's dependency resolves
        System.loadLibrary("omp")
        System.loadLibrary("ionclaw")
        System.loadLibrary("ionclaw_jni")
    }

    external fun nativeProjectInit(path: String): String

    external fun nativeServerStart(projectPath: String, host: String, port: Int): String

    external fun nativeServerStop(): String

    external fun nativeSetPlatformHandler(timeoutSeconds: Int)

    external fun nativePlatformRespond(requestId: Long, result: String)
}
