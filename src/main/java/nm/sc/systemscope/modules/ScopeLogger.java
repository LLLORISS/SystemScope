package nm.sc.systemscope.modules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for logging messages using the SLF4J logging framework.
 * Provides static methods for logging messages at different log levels: INFO, ERROR, and WARN.
 */
public class ScopeLogger {
    private static final Logger logger = LoggerFactory.getLogger(ScopeLogger.class);

    /**
     * Logs an informational message.
     *
     * @param message the message to log.
     */
    public static void logInfo(String message) {
        logger.info(message);
    }

    /**
     * Logs an informational message with optional arguments for formatting.
     *
     * @param message the message to log.
     * @param args optional arguments to format the message.
     */
    public static void logInfo(String message, Object... args) {
        logger.info(message, args);
    }

    /**
     * Logs an error message with optional arguments for formatting.
     *
     * @param message the error message to log.
     * @param args optional arguments to format the message.
     */
    public static void logError(String message, Object... args) {
        logger.error(message, args);
    }

    /**
     * Logs an error message with an exception.
     *
     * @param message the error message to log.
     * @param throwable the exception associated with the error.
     */
    public static void logError(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    /**
     * Logs a warning message.
     *
     * @param message the warning message to log.
     */
    public static void logWarn(String message) {
        logger.warn(message);
    }
}
