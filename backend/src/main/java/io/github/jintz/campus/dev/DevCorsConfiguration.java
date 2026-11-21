package io.github.jintz.campus.dev;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Profile("dev")
@Configuration
public class DevCorsConfiguration implements WebMvcConfigurer {
  private final String adminOrigin;

  public DevCorsConfiguration(
      @Value("${app.dev.admin-origin:http://127.0.0.1:5173}") String adminOrigin) {
    this.adminOrigin = adminOrigin;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/dev/**").allowedOrigins(adminOrigin).allowedMethods("GET");
    registry.addMapping("/api/auth/admin/login").allowedOrigins(adminOrigin).allowedMethods("POST");
    registry
        .addMapping("/api/auth/logout")
        .allowedOrigins(adminOrigin)
        .allowedMethods("POST")
        .allowedHeaders("Authorization", "Content-Type");
    registry
        .addMapping("/api/admin/**")
        .allowedOrigins(adminOrigin)
        .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE")
        .allowedHeaders("Authorization", "Content-Type");
    registry
        .addMapping("/api/media/**")
        .allowedOrigins(adminOrigin)
        .allowedMethods("GET", "HEAD")
        .allowedHeaders("Authorization");
  }
}
