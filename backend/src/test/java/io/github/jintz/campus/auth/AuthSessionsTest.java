package io.github.jintz.campus.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class AuthSessionsTest {
  private final Account student = new Account(1, "student", Account.Role.STUDENT);
  private final AtomicLong nanoTime = new AtomicLong();
  private final AuthSessions sessions = new AuthSessions(nanoTime::get);

  @Test
  void tokenExpiresAtEightHoursWithoutSlidingOnUse() {
    String token = sessions.issue(student);
    long expiry = AuthSessions.LIFETIME.toNanos();
    nanoTime.set(expiry - 1);
    assertEquals(student, sessions.account(token));
    nanoTime.set(expiry);
    assertNull(sessions.account(token));
  }

  @Test
  void logoutRevokesOnlyTheCurrentToken() {
    String first = sessions.issue(student);
    String second = sessions.issue(student);
    assertNotEquals(first, second);
    sessions.revoke(first);
    assertNull(sessions.account(first));
    assertEquals(student, sessions.account(second));
  }
}
