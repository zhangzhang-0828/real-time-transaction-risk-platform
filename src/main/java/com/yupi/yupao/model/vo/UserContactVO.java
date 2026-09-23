package com.yupi.yupao.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户联系人/会话响应体
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class UserContactVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 联系人关系id
     */
    private Long id;

    /**
     * 联系人用户id
     */
    private Long contactId;

    /**
     * 联系人昵称
     */
    private String contactName;

    /**
     * 联系人头像
     */
    private String contactAvatar;

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
}
