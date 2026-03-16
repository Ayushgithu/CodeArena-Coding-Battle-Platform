package com.codearena.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> StandardResponse<T> success(String message, T data) {
        return new StandardResponse<>(true, message, data);
    }

    public static <T> StandardResponse<T> error(String message) {
        return new StandardResponse<>(false, message, null);
    }
}
