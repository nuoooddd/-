package com.exemptenjoy.system.service;
import java.util.List;
import com.exemptenjoy.system.domain.EeMessage;
public interface IEeMessageService {
    public EeMessage selectEeMessageByMessageId(Long messageId);
    public List<EeMessage> selectEeMessageList(EeMessage eeMessage);
    public int insertEeMessage(EeMessage eeMessage);
    public int updateEeMessage(EeMessage eeMessage);
    public int deleteEeMessageByMessageIds(Long[] messageIds);
    public int markAllRead(Long userId);
    public int selectUnreadCount(Long userId);
    public void sendNotification(Long userId, String title, String content, String msgType, Long relatedId, String relatedType);
}