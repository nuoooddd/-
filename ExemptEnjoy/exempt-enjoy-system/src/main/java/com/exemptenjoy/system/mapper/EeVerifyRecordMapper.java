package com.exemptenjoy.system.mapper;

import java.util.List;
import com.exemptenjoy.system.domain.EeVerifyRecord;
import org.apache.ibatis.annotations.Param;

public interface EeVerifyRecordMapper
{
    public int insertEeVerifyRecord(EeVerifyRecord record);

    public List<EeVerifyRecord> selectByUserId(@Param("userId") Long userId);

    public List<EeVerifyRecord> selectByVerifyType(@Param("verifyType") String verifyType);
}