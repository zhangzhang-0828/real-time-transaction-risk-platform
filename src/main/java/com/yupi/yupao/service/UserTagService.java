package com.yupi.yupao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupao.model.domain.UserTag;

import java.util.List;

/**
 * 用户标签关联服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface UserTagService extends IService<UserTag> {

    /**
     * 更新用户的标签
     *
     * @param userId   用户id
     * @param tagNames 标签名称列表
     * @return 是否成功
     */
    boolean updateUserTags(Long userId, List<String> tagNames);

    /**
     * 获取用户的标签名称列表
     *
     * @param userId 用户id
     * @return 标签名称列表
     */
    List<String> getUserTagNames(Long userId);

    /**
     * 获取用户的标签id列表
     *
     * @param userId 用户id
     * @return 标签id列表
     */
    List<Long> getUserTagIds(Long userId);
}
