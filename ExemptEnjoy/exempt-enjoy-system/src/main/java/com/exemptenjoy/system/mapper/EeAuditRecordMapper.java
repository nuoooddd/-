package com.exemptenjoy.system.mapper;

import java.util.List;
import com.exemptenjoy.system.domain.EeAuditRecord;
import org.apache.ibatis.annotations.Param;

public interface EeAuditRecordMapper
{
    public int insertEeAuditRecord(EeAuditRecord record);

    public List<EeAuditRecord> selectAuditList(EeAuditRecord record);

    public EeAuditRecord selectAuditById(Long recordId);

    public int updateAuditStatus(EeAuditRecord record);

    public int deleteAuditById(Long recordId);
}