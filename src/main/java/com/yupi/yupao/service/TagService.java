package com.yupi.yupao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupao.model.domain.Tag;

import java.util.List;

/**
 * 标签服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface TagService extends IService<Tag> {

    /**
     * 获取所有有效的叶子标签（用户可以选择的）
     *
     * @return 标签列表
     */
    List<Tag> listValidTags();

    /**
     * 根据标签类型获取标签
     *
     * @param tagType 标签类型
     * @return 标签列表
     */
    List<Tag> listTagsByType(Integer tagType);

    /**
     * 获取所有标签名称集合（用于校验）
     *
     * @return 标签名称集合
     */
    List<String> listAllTagNames();

    /**
     * 校验标签是否合法
     *
     * @param tagNames 标签名称列表
     * @return 是否全部合法
     */
    boolean validateTags(List<String> tagNames);

    /**
     * 初始化默认标签数据（系统启动时调用）
     */
    void initDefaultTags();
}
