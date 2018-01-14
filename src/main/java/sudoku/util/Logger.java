package sudoku.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Logger {
    private static Level level = Level.OFF;
    private static PrintStream out;

    static {
        try {
            out = new PrintStream(new FileOutputStream("./test.log", false));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Logger() {

    }

    public static void setLevel(Level level) {
        Logger.level = level;
        info("Level set to " + level);
    }

    private static void write(String msg) {
        out.println(msg);
        System.out.println(msg);
    }

    public static void info(Object obj) {
        if (level.compareTo(Level.INFO) >= 0) {
            write("[INFO] " + obj);
        }
    }

    public static void debug(Object obj) {
        if (level.compareTo(Level.DEBUG) >= 0) {
            write("[DEBUG] " + obj);
        }
    }

    public enum Level {
        OFF, INFO, DEBUG
    }
}
