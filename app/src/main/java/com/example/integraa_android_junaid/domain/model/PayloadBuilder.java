package com.example.integraa_android_junaid.domain.model;

import com.example.integraa_android_junaid.domain.usecase.CalculateChecksumUseCase;
import com.example.integraa_android_junaid.util.PayloadTransformer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PayloadBuilder {
    private final CalculateChecksumUseCase calculateChecksumUseCase;

    public PayloadBuilder(CalculateChecksumUseCase calculateChecksumUseCase) {
        this.calculateChecksumUseCase = calculateChecksumUseCase;
    }

    /**
     * Build final payload by replacing all placeholders
     * @param payload Original payload with placeholders like {MeterID}, {IP1}, {CHK}
     * @param parameterValues Map of parameter keys to their values
     * @return Final hex payload with all replacements and checksum
     */
    public String buildPayload(String payload, Map<String, String> parameterValues) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }

        String result = payload;

        // First, replace all non-checksum parameters
        for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String placeholder = "{" + key + "}";

            // Skip checksum placeholder - it will be calculated later
            if (key.equals("CHK") || key.equals("chk")) {
                continue;
            }

            // Get the value type from parameter (if available in the map)
            // For now, we'll use "equal" as default, but this should come from the parameter definition
            String valueType = "equal"; // This should be passed from the parameter definition
            String transformedValue = PayloadTransformer.transformValue(value, valueType);
            result = result.replace(placeholder, transformedValue);
        }

        // Find checksum placeholder and calculate
        Pattern checksumPattern = Pattern.compile("\\{CHK\\}", Pattern.CASE_INSENSITIVE);
        Matcher checksumMatcher = checksumPattern.matcher(result);

        if (checksumMatcher.find()) {
            // Get the string before the checksum placeholder
            int checksumIndex = checksumMatcher.start();
            String beforeChecksum = result.substring(0, checksumIndex);

            // Calculate checksum
            String checksum = calculateChecksumUseCase.calculate(beforeChecksum);

            // Replace checksum placeholder
            result = checksumMatcher.replaceFirst(checksum);
        }

        return result;
    }

    /**
     * Build payload with parameter definitions for value type handling
     */
    public String buildPayload(String payload, Map<String, String> parameterValues, Map<String, Parameter> parameterDefinitions) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }

        String result = payload;
        String checksumPlaceholder = null;
        String checksumKey = null;

        // First pass: Identify checksum parameter and replace all non-checksum parameters
        if (parameterDefinitions != null) {
            for (Map.Entry<String, Parameter> paramEntry : parameterDefinitions.entrySet()) {
                String key = paramEntry.getKey();
                Parameter param = paramEntry.getValue();
                
                // Check if this is a checksum parameter by type
                if (param != null && param.getType() != null && "checksum".equalsIgnoreCase(param.getType())) {
                    checksumKey = key;
                    checksumPlaceholder = "{" + key + "}";
                    break; // Found checksum parameter
                }
            }
        }

        // Replace all non-checksum parameters with proper value transformation
        for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String placeholder = "{" + key + "}";

            // Skip checksum placeholder - it will be calculated later
            if (checksumKey != null && key.equals(checksumKey)) {
                continue;
            }
            // Also skip if key is "CHK" or "chk" for backward compatibility
            if (key.equals("CHK") || key.equals("chk")) {
                continue;
            }

            // Get value type from parameter definition
            String valueType = "equal";
            if (parameterDefinitions != null && parameterDefinitions.containsKey(key)) {
                Parameter param = parameterDefinitions.get(key);
                if (param != null && param.getValue() != null) {
                    valueType = param.getValue();
                }
            }

            String transformedValue = PayloadTransformer.transformValue(value, valueType);
            result = result.replace(placeholder, transformedValue);
        }

        // Calculate and replace checksum
        // First try to find checksum by parameter key (dynamic placeholder)
        if (checksumPlaceholder != null && result.contains(checksumPlaceholder)) {
            int checksumIndex = result.indexOf(checksumPlaceholder);
            String beforeChecksum = result.substring(0, checksumIndex);
            String checksum = calculateChecksumUseCase.calculate(beforeChecksum);
            result = result.replace(checksumPlaceholder, checksum);
        } else {
            // Fallback: Look for {CHK} placeholder (backward compatibility)
            Pattern checksumPattern = Pattern.compile("\\{CHK\\}", Pattern.CASE_INSENSITIVE);
            Matcher checksumMatcher = checksumPattern.matcher(result);

            if (checksumMatcher.find()) {
                int checksumIndex = checksumMatcher.start();
                String beforeChecksum = result.substring(0, checksumIndex);
                String checksum = calculateChecksumUseCase.calculate(beforeChecksum);
                result = checksumMatcher.replaceFirst(checksum);
            }
        }

        return result;
    }
}

