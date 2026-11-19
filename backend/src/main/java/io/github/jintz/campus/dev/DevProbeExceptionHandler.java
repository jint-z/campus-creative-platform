package io.github.jintz.campus.dev;

import io.github.jintz.campus.api.ApiResponse;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Profile("dev")
@RestControllerAdvice(assignableTypes = DevProbeController.class)
public class DevProbeExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(DevProbeExceptionHandler.class);

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> probeFailure(Exception error) {
    if (isDatabaseConnectionFailure(error)) {
      LOGGER.warn("Development database probe could not connect");
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
          .body(ApiResponse.failure("DB_UNAVAILABLE", "数据库不可用，请检查本机 MySQL 与连接配置"));
    }

    LOGGER.error("Development probe failed", error);
    return ResponseEntity.internalServerError()
        .body(ApiResponse.failure("INTERNAL_ERROR", "验证失败，请查看后端日志"));
  }

  private static boolean isDatabaseConnectionFailure(Throwable error) {
    for (Throwable cause = error; cause != null; cause = cause.getCause()) {
      if (cause instanceof DataAccessResourceFailureException) {
        return true;
      }
      if (cause instanceof SQLException sqlError
          && sqlError.getSQLState() != null
          && sqlError.getSQLState().startsWith("08")) {
        return true;
      }
    }
    return false;
  }
}
