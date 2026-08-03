package com.exemptenjoy.system.mapper;
import java.util.List;
import com.exemptenjoy.system.domain.EeMatchRecord;
public interface EeMatchRecordMapper {
    public EeMatchRecord selectEeMatchRecordByRecordId(Long recordId);
    public List<EeMatchRecord> selectEeMatchRecordList(EeMatchRecord eeMatchRecord);
    public int insertEeMatchRecord(EeMatchRecord eeMatchRecord);
    public int updateEeMatchRecord(EeMatchRecord eeMatchRecord);
    public int deleteEeMatchRecordByRecordIds(Long[] recordIds);
}
