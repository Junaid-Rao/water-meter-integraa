package com.example.integraa_android_junaid.domain.model;

public class Parameter {
    private String key;
    private String label;
    private String type;
    private String value;
    private String required;
    private Integer min;
    private Integer max;

    public Parameter(String key, String label, String type, String value, String required, Integer min, Integer max) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.value = value;
        this.required = required;
        this.min = min;
        this.max = max;
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

