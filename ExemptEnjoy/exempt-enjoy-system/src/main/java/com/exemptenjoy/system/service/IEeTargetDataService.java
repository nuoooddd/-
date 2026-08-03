package com.exemptenjoy.system.service;
import java.util.List;
import com.exemptenjoy.system.domain.EeTargetData;
public interface IEeTargetDataService {
    public EeTargetData selectEeTargetDataByTargetId(Long targetId);
    public List<EeTargetData> selectEeTargetDataList(EeTargetData eeTargetData);
    public int insertEeTargetData(EeTargetData eeTargetData);
    public int updateEeTargetData(EeTargetData eeTargetData);
    public int deleteEeTargetDataByTargetIds(Long[] targetIds);
}
