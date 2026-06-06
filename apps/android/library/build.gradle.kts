plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.ionclaw.lib"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24

        ndk {
            // 64-bit only: llama.cpp does not build on 32-bit arm, and 32-bit abis are legacy
            abiFilters += listOf("arm64-v8a", "x86_64")
        }

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
            }
        }

        consumerProguardFiles("consumer-rules.pro")
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    // the prebuilt native runtime is shipped unstripped to preserve its exported symbols
    packaging {
        jniLibs.keepDebugSymbols += "**/libionclaw.so"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
