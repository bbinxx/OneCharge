# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Preserve Compose classes to prevent crashes with R8 minification
-keep class androidx.compose.** { *; }

# Keep domain models if you use Gson/Moshi reflection, though Room usually handles this
-keep class com.bbinxx.onecharge.domain.model.** { *; }

# Room 2.6.x instantiates generated databases reflectively. AGP 9's strict
# R8 rules no longer retain no-arg constructors from class-only keep rules.
# Covers both OneChargeDatabase_Impl and WorkManager's WorkDatabase_Impl.
-keep class * extends androidx.room.RoomDatabase {
    <init>();
}
