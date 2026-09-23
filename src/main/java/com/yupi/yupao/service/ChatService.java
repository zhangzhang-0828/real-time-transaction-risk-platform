package com.yupi.yupao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupao.model.domain.Message;
import com.yupi.yupao.model.domain.User;
import com.yupi.yupao.model.request.ChatMessageRequest;
import com.yupi.yupao.model.vo.ChatMessageVO;

import java.util.List;

/**
 * 聊天服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface ChatService extends IService<Message> {

    /**
     * 发送消息
     *
     * @param sender          发送者
     * @param messageRequest  消息请求
     * @return 消息VO
     */
    ChatMessageVO sendMessage(User sender, ChatMessageRequest messageRequest);

    /**
     * 获取聊天记录
     *
     * @param userId     当前用户id
     * @param contactId  联系人id
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @return 消息列表
     */
    List<ChatMessageVO> getChatHistory(Long userId, Long contactId, int pageNum, int pageSize);

    /**
     * 标记消息为已读
     *
     * @param userId     当前用户id
     * @param contactId  联系人id
     */
    void markAsRead(Long userId, Long contactId);

    /**
     * 获取未读消息数
     *
     * @param userId 用户id
     * @return 未读消息数
     */
    int getUnreadCount(Long userId);
}
