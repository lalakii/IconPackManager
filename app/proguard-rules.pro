-verbose
-ignorewarnings
-optimizationpasses 7
-dontusemixedcaseclassnames
-overloadaggressively
-flattenpackagehierarchy "unused"
-adaptresourcefilenames
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static *** isLoggable(java.lang.String, ...);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
    public static java.lang.String getStackTraceString(java.lang.Throwable);}
-assumenosideeffects class java.io.PrintStream {
     public void println(%);
     public void println(**);
     public *** print(...);
     public *** println(...);}
-assumenosideeffects class java.util.logging.Logger {
     public *** log(...);
     public *** logp(...);}
-assumenosideeffects class androidx.profileinstaller.**{ *;}
-assumenosideeffects class androidx.view.menu.**{ *;}
-assumenosideeffects class androidx.emoji2.**{ *;}
-assumenosideeffects class androidx.startup.**{ *;}
-assumenosideeffects class androidx.versionedparcelable.**{ *;}
-assumenosideeffects class androidx.activity.OnBackPressedDispatcher{ *;}
-assumenosideeffects class androidx.activity.OnBackPressedDispatcher$LifecycleOnBackPressedCancellable{ *;}
-assumenosideeffects class androidx.activity.ImmLeaksCleaner{ *;}