package com.exemptenjoy.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.exemptenjoy.system.domain.EeMessage;
import com.exemptenjoy.system.mapper.EeMessageMapper;
import com.exemptenjoy.system.service.IEeMessageService;

@Service
public class EeMessageServiceImpl implements IEeMessageService {

    @Autowired
    private EeMessageMapper eeMessageMapper;

    @Override
    public EeMessage selectEeMessageByMessageId(Long messageId) {
        return eeMessageMapper.selectEeMessageByMessageId(messageId);
    }

    @Override
    public List<EeMessage> selectEeMessageList(EeMessage eeMessage) {
        return eeMessageMapper.selectEeMessageList(eeMessage);
    }

    @Override
    public int insertEeMessage(EeMessage eeMessage) {
        return eeMessageMapper.insertEeMessage(eeMessage);
    }

    @Override
    public int updateEeMessage(EeMessage eeMessage) {
        return eeMessageMapper.updateEeMessage(eeMessage);
    }

    @Override
    public int deleteEeMessageByMessageIds(Long[] messageIds) {
        return eeMessageMapper.deleteEeMessageByMessageIds(messageIds);
    }

    @Override
    public int markAllRead(Long userId) {
        return eeMessageMapper.markAllRead(userId);
    }

    @Override
    public int selectUnreadCount(Long userId) {
        return eeMessageMapper.selectUnreadCount(userId);
    }

    @Override
    public void sendNotification(Long userId, String title, String content, String msgType, Long relatedId, String relatedType) {
        EeMessage msg = new EeMessage();
        msg.setUserId(userId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setMsgType(msgType);
        msg.setRelatedId(relatedId);
        msg.setRelatedType(relatedType);
        eeMessageMapper.insertEeMessage(msg);
    }
}