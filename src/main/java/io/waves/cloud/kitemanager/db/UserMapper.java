/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.db;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * user mapper
 * @author ytzhang0828@qq.com
 */
@Mapper
public interface UserMapper {

    @Select("select * from user where name = #{name} and password = #{password}")
    User getUserByNameAndPassword(@Param("name") String name, @Param("password") String password);

    @Update("update user set password = #{password} where name = #{name}")
    int updateUser(User user);

    @Select("select * from user order by id asc")
    List<User> getUsers();

    @Select("select * from user where name = #{name}")
    User getUserByName(String name);

    @Insert("insert into user(name, password, username, createTime, role) values(#{name}, #{password}, " +
            "#{username}, #{createTime}, #{role})")
    int insertUser(User user);

    @Delete("delete from user where id = #{id}")
    int deleteUserById(int id);

}
