import request from '@/utils/request'

// 查询自动匹配及兑现流程列表
export function listEeMatchRecord(query) {
  return request({
    url: '/biz/matchRecord/list',
    method: 'get',
    params: query
  })
}

// 获取自动匹配及兑现流程详细信息
export function getEeMatchRecord(recordId) {
  return request({
    url: '/biz/matchRecord/' + recordId,
    method: 'get'
  })
}

// 新增自动匹配及兑现流程
export function addEeMatchRecord(data) {
  return request({
    url: '/biz/matchRecord',
    method: 'post',
    data: data
  })
}

// 修改自动匹配及兑现流程
export function updateEeMatchRecord(data) {
  return request({
    url: '/biz/matchRecord',
    method: 'put',
    data: data
  })
}

// 删除自动匹配及兑现流程
export function delEeMatchRecord(recordId) {
  return request({
    url: '/biz/matchRecord/' + recordId,
    method: 'delete'
  })
}

// ===================================================
// “免申即享”核心流程节点操作 API
// ===================================================

// 一键执行智能比对匹配计算
export function triggerMatch() {
  return request({
    url: '/biz/matchRecord/triggerMatch',
    method: 'post'
  })
}

// 精准推送（政策找人）
export function pushPolicy(recordId) {
  return request({
    url: '/biz/matchRecord/push/' + recordId,
    method: 'put'
  })
}

// 意愿确认
export function confirmIntention(recordId) {
  return request({
    url: '/biz/matchRecord/confirm/' + recordId,
    method: 'put'
  })
}

// 自动免审拨付（资金直达）
export function fulfillPayment(recordId) {
  return request({
    url: '/biz/matchRecord/fulfill/' + recordId,
    method: 'put'
  })
}

// 公示归档
export function archiveRecord(recordId) {
  return request({
    url: '/biz/matchRecord/archive/' + recordId,
    method: 'put'
  })
}

// 批量推送
export function pushBatch(recordIds) {
  return request({
    url: '/biz/matchRecord/pushBatch',
    method: 'put',
    data: recordIds
  })
}

// 批量归档
export function archiveBatch(recordIds) {
  return request({
    url: '/biz/matchRecord/archiveBatch',
    method: 'put',
    data: recordIds
  })
}

// 人工审核
export function auditRecord(recordId, auditStatus) {
  return request({
    url: '/biz/matchRecord/audit/' + recordId + '?auditStatus=' + auditStatus,
    method: 'put'
  })
}

// 导出Excel
export function exportExcel() {
  return request({
    url: '/biz/matchRecord/export',
    method: 'post',
    responseType: 'blob'
  })
}
