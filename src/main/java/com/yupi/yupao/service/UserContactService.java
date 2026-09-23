package com.yupi.yupao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupao.model.domain.UserContact;
import com.yupi.yupao.model.vo.UserContactVO;

import java.util.List;

/**
 * 用户联系人服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface UserContactService extends IService<UserContact> {

    /**
     * 获取用户联系人列表
     *
     * @param userId 用户id
     * @return 联系人列表
     */
    List<UserContactVO> getUserContacts(Long userId);

    /**
     * 添加联系人
     *
     * @param userId    用户id
     * @param contactId 联系人id
     */
    void addContact(Long userId, Long contactId);

    /**
     * 更新最后消息
     *
     * @param userId      用户id
     * @param contactId   联系人id
     * @param lastMessage 最后消息内容
     */
    void updateLastMessage(Long userId, Long contactId, String lastMessage);

    /**
     * 增加未读消息数
     *
     * @param userId    用户id
     * @param contactId 联系人id
     */
    void incrementUnreadCount(Long userId, Long contactId);

    /**
     * 清除未读消息数
     *
     * @param userId    用户id
     * @param contactId 联系人id
     */
    void clearUnreadCount(Long userId, Long contactId);
}
