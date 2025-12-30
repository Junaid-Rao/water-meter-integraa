package com.example.integraa_android_junaid.data.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PermissionResponse {
    @SerializedName("actions")
    private List<Action> actions;

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }
}

