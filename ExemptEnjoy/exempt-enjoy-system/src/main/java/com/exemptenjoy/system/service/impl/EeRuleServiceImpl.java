package com.exemptenjoy.system.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.exemptenjoy.system.mapper.EeRuleMapper;
import com.exemptenjoy.system.domain.EeRule;
import com.exemptenjoy.system.service.IEeRuleService;
@Service
public class EeRuleServiceImpl implements IEeRuleService {
    @Autowired
    private EeRuleMapper eeRuleMapper;
    
    @Override
    public EeRule selectEeRuleByRuleId(Long ruleId) {
        return eeRuleMapper.selectEeRuleByRuleId(ruleId);
    }
    
    @Override
    public List<EeRule> selectEeRuleList(EeRule eeRule) {
        return eeRuleMapper.selectEeRuleList(eeRule);
    }
    
    @Override
    public int insertEeRule(EeRule eeRule) {
        return eeRuleMapper.insertEeRule(eeRule);
    }
    
    @Override
    public int updateEeRule(EeRule eeRule) {
        return eeRuleMapper.updateEeRule(eeRule);
    }
    
    @Override
    public int deleteEeRuleByRuleIds(Long[] ruleIds) {
        return eeRuleMapper.deleteEeRuleByRuleIds(ruleIds);
    }
}
