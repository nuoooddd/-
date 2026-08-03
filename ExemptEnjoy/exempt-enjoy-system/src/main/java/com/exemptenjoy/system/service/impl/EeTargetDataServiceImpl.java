package com.exemptenjoy.system.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.exemptenjoy.system.mapper.EeTargetDataMapper;
import com.exemptenjoy.system.domain.EeTargetData;
import com.exemptenjoy.system.service.IEeTargetDataService;
@Service
public class EeTargetDataServiceImpl implements IEeTargetDataService {
    @Autowired
    private EeTargetDataMapper eeTargetDataMapper;
    
    @Override
    public EeTargetData selectEeTargetDataByTargetId(Long targetId) {
        return eeTargetDataMapper.selectEeTargetDataByTargetId(targetId);
    }
    
    @Override
    public List<EeTargetData> selectEeTargetDataList(EeTargetData eeTargetData) {
        return eeTargetDataMapper.selectEeTargetDataList(eeTargetData);
    }
    
    @Override
    public int insertEeTargetData(EeTargetData eeTargetData) {
        return eeTargetDataMapper.insertEeTargetData(eeTargetData);
    }
    
    @Override
    public int updateEeTargetData(EeTargetData eeTargetData) {
        return eeTargetDataMapper.updateEeTargetData(eeTargetData);
    }
    
    @Override
    public int deleteEeTargetDataByTargetIds(Long[] targetIds) {
        return eeTargetDataMapper.deleteEeTargetDataByTargetIds(targetIds);
    }
}
