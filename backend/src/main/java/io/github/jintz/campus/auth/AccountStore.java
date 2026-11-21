package io.github.jintz.campus.auth;

import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class AccountStore {
  record Credentials(Account account, String hash) {}

  private final JdbcClient jdbc;

  AccountStore(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  Optional<Credentials> find(String username) {
    return jdbc.sql("SELECT id, username, role, password_hash FROM accounts WHERE username = :name")
        .param("name", username)
        .query(
            (row, index) ->
                new Credentials(
                    new Account(
                        row.getLong("id"),
                        row.getString("username"),
                        Account.Role.valueOf(row.getString("role"))),
                    row.getString("password_hash")))
        .optional();
  }

  void insert(String username, String hash, Account.Role role) {
    jdbc.sql("INSERT INTO accounts (username, password_hash, role) VALUES (:name, :hash, :role)")
        .param("name", username)
        .param("hash", hash)
        .param("role", role.name())
        .update();
  }
}
