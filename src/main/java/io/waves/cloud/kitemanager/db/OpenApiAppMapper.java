/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * open api mapper
 * @author ytzhang0828@qq.com
 */
@Mapper
public interface OpenApiAppMapper {

    @Select("select * from open_api_app where appId = #{appId}")
    OpenApiApp getOpenApiAppByAppId(@Param("appId") String appId);

}
