package com.exemptenjoy.system.service;
import java.util.List;
import com.exemptenjoy.system.domain.EeRule;
public interface IEeRuleService {
    public EeRule selectEeRuleByRuleId(Long ruleId);
    public List<EeRule> selectEeRuleList(EeRule eeRule);
    public int insertEeRule(EeRule eeRule);
    public int updateEeRule(EeRule eeRule);
    public int deleteEeRuleByRuleIds(Long[] ruleIds);
}
