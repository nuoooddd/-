package com.exemptenjoy.system.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.exemptenjoy.system.mapper.EePolicyMapper;
import com.exemptenjoy.system.mapper.EeRuleMapper;
import com.exemptenjoy.system.domain.EePolicy;
import com.exemptenjoy.system.service.IEePolicyService;
@Service
public class EePolicyServiceImpl implements IEePolicyService {
    @Autowired
    private EePolicyMapper eePolicyMapper;

    @Autowired
    private EeRuleMapper eeRuleMapper;
    
    @Override
    public EePolicy selectEePolicyByPolicyId(Long policyId) {
        return eePolicyMapper.selectEePolicyByPolicyId(policyId);
    }
    
    @Override
    public List<EePolicy> selectEePolicyList(EePolicy eePolicy) {
        return eePolicyMapper.selectEePolicyList(eePolicy);
    }
    
    @Override
    public int insertEePolicy(EePolicy eePolicy) {
        return eePolicyMapper.insertEePolicy(eePolicy);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEePolicy(EePolicy eePolicy) {
        int result = eePolicyMapper.updateEePolicy(eePolicy);
        // 级联更新关联规则的状态
        if (result > 0 && eePolicy.getStatus() != null) {
            eeRuleMapper.updateEeRuleStatusByPolicyId(eePolicy.getPolicyId(), eePolicy.getStatus());
        }
        return result;
    }
    
    @Override
    public int deleteEePolicyByPolicyIds(Long[] policyIds) {
        return eePolicyMapper.deleteEePolicyByPolicyIds(policyIds);
    }
}

