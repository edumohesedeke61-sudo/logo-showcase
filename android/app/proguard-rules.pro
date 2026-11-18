# Keep rules for when minification is switched on.
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class ng.lightgrab.courier.**$$serializer { *; }
-keepclassmembers class ng.lightgrab.courier.** { *** Companion; }
-keep class io.livekit.android.** { *; }
-keep class livekit.org.webrtc.** { *; }
-dontwarn org.slf4j.**
