# CalcU release R8 rules.
# Keeps for reflection-heavy third-party libs + generated Hilt/Dagger/Room code
# so minified release builds do not strip behavior at runtime.

# --- Compose runtime / material (standard, API-safe) ---
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**
-dontwarn androidx.lifecycle.**

# --- Kotlinx serialization (generated serializers referenced reflectively) ---
-keepattributes *Annotation*, InnerClasses, Signature
-keep,includedescriptorclasses class **$$serializer { *; }
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers @kotlinx.serialization.Serializable class * {
    companion object;
}
-dontnote kotlinx.serialization.**

# --- Hilt / Dagger generated components and factories ---
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-dontwarn dagger.hilt.**
-dontwarn javax.inject.**
-dontwarn hilt.**

# --- Room (generated DAO/schema code) ---
-keep class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * { @androidx.room.* <methods>; }
-dontwarn androidx.room.**

# --- Retrofit + OkHttp (interface proxies via reflection) ---
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# --- kotlinx.coroutines ---
-dontwarn kotlinx.coroutines.**

# --- EvalEx (evaluates expressions; keep operator/function registry) ---
-keep class com.ezylang.evalex.** { *; }
-dontwarn com.ezylang.evalex.**

# --- ZXing core (reflection around barcode formats) ---
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# --- Coil image loading ServiceLoader / factories ---
-keep class coil.** { *; }
-dontwarn coil.**

# --- CameraX ---
-dontwarn androidx.camera.**

# --- JBCrypt (reflection-free static entry points, keep for safety) ---
-keep class org.mindrot.jbcrypt.** { *; }

# --- Material color utilities (Hct, TonalPalette used by the theme engine) ---
-keep class com.android.base.materialcolorutilities.** { *; }
-dontwarn com.android.base.materialcolorutilities.**

# --- App entry points ---
-keep class calc.u.MainActivity { *; }
-keep class * extends android.app.Application { *; }

# Keep Hilt injection annotation metadata for all app classes.
-keepattributes javax.inject.*