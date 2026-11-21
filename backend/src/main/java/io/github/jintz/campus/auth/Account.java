package io.github.jintz.campus.auth;

public record Account(long id, String username, Role role) {
  public enum Role {
    STUDENT,
    ADMIN
  }
}
