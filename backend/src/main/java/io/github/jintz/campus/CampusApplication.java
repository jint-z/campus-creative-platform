package io.github.jintz.campus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class CampusApplication {
  public static void main(String[] args) {
    SpringApplication.run(CampusApplication.class, args);
  }
}
