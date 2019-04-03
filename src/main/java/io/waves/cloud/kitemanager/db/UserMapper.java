/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * user mapper
 * @author ytzhang0828@qq.com
 */
@Mapper
public interface UserMapper {

    @Select("select * from user where name = #{name} and password = #{password}")
    User getUserByNameAndPassword(@Param("name") String name, @Param("password") String password);

}
