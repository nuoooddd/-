package com.exemptenjoy.system.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.exemptenjoy.system.mapper.EeFundMapper;
import com.exemptenjoy.system.domain.EeFund;
import com.exemptenjoy.system.service.IEeFundService;
@Service
public class EeFundServiceImpl implements IEeFundService {
    @Autowired
    private EeFundMapper eeFundMapper;
    
    @Override
    public EeFund selectEeFundByFundId(Long fundId) {
        return eeFundMapper.selectEeFundByFundId(fundId);
    }
    
    @Override
    public List<EeFund> selectEeFundList(EeFund eeFund) {
        return eeFundMapper.selectEeFundList(eeFund);
    }
    
    @Override
    public int insertEeFund(EeFund eeFund) {
        return eeFundMapper.insertEeFund(eeFund);
    }
    
    @Override
    public int updateEeFund(EeFund eeFund) {
        return eeFundMapper.updateEeFund(eeFund);
    }
    
    @Override
    public int deleteEeFundByFundIds(Long[] fundIds) {
        return eeFundMapper.deleteEeFundByFundIds(fundIds);
    }
}
