package com.yupi.yupao.controller;

import com.yupi.yupao.model.domain.User;
import com.yupi.yupao.model.request.ChatMessageRequest;
import com.yupi.yupao.model.vo.ChatMessageVO;
import com.yupi.yupao.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.util.Map;

import static com.yupi.yupao.constant.UserConstant.USER_LOGIN_STATE;

/**
 * WebSocket 聊天控制器
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Controller
@Slf4j
public class ChatWebSocketController {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    @Resource
    private ChatService chatService;

    /**
     * 处理客户端发送的消息
     * 客户端发送消息到 /app/chat/send
     */
    @MessageMapping("/chat/send")
    public void handleChatMessage(@Payload ChatMessageRequest messageRequest,
                                   @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        // 从 session 中获取当前登录用户
        User loginUser = (User) sessionAttributes.get(USER_LOGIN_STATE);
        if (loginUser == null) {
            log.error("用户未登录，无法发送消息");
            return;
        }

        // 保存消息并获取消息VO
        ChatMessageVO messageVO = chatService.sendMessage(loginUser, messageRequest);

        // 发送给接收者（点对点）
        long receiverId = messageRequest.getReceiverId();
        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiverId),
                "/queue/messages",
                messageVO
        );

        // 同时发送给发送者（确认消息已发送）
        messagingTemplate.convertAndSendToUser(
                String.valueOf(loginUser.getId()),
                "/queue/messages",
                messageVO
        );

        log.info("消息已转发: 从 {} 到 {}", loginUser.getId(), receiverId);
    }
}
