package com.yupi.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupao.mapper.UserContactMapper;
import com.yupi.yupao.model.domain.User;
import com.yupi.yupao.model.domain.UserContact;
import com.yupi.yupao.model.vo.UserContactVO;
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
 * 用户联系人服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact>
        implements UserContactService {

    @Resource
    private UserService userService;

    @Override
    public List<UserContactVO> getUserContacts(Long userId) {
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .orderByDesc("lastTime");

        List<UserContact> contacts = this.list(queryWrapper);

        return contacts.stream().map(contact -> {
            UserContactVO vo = new UserContactVO();
            BeanUtils.copyProperties(contact, vo);

            // 查询联系人信息
            User contactUser = userService.getById(contact.getContactId());
            if (contactUser != null) {
                vo.setContactName(contactUser.getUsername());
                vo.setContactAvatar(contactUser.getAvatarUrl());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addContact(Long userId, Long contactId) {
        // 检查是否已存在
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("contactId", contactId);
        UserContact existContact = this.getOne(queryWrapper);

        if (existContact == null) {
            // 创建新的联系人记录
            UserContact contact = new UserContact();
            contact.setUserId(userId);
            contact.setContactId(contactId);
            contact.setContactType(0);
            contact.setUnreadCount(0);
            contact.setCreateTime(new Date());
            this.save(contact);
            log.info("用户 {} 添加联系人 {}", userId, contactId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastMessage(Long userId, Long contactId, String lastMessage) {
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("contactId", contactId);

        UserContact contact = this.getOne(queryWrapper);
        if (contact != null) {
            contact.setLastMessage(lastMessage);
            contact.setLastTime(new Date());
            this.updateById(contact);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementUnreadCount(Long userId, Long contactId) {
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("contactId", contactId);

        UserContact contact = this.getOne(queryWrapper);
        if (contact != null) {
            contact.setUnreadCount(contact.getUnreadCount() + 1);
            contact.setLastTime(new Date());
            this.updateById(contact);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearUnreadCount(Long userId, Long contactId) {
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("contactId", contactId);

        UserContact contact = this.getOne(queryWrapper);
        if (contact != null) {
            contact.setUnreadCount(0);
            this.updateById(contact);
        }
    }
}
