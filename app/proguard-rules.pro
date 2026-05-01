# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK tools/proguard/proguard-android.txt

# Keep the app's own classes (not needed for debug builds)
-keep class com.iama2z.glyphmeter.** { *; }

# Keep Nothing Glyph SDK classes to prevent R8 from stripping them
-keep class com.nothing.ketchum.** { *; }
