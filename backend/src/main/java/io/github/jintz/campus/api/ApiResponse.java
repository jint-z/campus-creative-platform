package io.github.jintz.campus.api;

public record ApiResponse<T>(String code, String message, T data) {
  public static <T> ApiResponse<T> ok(String message, T data) {
    return new ApiResponse<>("OK", message, data);
  }

  public static ApiResponse<Void> failure(String code, String message) {
    return new ApiResponse<>(code, message, null);
  }
}
