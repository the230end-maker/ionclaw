#include <jni.h>
#include <string>

#include "ionclaw/ionclaw.h"

namespace {
    JavaVM *vm = nullptr;
    jclass platformClass = nullptr;
    jmethodID platformDispatch = nullptr;

    // wraps an owned native json string into a jstring and releases the native allocation
    jstring takeOwnedString(JNIEnv *env, const char *owned) {
        if (owned == nullptr) {
            return env->NewStringUTF("{}");
        }

        jstring result = env->NewStringUTF(owned);
        ionclaw_free(owned);

        return result;
    }

    // forwards a native platform request to the kotlin handler on an attached thread.
    // the callback runs on a core thread, so the response is delivered later via ionclaw_platform_respond.
    void platformCallback(int64_t requestId, const char *functionName, const char *paramsJson) {
        if (vm == nullptr || platformClass == nullptr || platformDispatch == nullptr) {
            return;
        }

        JNIEnv *env = nullptr;
        bool attached = false;

        if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) == JNI_EDETACHED) {
            vm->AttachCurrentThread(&env, nullptr);
            attached = true;
        }

        jstring function = env->NewStringUTF(functionName != nullptr ? functionName : "");
        jstring params = env->NewStringUTF(paramsJson != nullptr ? paramsJson : "");

        env->CallStaticVoidMethod(platformClass, platformDispatch, static_cast<jlong>(requestId), function, params);

        env->DeleteLocalRef(function);
        env->DeleteLocalRef(params);

        if (attached) {
            vm->DetachCurrentThread();
        }
    }
}

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *javaVm, void *) {
    vm = javaVm;
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_ionclaw_lib_IonClawNative_nativeProjectInit(JNIEnv *env, jobject, jstring path) {
    const char *pathChars = env->GetStringUTFChars(path, nullptr);
    const char *result = ionclaw_project_init(pathChars);
    env->ReleaseStringUTFChars(path, pathChars);

    return takeOwnedString(env, result);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_ionclaw_lib_IonClawNative_nativeServerStart(JNIEnv *env, jobject, jstring projectPath, jstring host, jint port) {
    const char *projectChars = env->GetStringUTFChars(projectPath, nullptr);
    const char *hostChars = env->GetStringUTFChars(host, nullptr);

    const char *result = ionclaw_server_start(projectChars, hostChars, port, "", "");

    env->ReleaseStringUTFChars(projectPath, projectChars);
    env->ReleaseStringUTFChars(host, hostChars);

    return takeOwnedString(env, result);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_ionclaw_lib_IonClawNative_nativeServerStop(JNIEnv *env, jobject) {
    return takeOwnedString(env, ionclaw_server_stop());
}

extern "C" JNIEXPORT void JNICALL
Java_com_ionclaw_lib_IonClawNative_nativeSetPlatformHandler(JNIEnv *env, jobject, jint timeoutSeconds) {
    jclass localClass = env->FindClass("com/ionclaw/lib/IonClawPlatform");
    platformClass = static_cast<jclass>(env->NewGlobalRef(localClass));
    platformDispatch = env->GetStaticMethodID(platformClass, "dispatch", "(JLjava/lang/String;Ljava/lang/String;)V");
    env->DeleteLocalRef(localClass);

    ionclaw_set_platform_handler(platformCallback, timeoutSeconds);
}

extern "C" JNIEXPORT void JNICALL
Java_com_ionclaw_lib_IonClawNative_nativePlatformRespond(JNIEnv *env, jobject, jlong requestId, jstring result) {
    const char *resultChars = env->GetStringUTFChars(result, nullptr);
    ionclaw_platform_respond(requestId, resultChars);
    env->ReleaseStringUTFChars(result, resultChars);
}
