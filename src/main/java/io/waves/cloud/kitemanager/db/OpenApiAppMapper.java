package io.waves.cloud.kitemanager.db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OpenApiAppMapper {

    @Select("select * from open_api_app where appId = #{appId}")
    OpenApiApp getOpenApiAppByAppId(@Param("appId") String appId);

}
