package com.exemptenjoy.system.mapper;

import java.util.List;
import com.exemptenjoy.system.domain.EePolicyContent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EePolicyContentMapper {
    public EePolicyContent selectByPolicyId(Long policyId);
    public List<EePolicyContent> selectAll();
    public int insert(EePolicyContent content);
    public int updateByPolicyId(EePolicyContent content);
    public int deleteByPolicyId(Long policyId);
}