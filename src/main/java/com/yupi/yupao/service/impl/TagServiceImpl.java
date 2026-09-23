package com.yupi.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupao.mapper.TagMapper;
import com.yupi.yupao.model.domain.Tag;
import com.yupi.yupao.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
        implements TagService {

    // 标签类型常量
    public static final int TAG_TYPE_TECH = 0;      // 技术栈
    public static final int TAG_TYPE_GRADE = 1;     // 年级
    public static final int TAG_TYPE_GOAL = 2;      // 目标
    public static final int TAG_TYPE_GENDER = 3;    // 性别
    public static final int TAG_TYPE_LEVEL = 4;     // 等级

    // 默认标签数据
    private static final String[][] DEFAULT_TAGS = {
            // 技术栈 (type=0)
            {"Java", "0", "0"},
            {"Python", "0", "0"},
            {"Go", "0", "0"},
            {"前端", "0", "0"},
            {"C++", "0", "0"},
            {"算法", "0", "0"},
            // 年级 (type=1)
            {"大一", "0", "1"},
            {"大二", "0", "1"},
            {"大三", "0", "1"},
            {"大四", "0", "1"},
            {"研一", "0", "1"},
            {"研二", "0", "1"},
            {"研三", "0", "1"},
            {"已就业", "0", "1"},
            {"待业", "0", "1"},
            // 目标 (type=2)
            {"春招", "0", "2"},
            {"秋招", "0", "2"},
            {"考研", "0", "2"},
            {"考公", "0", "2"},
            {"蓝桥杯", "0", "2"},
            {"转行", "0", "2"},
            {"跳槽", "0", "2"},
            // 性别 (type=3)
            {"男", "0", "3"},
            {"女", "0", "3"},
            // 等级 (type=4)
            {"初级", "0", "4"},
            {"中级", "0", "4"},
            {"高级", "0", "4"},
            {"王者", "0", "4"}
    };

    @Override
    public List<Tag> listValidTags() {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isParent", 0);  // 只查询叶子标签
        queryWrapper.eq("isDelete", 0);
        return this.list(queryWrapper);
    }

    @Override
    public List<Tag> listTagsByType(Integer tagType) {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tagType", tagType);
        queryWrapper.eq("isParent", 0);
        queryWrapper.eq("isDelete", 0);
        return this.list(queryWrapper);
    }

    @Override
    public List<String> listAllTagNames() {
        return listValidTags().stream()
                .map(Tag::getTagName)
                .collect(Collectors.toList());
    }

    @Override
    public boolean validateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return true;
        }
        List<String> validTagNames = listAllTagNames();
        return validTagNames.containsAll(tagNames);
    }

    @Override
    @PostConstruct
    public void initDefaultTags() {
        // 系统启动时初始化默认标签
        long count = this.count();
        if (count > 0) {
            log.info("标签数据已存在，跳过初始化");
            return;
        }

        log.info("开始初始化默认标签数据...");
        for (String[] tagData : DEFAULT_TAGS) {
            Tag tag = new Tag();
            tag.setTagName(tagData[0]);
            tag.setIsParent(Integer.parseInt(tagData[1]));
            tag.setTagType(Integer.parseInt(tagData[2]));
            tag.setParentId(0L);
            this.save(tag);
        }
        log.info("默认标签数据初始化完成，共 {} 个", DEFAULT_TAGS.length);
    }
}
