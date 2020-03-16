# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html
-dontwarn org.bouncycastle.x509.util.LDAPStoreHelper
-dontwarn org.bouncycastle.jce.provider.X509LDAPCertStoreSpi
-dontwarn org.bitcoinj.store.**
-dontwarn afu.org.checkerframework.checker.**
-dontwarn org.checkerframework.checker.**
-dontwarn info.blockchain.wallet.send.SendCoins #can't find referenced class org.bitcoinj.core.Wallet
-dontwarn org.bitcoinj.protocols.channels.PaymentChannelClient #can't find referenced class org.bitcoinj.core.Wallet
-dontoptimize
-dontobfuscate

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# GUAVA - https://github.com/google/guava/wiki/UsingProGuardWithGuava
-dontwarn javax.lang.model.element.Modifier
-dontnote sun.misc.SharedSecrets
-keep class sun.misc.SharedSecrets {
  *** getJavaLangAccess(...);
}
-dontnote sun.misc.JavaLangAccess
-keep class sun.misc.JavaLangAccess {
  *** getStackTraceElement(...);
  *** getStackTraceDepth(...);
}
-keepnames class com.google.common.base.internal.Finalizer {
  *** startFinalizer(...);
}
-keepclassmembers class com.google.common.base.internal.Finalizer {
  *** startFinalizer(...);
}
-keepnames class com.google.common.base.FinalizableReference {
  void finalizeReferent();
}
-keepclassmembers class com.google.common.base.FinalizableReference {
  void finalizeReferent();
}
-dontwarn sun.misc.Unsafe
-keepclassmembers class com.google.common.cache.Striped64 {
  *** base;
  *** busy;
}
-keepclassmembers class com.google.common.cache.Striped64$Cell {
  <fields>;
}
-dontwarn java.lang.SafeVarargs
-keep class java.lang.Throwable {
  *** addSuppressed(...);
}
-keepclassmembers class com.google.common.util.concurrent.AbstractFuture** {
  *** waiters;
  *** value;
  *** listeners;
  *** thread;
  *** next;
}
-keepclassmembers class com.google.common.util.concurrent.AtomicDouble {
  *** value;
}
-keepclassmembers class com.google.common.util.concurrent.AggregateFutureState {
  *** remaining;
  *** seenExceptions;
}
-keep,allowshrinking,allowobfuscation class com.google.common.util.concurrent.AbstractFuture** {
  <fields>;
}
-dontwarn java.lang.ClassValue
-dontnote com.google.appengine.api.ThreadManager
-keep class com.google.appengine.api.ThreadManager {
  static *** currentRequestThreadFactory(...);
}
-dontnote com.google.apphosting.api.ApiProxy
-keep class com.google.apphosting.api.ApiProxy {
  static *** getCurrentEnvironment (...);
}
-keep class com.google.android.gms.ads.** { *; }
    -dontwarn okio.**