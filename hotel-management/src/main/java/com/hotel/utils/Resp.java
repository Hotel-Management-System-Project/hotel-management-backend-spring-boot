package com.hotel.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Resp<T> {

    public static enum Status {
        success, error
    }

    private Status status;
    private T data;          // ✅ FIX: use T instead of Object
    private String message;

    // ✅ success with data only
    public static <T> Resp<T> success(T data) {
        return new Resp<T>(Status.success, data, null);
    }

    // ✅ success with data + message
    public static <T> Resp<T> success(T data, String message) {
        return new Resp<T>(Status.success, data, message);
    }

    // ✅ success with message only
    public static <T> Resp<T> successMessage(String message) {
        return new Resp<T>(Status.success, null, message);
    }

    // ❌ error
    public static <T> Resp<T> error(String message) {
        return new Resp<T>(Status.error, null, message);
    }
}