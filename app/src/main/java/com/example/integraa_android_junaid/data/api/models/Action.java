package com.example.integraa_android_junaid.data.api.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Action implements Serializable {
    @SerializedName("label")
    private String label;

    @SerializedName("items")
    private List<Command> items;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Command> getItems() {
        return items;
    }

    public void setItems(List<Command> items) {
        this.items = items;
    }
}

