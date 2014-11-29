-injars       pastyAntOutput.jar
-outjars      pasty.jar
-libraryjars  <java.home>/lib/rt.jar

-dontobfuscate

-keep public class com.alvinalexander.pasty.Pasty {
    public static void main(java.lang.String[]);
}

