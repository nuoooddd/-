package com.exemptenjoy.system.service;

import java.util.List;
import com.exemptenjoy.system.domain.EeMatchRecord;

/**
 * 自动匹配及兑现流程Service接口
 */
public interface IEeMatchRecordService {
    public EeMatchRecord selectEeMatchRecordByRecordId(Long recordId);
    public List<EeMatchRecord> selectEeMatchRecordList(EeMatchRecord eeMatchRecord);
    public int insertEeMatchRecord(EeMatchRecord eeMatchRecord);
    public int updateEeMatchRecord(EeMatchRecord eeMatchRecord);
    public int deleteEeMatchRecordByRecordIds(Long[] recordIds);

    // ==========================================
    // “免申即享”核心流程状态流转方法
    // ==========================================

    /**
     * 触发一键智能比对比对 (政策比对引擎)
     */
    public int triggerMatch();
    public int triggerMatchForTarget(Long targetId);

    /**
     * 推送政策给对应的对象 (精准推送)
     */
    public int pushPolicy(Long recordId);

    /**
     * 确认意愿操作 (意愿确认)
     */
    public int confirmIntention(Long recordId);

    /**
     * 自动资金兑付直达 (自动兑现)
     */
    public int fulfillPayment(Long recordId);

    /**
     * 公示并归档匹配记录 (公示归档)
     */
    public int archiveRecord(Long recordId);

    /**
     * 人工审核操作 (审核通过/拒绝)
     */
    public int auditRecord(Long recordId, String auditStatus);
}
