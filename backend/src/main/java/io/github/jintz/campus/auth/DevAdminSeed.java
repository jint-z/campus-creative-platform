package io.github.jintz.campus.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

@Profile("dev")
@Configuration
class DevAdminSeed {
  @Bean
  ApplicationRunner seedAdmin(
      AccountStore store,
      PasswordEncoder encoder,
      @Value("${app.dev.admin-password:}") String password) {
    return args -> {
      if (password.isEmpty()) {
        return;
      }
      var existing = store.find(AuthController.ADMIN_USERNAME);
      if (existing.isEmpty()) {
        if (!AuthController.validPassword(password)) {
          throw new IllegalArgumentException("Invalid app.dev.admin-password length");
        }
        try {
          store.insert(AuthController.ADMIN_USERNAME, encoder.encode(password), Account.Role.ADMIN);
        } catch (DuplicateKeyException concurrentSeed) {
          verifyAdmin(store);
        }
      } else if (existing.get().account().role() != Account.Role.ADMIN) {
        throw new IllegalStateException("Reserved admin username belongs to a non-admin account");
      }
    };
  }

  private static void verifyAdmin(AccountStore store) {
    if (store
        .find(AuthController.ADMIN_USERNAME)
        .filter(credentials -> credentials.account().role() == Account.Role.ADMIN)
        .isEmpty()) {
      throw new IllegalStateException("Could not initialize reserved admin account");
    }
  }
}
