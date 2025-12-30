package com.example.integraa_android_junaid.util;

import java.util.Locale;

public class PayloadTransformer {

    /**
     * Transform value based on parameter value type
     * @param input The user input
     * @param valueType The value type (equal, IP, int4)
     * @return Transformed hex string
     */
    public static String transformValue(String input, String valueType) {
        if (input == null || valueType == null) {
            return input;
        }

        switch (valueType.toLowerCase()) {
            case "ip":
                return transformIP(input);
            case "int4":
                return transformInt4(input);
            case "equal":
            default:
                return input;
        }
    }

    /**
     * Transform IP address to 4-byte hex string
     * Example: 021.042.063.084 -> 152A3F54
     */
    private static String transformIP(String ip) {
        try {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid IP format");
            }

            StringBuilder hex = new StringBuilder();
            for (String part : parts) {
                int value = Integer.parseInt(part.trim());
                hex.append(String.format("%02X", value));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid IP address: " + ip, e);
        }
    }

    /**
     * Transform integer to fixed 4-byte hex string (big-endian)
     * Example: 5013 -> 00001395 (5013 in hex is 0x1395, padded to 4 bytes)
     */
    private static String transformInt4(String input) {
        try {
            int value = Integer.parseInt(input);
            // Convert to 4-byte hex (8 hex characters), big-endian (most significant byte first)
            // 5013 = 0x1395 -> 00001395
            return String.format("%08X", value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer: " + input, e);
        }
    }
}

