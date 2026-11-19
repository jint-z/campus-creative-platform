package io.github.jintz.campus.dev;

import io.github.jintz.campus.api.ApiResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequestMapping("/api/dev")
public class DevProbeController {
  private final DevProbeMapper mapper;

  public DevProbeController(DevProbeMapper mapper) {
    this.mapper = mapper;
  }

  @GetMapping("/health")
  public ApiResponse<ServiceStatus> health() {
    return ApiResponse.ok("后端服务正常", new ServiceStatus("UP"));
  }

  @GetMapping("/db")
  public ApiResponse<DatabaseStatus> database() {
    return ApiResponse.ok("数据库查询成功", new DatabaseStatus(mapper.selectOne()));
  }

  public record ServiceStatus(String status) {}

  public record DatabaseStatus(int result) {}
}
