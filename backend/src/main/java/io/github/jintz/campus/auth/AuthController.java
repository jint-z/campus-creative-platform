package io.github.jintz.campus.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jintz.campus.api.ApiResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
class AuthController {
  static final String ADMIN_USERNAME = "admin";

  @JsonIgnoreProperties(ignoreUnknown = true)
  record LoginRequest(String username, String password) {}

  record LoginResult(String accessToken, long expiresIn, Account account) {}

  private final AccountStore store;
  private final PasswordEncoder encoder;
  private final AuthSessions sessions;
  private final String unusedHash;

  AuthController(AccountStore store, PasswordEncoder encoder, AuthSessions sessions) {
    this.store = store;
    this.encoder = encoder;
    this.sessions = sessions;
    unusedHash = encoder.encode("unused-password");
  }

  @PostMapping("/student/register")
  ResponseEntity<ApiResponse<Void>> register(@RequestBody LoginRequest request) {
    if (!validUsername(request.username()) || !validPassword(request.password())) {
      return invalidInput();
    }
    String username = normalize(request.username());
    if (ADMIN_USERNAME.equals(username)) {
      return usernameTaken();
    }
    try {
      store.insert(username, encoder.encode(request.password()), Account.Role.STUDENT);
    } catch (DuplicateKeyException duplicate) {
      return usernameTaken();
    }
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("注册成功", null));
  }

  @PostMapping("/student/login")
  ResponseEntity<ApiResponse<LoginResult>> studentLogin(@RequestBody LoginRequest request) {
    return login(request, Account.Role.STUDENT);
  }

  @PostMapping("/admin/login")
  ResponseEntity<ApiResponse<LoginResult>> adminLogin(@RequestBody LoginRequest request) {
    return login(request, Account.Role.ADMIN);
  }

  @PostMapping("/logout")
  ApiResponse<Void> logout(Authentication authentication) {
    sessions.revoke((String) authentication.getCredentials());
    return ApiResponse.ok("已退出登录", null);
  }

  private ResponseEntity<ApiResponse<LoginResult>> login(LoginRequest request, Account.Role role) {
    if (!validUsername(request.username()) || !validPassword(request.password())) {
      return invalidInput();
    }
    var credentials = store.find(normalize(request.username()));
    String hash = credentials.map(AccountStore.Credentials::hash).orElse(unusedHash);
    boolean passwordMatches = encoder.matches(request.password(), hash);
    if (!passwordMatches || credentials.isEmpty() || credentials.get().account().role() != role) {
      return failure(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "用户名或密码错误");
    }
    Account account = credentials.get().account();
    var result =
        new LoginResult(sessions.issue(account), AuthSessions.LIFETIME.toSeconds(), account);
    return ResponseEntity.ok(ApiResponse.ok("登录成功", result));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<ApiResponse<Void>> unreadableRequest() {
    return invalidInput();
  }

  @ExceptionHandler(DataAccessException.class)
  ResponseEntity<ApiResponse<Void>> databaseFailure() {
    return failure(HttpStatus.SERVICE_UNAVAILABLE, "ACCOUNT_STORE_UNAVAILABLE", "账号服务暂不可用");
  }

  static boolean validPassword(String password) {
    return password != null
        && password.codePointCount(0, password.length()) >= 8
        && password.getBytes(StandardCharsets.UTF_8).length <= 72;
  }

  private static boolean validUsername(String username) {
    return username != null && username.matches("[A-Za-z0-9_]{3,32}");
  }

  private static String normalize(String username) {
    return username.toLowerCase(Locale.ROOT);
  }

  private static ResponseEntity<ApiResponse<Void>> usernameTaken() {
    return failure(HttpStatus.CONFLICT, "USERNAME_TAKEN", "用户名不可用");
  }

  private static <T> ResponseEntity<ApiResponse<T>> invalidInput() {
    return failure(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "请检查用户名和密码格式");
  }

  private static <T> ResponseEntity<ApiResponse<T>> failure(
      HttpStatus status, String code, String message) {
    return ResponseEntity.status(status).body(new ApiResponse<>(code, message, null));
  }
}
