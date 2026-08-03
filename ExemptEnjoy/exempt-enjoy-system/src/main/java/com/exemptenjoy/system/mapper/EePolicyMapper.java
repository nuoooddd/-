package com.exemptenjoy.system.mapper;
import java.util.List;
import com.exemptenjoy.system.domain.EePolicy;
public interface EePolicyMapper {
    public EePolicy selectEePolicyByPolicyId(Long policyId);
    public List<EePolicy> selectEePolicyList(EePolicy eePolicy);
    public int insertEePolicy(EePolicy eePolicy);
    public int updateEePolicy(EePolicy eePolicy);
    public int deleteEePolicyByPolicyIds(Long[] policyIds);
}
