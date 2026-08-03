package com.exemptenjoy.system.domain;

import com.exemptenjoy.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public class EeMessage extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long messageId;
    private Long userId;
    private Long senderId;
    private String senderName;
    private String title;
    private String content;
    private String msgType;
    private String isRead;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date readTime;
    private Long relatedId;
    private String relatedType;

    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public Long getMessageId() { return messageId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getUserId() { return userId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public Long getSenderId() { return senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderName() { return senderName; }
    public void setTitle(String title) { this.title = title; }
    public String getTitle() { return title; }
    public void setContent(String content) { this.content = content; }
    public String getContent() { return content; }
    public void setMsgType(String msgType) { this.msgType = msgType; }
    public String getMsgType() { return msgType; }
    public void setIsRead(String isRead) { this.isRead = isRead; }
    public String getIsRead() { return isRead; }
    public void setReadTime(Date readTime) { this.readTime = readTime; }
    public Date getReadTime() { return readTime; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }
    public Long getRelatedId() { return relatedId; }
    public void setRelatedType(String relatedType) { this.relatedType = relatedType; }
    public String getRelatedType() { return relatedType; }
}