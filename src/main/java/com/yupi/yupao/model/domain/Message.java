package com.yupi.yupao.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息实体
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@TableName(value = "message")
@Data
public class Message implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送者id
     */
    private Long senderId;

    /**
     * 接收者id
     */
    private Long receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型 0-文本 1-图片 2-文件
     */
    private Integer messageType;

    /**
     * 状态 0-未读 1-已读
     */
    private Integer status;

    /**
     * 发送时间
     */
    private Date sendTime;

    /**
     * 阅读时间
     */
    private Date readTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
