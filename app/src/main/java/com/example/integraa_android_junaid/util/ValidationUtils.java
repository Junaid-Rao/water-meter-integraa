package com.example.integraa_android_junaid.util;

import java.util.regex.Pattern;

public class ValidationUtils {

    public static boolean validateInput(String input, String regex) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        if (regex == null || regex.isEmpty()) {
            return true; // No validation required
        }
        try {
            Pattern pattern = Pattern.compile(regex);
            return pattern.matcher(input).matches();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateInteger(String input, Integer min, Integer max) {
        try {
            int value = Integer.parseInt(input);
            if (min != null && value < min) {
                return false;
            }
            if (max != null && value > max) {
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

