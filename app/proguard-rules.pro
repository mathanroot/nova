# Nova Assistant Proguard Rules

-keep class org.vosk.** { *; }
-keep class com.nova.assistant.data.database.** { *; }
-keepattributes *Annotation*
-dontwarn org.vosk.**
