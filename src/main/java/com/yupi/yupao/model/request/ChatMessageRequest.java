package com.yupi.yupao.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 聊天消息请求体
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class ChatMessageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

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
}
