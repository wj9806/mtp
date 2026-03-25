package com.mtp.config.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtp.config.center.model.vo.UserWithRoles;
import com.mtp.config.center.entity.MtpUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MtpUserMapper extends BaseMapper<MtpUserEntity> {
    List<UserWithRoles> selectUserListWithRoles(@Param("username") String username, @Param("offset") int offset, @Param("limit") int limit);
    long countUserListWithRoles(@Param("username") String username);
}