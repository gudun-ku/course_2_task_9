package com.elegion.myfirstapplication;

import com.google.gson.JsonObject;

public class ApiError {

    private JsonObject errors;

    public JsonObject getErrors() {
        return errors;
    }

    public void setErrors(JsonObject errors) {
        this.errors = errors;
    }
}
