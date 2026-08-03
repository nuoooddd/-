package com.exemptenjoy.system.mapper;
import java.util.List;
import com.exemptenjoy.system.domain.EeRule;
public interface EeRuleMapper {
    public EeRule selectEeRuleByRuleId(Long ruleId);
    public List<EeRule> selectEeRuleList(EeRule eeRule);
    public int insertEeRule(EeRule eeRule);
    public int updateEeRule(EeRule eeRule);
    public int deleteEeRuleByRuleIds(Long[] ruleIds);

    /** 按政策ID批量更新规则状态（政策停用/启用级联） */
    public int updateEeRuleStatusByPolicyId(@org.apache.ibatis.annotations.Param("policyId") Long policyId, @org.apache.ibatis.annotations.Param("status") String status);
}