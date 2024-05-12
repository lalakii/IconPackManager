-verbose
-ignorewarnings
-optimizationpasses 7
-optimizations !code/simplification/arithmetic,!field/,!class/merging/,!code/allocation/variable
-dontusemixedcaseclassnames
-allowaccessmodification
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}