package io.github.jintz.campus.dev;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DevProbeMapper {
  @Select("SELECT 1")
  int selectOne();
}
