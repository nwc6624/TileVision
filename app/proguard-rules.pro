# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ARCore - Keep all ARCore classes
-keep class com.google.ar.core.** { *; }
-dontwarn com.google.ar.core.**

# Sceneform - Keep all Sceneform classes
-keep class com.google.ar.sceneform.** { *; }
-keep class com.google.ar.sceneform.animation.** { *; }
-keep class com.google.ar.sceneform.rendering.** { *; }
-dontwarn com.google.ar.sceneform.**

# Keep custom classes
-keep class de.westnordost.streetmeasure.** { *; }

# Keep classes that implement ARCore interfaces
-keep class * implements com.google.ar.core.Anchor { *; }
-keep class * implements com.google.ar.core.Trackable { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep ViewBinding classes
-keep class * implements androidx.viewbinding.ViewBinding { *; }

# Keep data classes for Gson
-keep class de.westnordost.streetmeasure.data.** { *; }

# Keep existing warnings
-dontwarn com.google.ar.sceneform.animation.AnimationEngine
-dontwarn com.google.ar.sceneform.animation.AnimationLibraryLoader
-dontwarn com.google.ar.sceneform.assets.Loader
-dontwarn com.google.ar.sceneform.assets.ModelData
-dontwarn com.google.devtools.build.android.desugar.runtime.ThrowableExtension
