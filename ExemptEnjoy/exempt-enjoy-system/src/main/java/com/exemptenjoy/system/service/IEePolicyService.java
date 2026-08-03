package com.exemptenjoy.system.service;
import java.util.List;
import com.exemptenjoy.system.domain.EePolicy;
public interface IEePolicyService {
    public EePolicy selectEePolicyByPolicyId(Long policyId);
    public List<EePolicy> selectEePolicyList(EePolicy eePolicy);
    public int insertEePolicy(EePolicy eePolicy);
    public int updateEePolicy(EePolicy eePolicy);
    public int deleteEePolicyByPolicyIds(Long[] policyIds);
}
