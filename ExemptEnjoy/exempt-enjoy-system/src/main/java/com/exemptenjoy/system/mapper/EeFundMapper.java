package com.exemptenjoy.system.mapper;
import java.util.List;
import com.exemptenjoy.system.domain.EeFund;
public interface EeFundMapper {
    public EeFund selectEeFundByFundId(Long fundId);
    public List<EeFund> selectEeFundList(EeFund eeFund);
    public int insertEeFund(EeFund eeFund);
    public int updateEeFund(EeFund eeFund);
    public int deleteEeFundByFundIds(Long[] fundIds);
}
