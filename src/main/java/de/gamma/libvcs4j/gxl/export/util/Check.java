package de.gamma.libvcs4j.gxl.export.util;

/**
 * Enables easy checking of function parameters.
 */
public class Check {

    /**
     * Checks if a given Object is not null and throws an Exception
     * if it is null.
     * @param obj The given Object.
     * @param message The message for the thrown Exception.
     */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Checks if a given String is not null and not empty and throws an Exception
     * if it is null or empty.
     * @param obj The given String.
     * @param message The message for the thrown Exception.
     */
    public static void notNullOrEmpty(String obj, String message) {
        notNull(obj, message);
        if (obj.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
