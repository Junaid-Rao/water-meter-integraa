package com.example.integraa_android_junaid.domain.usecase;

public class CalculateChecksumUseCase {

    /**
     * Calculate CheckSum8 Modulo 256
     * Sum all bytes before the checksum placeholder and take modulo 256
     * @param hexString Hex string without the checksum placeholder
     * @return 2-character hex string representing the checksum
     */
    public String calculate(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return "00";
        }

        // Remove any whitespace
        hexString = hexString.replaceAll("\\s", "");

        // Convert hex string to bytes and sum
        int sum = 0;
        for (int i = 0; i < hexString.length(); i += 2) {
            if (i + 1 < hexString.length()) {
                String hexByte = hexString.substring(i, i + 2);
                int byteValue = Integer.parseInt(hexByte, 16);
                sum += byteValue;
            }
        }

        // Modulo 256
        int checksum = sum % 256;

        // Return as 2-character hex string (uppercase)
        return String.format("%02X", checksum);
    }
}

