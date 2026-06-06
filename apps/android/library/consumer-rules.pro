# the native bridge resolves these by jni symbol name, so they must survive shrinking
-keep class com.ionclaw.lib.IonClawNative { *; }
-keep class com.ionclaw.lib.IonClawPlatform { *; }
