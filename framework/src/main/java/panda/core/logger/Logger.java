package panda.core.logger;

/**
 * Author: kerr
 */
public class Logger {
    public static void trace(String message) {
        System.out.println("[trace] :"+message);
    }

    public static void debug(String message){
        System.out.println("[debug] :"+message);
    }

    public static void info(String message){
        System.out.println("[info] :"+message);
    }

    public static void warn(String message){
        System.out.println("[warn] :"+message);
    }

    public static void error(String message){
        System.out.println("[error] :"+message);
    }

}
