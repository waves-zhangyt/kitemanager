/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.db;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * open api mapper
 * @author ytzhang0828@qq.com
 */
@Mapper
public interface OpenApiAppMapper {

    @Select("select * from open_api_app where appId = #{appId}")
    OpenApiApp getOpenApiAppByAppId(@Param("appId") String appId);

    @Select("select * from open_api_app order by id asc")
    List<OpenApiApp> getApps();

    @Insert("insert into open_api_app(appId, secret, uris, createTime, status) " +
            "values(#{appId}, #{secret}, #{uris}, #{createTime}, #{status})")
    int insertOpenApiApp(OpenApiApp openApiApp);

    @Delete("delete from open_api_app where id = #{id}")
    int deleteOpenApiAppById(@Param("id") int id);

}
