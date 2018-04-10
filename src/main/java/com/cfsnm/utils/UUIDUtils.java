package com.cfsnm.utils;

import java.util.UUID;


/**
 * Provides static methods to work with UUIDs (Unique indentifiers).
 *
 * @author Cesar San-Nicolas-Martinez
 */

public class UUIDUtils {

    private UUIDUtils(){}

    /**
     * Generates a random UUID
     * @return new UUID
     */
    public static String generateUUID()
    {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
