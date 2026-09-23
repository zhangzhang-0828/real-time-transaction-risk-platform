# 数据库初始化
# @author <a href="https://github.com/liyupi">程序员鱼皮</a>
# @from <a href="https://yupi.icu">编程导航知识星球</a>
create
database if not exists yupao;

use
yupao;

-- 用户表
create table user
(
    username     varchar(256) null comment '用户昵称',
    id           bigint auto_increment comment 'id'
        primary key,
    userAccount  varchar(256) null comment '账号',
    avatarUrl    varchar(1024) null comment '用户头像',
    gender       tinyint null comment '性别',
    userPassword varchar(512)       not null comment '密码',
    phone        varchar(128) null comment '电话',
    email        varchar(512) null comment '邮箱',
    userStatus   int      default 0 not null comment '状态 0 - 正常',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete     tinyint  default 0 not null comment '是否删除',
    userRole     int      default 0 not null comment '用户角色 0 - 普通用户 1 - 管理员',
    planetCode   varchar(512) null comment '星球编号',
    tags         varchar(1024) null comment '标签 json 列表'
) comment '用户';

-- 队伍表
create table team
(
    id          bigint auto_increment comment 'id' primary key,
    name        varchar(256)       not null comment '队伍名称',
    description varchar(1024) null comment '描述',
    maxNum      int      default 1 not null comment '最大人数',
    expireTime  datetime null comment '过期时间',
    userId      bigint comment '用户id（队长 id）',
    status      int      default 0 not null comment '0 - 公开，1 - 私有，2 - 加密',
    password    varchar(512) null comment '密码',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete    tinyint  default 0 not null comment '是否删除'
) comment '队伍';

-- 用户队伍关系
create table user_team
(
    id         bigint auto_increment comment 'id'
        primary key,
    userId     bigint comment '用户id',
    teamId     bigint comment '队伍id',
    joinTime   datetime null comment '加入时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0 not null comment '是否删除'
) comment '用户队伍关系';


-- 标签表（启用版本，用于管理合法标签）
create table tag
(
    id          bigint auto_increment comment 'id' primary key,
    tagName     varchar(256) not null comment '标签名称',
    parentId    bigint       default 0 not null comment '父标签 id，0-表示根标签',
    isParent    tinyint      default 0 not null comment '0 - 叶子标签, 1 - 父标签',
    tagType     int          default 0 not null comment '标签类型 0-技术栈 1-年级 2-目标 3-性别 4-等级',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete    tinyint  default 0 not null comment '是否删除',
    constraint uniIdx_tagName unique (tagName)
) comment '系统标签表';

create index idx_parentId on tag (parentId);
create index idx_tagType on tag (tagType);

-- 用户标签关联表（记录用户选择了哪些标签）
create table user_tag
(
    id         bigint auto_increment comment 'id' primary key,
    userId     bigint not null comment '用户id',
    tagId      bigint not null comment '标签id',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    isDelete   tinyint  default 0 not null comment '是否删除',
    constraint uniIdx_user_tag unique (userId, tagId)
) comment '用户标签关联表';

create index idx_userId on user_tag (userId);
create index idx_tagId on user_tag (tagId);

-- 消息表（聊天功能）
create table message
(
    id           bigint auto_increment primary key comment 'id',
    senderId     bigint not null comment '发送者id',
    receiverId   bigint not null comment '接收者id',
    content      text   not null comment '消息内容',
    messageType  int    default 0 comment '0-文本 1-图片 2-文件',
    status       int    default 0 comment '0-未读 1-已读',
    sendTime     datetime default CURRENT_TIMESTAMP comment '发送时间',
    readTime     datetime null comment '阅读时间',
    isDelete     tinyint  default 0 comment '是否删除'
) comment '消息表';

create index idx_sender on message (senderId);
create index idx_receiver on message (receiverId);
create index idx_sendTime on message (sendTime);

-- 用户联系人/会话表
create table user_contact
(
    id          bigint auto_increment primary key comment 'id',
    userId      bigint not null comment '用户id',
    contactId   bigint not null comment '联系人id',
    contactType int    default 0 comment '0-用户 1-群聊',
    remarkName  varchar(256) null comment '备注名',
    lastMessage text null comment '最后一条消息',
    lastTime    datetime null comment '最后消息时间',
    unreadCount int    default 0 comment '未读消息数',
    createTime  datetime default CURRENT_TIMESTAMP comment '创建时间',
    isDelete    tinyint  default 0 comment '是否删除',
    unique key uk_user_contact (userId, contactId)
) comment '联系人/会话表';

create index idx_user_contact_userId on user_contact (userId);
create index idx_user_contact_contactId on user_contact (contactId);