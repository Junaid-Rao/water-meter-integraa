package com.example.integraa_android_junaid.domain.model;

import java.util.Map;

public class Command {
    private String key;
    private String label;
    private String payload;
    private Map<String, Parameter> parameters;

    public Command(String key, String label, String payload, Map<String, Parameter> parameters) {
        this.key = key;
        this.label = label;
        this.payload = payload;
        this.parameters = parameters;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Map<String, Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Parameter> parameters) {
        this.parameters = parameters;
    }
}

