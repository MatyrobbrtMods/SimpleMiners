package com.matyrobbrt.simpleminers.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

public record JsonBuilder(JsonObject delegate) {
    public JsonBuilder() {
        this(new JsonObject());
    }

    public JsonBuilder add(String key, String value) {
        delegate.addProperty(key, value);
        return this;
    }
    public JsonBuilder add(String key, int value) {
        delegate.addProperty(key, value);
        return this;
    }
    public JsonBuilder add(String key, JsonBuilder jsonBuilder) {
        delegate.add(key, jsonBuilder.delegate);
        return this;
    }

    // TODO support any type
    public JsonBuilder addArray(String key, Object... values) {
        final JsonArray array = new JsonArray();
        for (final var val : values) {
            if (val instanceof Integer integer) {
                array.add(integer);
            }
        }
        delegate.add(key, array);
        return this;
    }
}
