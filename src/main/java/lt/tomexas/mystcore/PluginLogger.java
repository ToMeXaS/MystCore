package lt.tomexas.mystcore;

import java.util.logging.Logger;

public class PluginLogger {

    private static final Logger logger = Logger.getLogger("MystCore");

    private PluginLogger() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Logger is a utility class and cannot be instantiated.");
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void warn(String message) {
        logger.warning(message);
    }

    public static void error(String message) {
        logger.severe(message);
    }

    public static void debug(String message) {
        logger.fine( "[DEBUG] " + message);
    }
}
