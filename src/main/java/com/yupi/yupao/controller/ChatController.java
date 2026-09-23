package com.yupi.yupao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.yupao.common.BaseResponse;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.common.ResultUtils;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.User;
import com.yupi.yupao.model.vo.ChatMessageVO;
import com.yupi.yupao.model.vo.UserContactVO;
import com.yupi.yupao.service.ChatService;
import com.yupi.yupao.service.UserContactService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.yupi.yupao.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 聊天 HTTP 接口控制器
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {

    @Resource
    private ChatService chatService;

    @Resource
    private UserContactService userContactService;

    /**
     * 获取聊天记录
     *
     * @param contactId 联系人id
     * @param pageNum   页码（默认1）
     * @param pageSize  每页大小（默认20）
     * @param request   HTTP请求
     * @return 聊天记录列表
     */
    @GetMapping("/history/{contactId}")
    public BaseResponse<List<ChatMessageVO>> getChatHistory(
            @PathVariable Long contactId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        if (contactId == null || contactId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "联系人id错误");
        }
        List<ChatMessageVO> history = chatService.getChatHistory(loginUser.getId(), contactId, pageNum, pageSize);
        return ResultUtils.success(history);
    }

    /**
     * 获取联系人列表
     *
     * @param request HTTP请求
     * @return 联系人列表
     */
    @GetMapping("/contacts")
    public BaseResponse<List<UserContactVO>> getContacts(HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        List<UserContactVO> contacts = userContactService.getUserContacts(loginUser.getId());
        return ResultUtils.success(contacts);
    }

    /**
     * 标记消息为已读
     *
     * @param contactId 联系人id
     * @param request   HTTP请求
     * @return 是否成功
     */
    @PostMapping("/read/{contactId}")
    public BaseResponse<Boolean> markAsRead(@PathVariable Long contactId, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        if (contactId == null || contactId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "联系人id错误");
        }
        chatService.markAsRead(loginUser.getId(), contactId);
        return ResultUtils.success(true);
    }

    /**
     * 获取未读消息总数
     *
     * @param request HTTP请求
     * @return 未读消息数
     */
    @GetMapping("/unread/count")
    public BaseResponse<Integer> getUnreadCount(HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        int count = chatService.getUnreadCount(loginUser.getId());
        return ResultUtils.success(count);
    }

    /**
     * 获取当前登录用户
     */
    private User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User) userObj;
    }
}
