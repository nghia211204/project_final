

-keepattributes *Annotation*

-keep class com.example.movieapp.api.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep class com.example.movieapp.manager.** { *; }

-keepattributes Signature,Kotlin
-keep class kotlin.Metadata { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.flow.internal.FlowExceptions_commonKt {}
-keepclassmembers class kotlinx.coroutines.android.AndroidDispatcherFactory {
    <init>();
}
-dontwarn kotlinx.coroutines.flow.**

-keep class retrofit2.DefaultCallAdapterFactory { *; }
-keep interface retrofit2.Call { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-dontwarn okio.**
-dontwarn retrofit2.Platform$Java8

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements android.os.Parcelable {
  public void writeToParcel(android.os.Parcel,int);
}