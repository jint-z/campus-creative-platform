package io.github.jintz.campus.auth;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
class AuthSessions {
  static final Duration LIFETIME = Duration.ofHours(8);
  private final SecureRandom random = new SecureRandom();
  private final Cache<String, Account> accounts;

  AuthSessions() {
    this(Ticker.systemTicker());
  }

  AuthSessions(Ticker ticker) {
    accounts =
        Caffeine.newBuilder().ticker(ticker).maximumSize(10000).expireAfterWrite(LIFETIME).build();
  }

  String issue(Account account) {
    byte[] randomBytes = new byte[32];
    random.nextBytes(randomBytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    accounts.put(token, account);
    return token;
  }

  Account account(String token) {
    return accounts.getIfPresent(token);
  }

  void revoke(String token) {
    accounts.invalidate(token);
  }
}
