package com.example.integraa_android_junaid.data.api.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Parameter implements Serializable {
    @SerializedName("label")
    private String label;

    @SerializedName("type")
    private String type;

    @SerializedName("value")
    private String value;

    @SerializedName("required")
    private String required;

    @SerializedName("min")
    private Integer min;

    @SerializedName("max")
    private Integer max;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }
}

