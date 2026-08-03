package com.exemptenjoy.system.mapper;
import java.util.List;
import com.exemptenjoy.system.domain.EeMessage;
public interface EeMessageMapper {
    public EeMessage selectEeMessageByMessageId(Long messageId);
    public List<EeMessage> selectEeMessageList(EeMessage eeMessage);
    public int insertEeMessage(EeMessage eeMessage);
    public int updateEeMessage(EeMessage eeMessage);
    public int deleteEeMessageByMessageIds(Long[] messageIds);
    public int markAllRead(Long userId);
    public int selectUnreadCount(Long userId);
}