# Exceptions
-keep public class * extends java.lang.Exception
-keepattributes Exceptions

# Annotations
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Ignore annotation used for build tooling.
#-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
#-dontwarn javax.annotation.**

# Stolen
#-keep class com.google.android.material.** { *; }
#-dontwarn com.google.android.material.**
#-keep class com.google.android.vending.licensing.** { *; }
#-keep class android.widget.** { *; }
#-keep class android.support.v4.** { *; }
#-keep class kotlin.internal.** { *; }
#-keep class com.google.android.googlequicksearchbox.** { *; }
#-keep class androidx.core.app.** { *; }
#-keep public class com.google.android.gms.** { *; }
#-dontwarn com.google.android.gms.**

# Retrofit
#-dontwarn retrofit2.KotlinExtensions
#-dontwarn retrofit2.KotlinExtensions$*
#-dontwarn retrofit2.**
#-dontwarn org.codehaus.mojo.**
#-keep class retrofit2.** { *; }
#-keepattributes EnclosingMethod
#-keepclasseswithmembers class * {
#    @retrofit2.* <methods>;
#}
#-keepclasseswithmembers interface * {
#    @retrofit2.* <methods>;
#}
#-keepclasseswithmembers class * {
#    @retrofit2.http.* <methods>;
#}
#-keepclassmembers,allowshrinking,allowobfuscation interface * {
#    @retrofit2.http.* <methods>;
#}
## Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
## EnclosingMethod is required to use InnerClasses.
#-keepattributes Signature, InnerClasses, EnclosingMethod

# Crashlytics
#-keepattributes SourceFile,LineNumberTable
#-keep class com.crashlytics.** { *; }
#-dontwarn com.crashlytics.**