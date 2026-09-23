package com.yupi.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupao.mapper.MessageMapper;
import com.yupi.yupao.model.domain.Message;
import com.yupi.yupao.model.domain.User;
import com.yupi.yupao.model.request.ChatMessageRequest;
import com.yupi.yupao.model.vo.ChatMessageVO;
import com.yupi.yupao.service.ChatService;
import com.yupi.yupao.service.UserContactService;
import com.yupi.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class ChatServiceImpl extends ServiceImpl<MessageMapper, Message>
        implements ChatService {

    @Resource
    private UserContactService userContactService;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageVO sendMessage(User sender, ChatMessageRequest messageRequest) {
        Long receiverId = messageRequest.getReceiverId();
        String content = messageRequest.getContent();

        // 1. 保存消息到数据库
        Message message = new Message();
        message.setSenderId(sender.getId());
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setMessageType(messageRequest.getMessageType() != null ? messageRequest.getMessageType() : 0);
        message.setStatus(0); // 未读
        message.setSendTime(new Date());
        this.save(message);

        // 2. 更新发送者的联系人记录
        userContactService.updateLastMessage(sender.getId(), receiverId, content);

        // 3. 更新接收者的联系人记录（如果不存在则创建）
        userContactService.addContact(receiverId, sender.getId());
        userContactService.updateLastMessage(receiverId, sender.getId(), content);
        userContactService.incrementUnreadCount(receiverId, sender.getId());

        // 4. 构建返回VO
        ChatMessageVO messageVO = new ChatMessageVO();
        BeanUtils.copyProperties(message, messageVO);
        messageVO.setSenderName(sender.getUsername());
        messageVO.setSenderAvatar(sender.getAvatarUrl());

        log.info("用户 {} 向用户 {} 发送消息: {}", sender.getId(), receiverId, content);
        return messageVO;
    }

    @Override
    public List<ChatMessageVO> getChatHistory(Long userId, Long contactId, int pageNum, int pageSize) {
        // 查询双方聊天记录
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.nested(qw -> qw.eq("senderId", userId).eq("receiverId", contactId))
                .or(qw -> qw.eq("senderId", contactId).eq("receiverId", userId))
                .orderByDesc("sendTime");

        Page<Message> page = new Page<>(pageNum, pageSize);
        Page<Message> messagePage = this.page(page, queryWrapper);

        // 转换为VO
        return messagePage.getRecords().stream().map(message -> {
            ChatMessageVO vo = new ChatMessageVO();
            BeanUtils.copyProperties(message, vo);

            // 查询发送者信息
            User sender = userService.getById(message.getSenderId());
            if (sender != null) {
                vo.setSenderName(sender.getUsername());
                vo.setSenderAvatar(sender.getAvatarUrl());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long contactId) {
        // 更新消息状态为已读
        Message message = new Message();
        message.setStatus(1);
        message.setReadTime(new Date());

        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiverId", userId)
                .eq("senderId", contactId)
                .eq("status", 0);

        this.update(message, queryWrapper);

        // 清除未读数
        userContactService.clearUnreadCount(userId, contactId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiverId", userId).eq("status", 0);
        return (int) this.count(queryWrapper);
    }
}
