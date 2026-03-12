# Add project specific ProGuard rules here.
# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# Keep crypto classes
-keep class java.security.** { *; }
-keep class android.security.keystore.** { *; }
