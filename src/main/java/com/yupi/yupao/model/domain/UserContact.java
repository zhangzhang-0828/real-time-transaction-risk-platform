package com.yupi.yupao.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户联系人/会话实体
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@TableName(value = "user_contact")
@Data
public class UserContact implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 联系人id
     */
    private Long contactId;

    /**
     * 联系人类型 0-用户 1-群聊
     */
    private Integer contactType;

    /**
     * 备注名
     */
    private String remarkName;

    /**
     * 最后一条消息
     */
    private String lastMessage;

    /**
     * 最后消息时间
     */
    private Date lastTime;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
