package com.exemptenjoy.system.mapper;
import java.util.List;
import com.exemptenjoy.system.domain.EeTargetData;
public interface EeTargetDataMapper {
    public EeTargetData selectEeTargetDataByTargetId(Long targetId);
    public List<EeTargetData> selectEeTargetDataList(EeTargetData eeTargetData);
    public List<EeTargetData> selectEeTargetDataByUserId(Long userId);
    public int insertEeTargetData(EeTargetData eeTargetData);
    public int updateEeTargetData(EeTargetData eeTargetData);
    public int deleteEeTargetDataByTargetIds(Long[] targetIds);
}
