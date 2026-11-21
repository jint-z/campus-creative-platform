package io.github.jintz.campus.auth;

import static org.springframework.security.config.Customizer.withDefaults;

import io.github.jintz.campus.api.ApiResponse;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

@Configuration
class SecurityConfiguration {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecurityFilterChain apiSecurity(HttpSecurity http, AuthSessions sessions, JsonMapper json)
      throws Exception {
    return http.cors(withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .requestCache(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            requests ->
                requests
                    .dispatcherTypeMatchers(DispatcherType.ERROR)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/dev/health", "/api/dev/db")
                    .permitAll()
                    .requestMatchers(HttpMethod.HEAD, "/api/dev/health", "/api/dev/db")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/auth/student/register",
                        "/api/auth/student/login",
                        "/api/auth/admin/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/logout")
                    .hasAnyRole("STUDENT", "ADMIN")
                    .requestMatchers("/api/student/**")
                    .hasRole("STUDENT")
                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/media/**")
                    .hasAnyRole("STUDENT", "ADMIN")
                    .requestMatchers(HttpMethod.HEAD, "/api/media/**")
                    .hasAnyRole("STUDENT", "ADMIN")
                    .anyRequest()
                    .denyAll())
        .exceptionHandling(
            errors ->
                errors
                    .authenticationEntryPoint(
                        (request, response, exception) -> {
                          response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
                          writeError(
                              json, response, 401, ApiResponse.failure("UNAUTHENTICATED", "请先登录"));
                        })
                    .accessDeniedHandler(
                        (request, response, exception) ->
                            writeError(
                                json,
                                response,
                                403,
                                ApiResponse.failure("FORBIDDEN", "当前账号无权执行此操作"))))
        .addFilterBefore(new BearerFilter(sessions), AnonymousAuthenticationFilter.class)
        .build();
  }

  private static void writeError(
      JsonMapper json, HttpServletResponse response, int status, ApiResponse<Void> body)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    json.writeValue(response.getOutputStream(), body);
  }

  private static class BearerFilter extends OncePerRequestFilter {
    private final AuthSessions sessions;

    BearerFilter(AuthSessions sessions) {
      this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
      String header = request.getHeader(HttpHeaders.AUTHORIZATION);
      if (header != null
          && header.length() == 50
          && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
        String token = header.substring(7);
        Account account = sessions.account(token);
        if (account != null) {
          var context = SecurityContextHolder.createEmptyContext();
          var authority = new SimpleGrantedAuthority("ROLE_" + account.role().name());
          context.setAuthentication(
              UsernamePasswordAuthenticationToken.authenticated(
                  account, token, List.of(authority)));
          SecurityContextHolder.setContext(context);
        }
      }
      chain.doFilter(request, response);
    }
  }
}
