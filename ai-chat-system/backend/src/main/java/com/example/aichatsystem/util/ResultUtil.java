package com.example.aichatsystem.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultUtil<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public static <T> ResultUtil<T> success(T data) {
        return new ResultUtil<>(200, "Success", data, System.currentTimeMillis());
    }

    public static <T> ResultUtil<T> success(String message, T data) {
        return new ResultUtil<>(200, message, data, System.currentTimeMillis());
    }

    public static <T> ResultUtil<T> error(Integer code, String message) {
        return new ResultUtil<>(code, message, null, System.currentTimeMillis());
    }

    public static <T> ResultUtil<T> error(String message) {
        return new ResultUtil<>(500, message, null, System.currentTimeMillis());
    }
}
