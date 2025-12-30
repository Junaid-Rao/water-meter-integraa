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

        // Replace all non-checksum parameters with proper value transformation
        for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String placeholder = "{" + key + "}";

            // Skip checksum placeholder
            if (key.equals("CHK") || key.equals("chk")) {
                continue;
            }

            // Get value type from parameter definition
            String valueType = "equal";
            if (parameterDefinitions != null && parameterDefinitions.containsKey(key)) {
                Parameter param = parameterDefinitions.get(key);
                if (param.getValue() != null) {
                    valueType = param.getValue();
                }
            }

            String transformedValue = PayloadTransformer.transformValue(value, valueType);
            result = result.replace(placeholder, transformedValue);
        }

        // Calculate and replace checksum
        Pattern checksumPattern = Pattern.compile("\\{CHK\\}", Pattern.CASE_INSENSITIVE);
        Matcher checksumMatcher = checksumPattern.matcher(result);

        if (checksumMatcher.find()) {
            int checksumIndex = checksumMatcher.start();
            String beforeChecksum = result.substring(0, checksumIndex);
            String checksum = calculateChecksumUseCase.calculate(beforeChecksum);
            result = checksumMatcher.replaceFirst(checksum);
        }

        return result;
    }
}

