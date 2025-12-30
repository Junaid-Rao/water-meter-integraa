package com.example.integraa_android_junaid.data.api.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

public class Command implements Serializable {
    @SerializedName("label")
    private String label;

    @SerializedName("payload")
    private String payload;

    @SerializedName("parameters")
    private Map<String, Parameter> parameters;

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

