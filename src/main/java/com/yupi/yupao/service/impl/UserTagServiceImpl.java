package com.yupi.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupao.mapper.UserTagMapper;
import com.yupi.yupao.model.domain.Tag;
import com.yupi.yupao.model.domain.UserTag;
import com.yupi.yupao.service.TagService;
import com.yupi.yupao.service.UserTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户标签关联服务实现类
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class UserTagServiceImpl extends ServiceImpl<UserTagMapper, UserTag>
        implements UserTagService {

    @Resource
    private TagService tagService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserTags(Long userId, List<String> tagNames) {
        if (userId == null || userId <= 0) {
            return false;
        }

        // 1. 删除用户原有的标签关联
        QueryWrapper<UserTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        this.remove(queryWrapper);

        // 2. 如果标签列表为空，直接返回
        if (tagNames == null || tagNames.isEmpty()) {
            return true;
        }

        // 3. 根据标签名称获取标签ID
        List<Tag> allTags = tagService.listValidTags();
        List<UserTag> userTagList = new ArrayList<>();

        for (String tagName : tagNames) {
            Long tagId = allTags.stream()
                    .filter(tag -> tag.getTagName().equals(tagName))
                    .map(Tag::getId)
                    .findFirst()
                    .orElse(null);

            if (tagId != null) {
                UserTag userTag = new UserTag();
                userTag.setUserId(userId);
                userTag.setTagId(tagId);
                userTagList.add(userTag);
            }
        }

        // 4. 批量保存新的标签关联
        if (!userTagList.isEmpty()) {
            return this.saveBatch(userTagList);
        }
        return true;
    }

    @Override
    public List<String> getUserTagNames(Long userId) {
        // 1. 获取用户的标签ID列表
        List<Long> tagIds = getUserTagIds(userId);
        if (tagIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 根据ID获取标签名称
        List<Tag> tags = tagService.listByIds(tagIds);
        return tags.stream()
                .map(Tag::getTagName)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getUserTagIds(Long userId) {
        QueryWrapper<UserTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("isDelete", 0);
        List<UserTag> userTagList = this.list(queryWrapper);
        return userTagList.stream()
                .map(UserTag::getTagId)
                .collect(Collectors.toList());
    }
}
