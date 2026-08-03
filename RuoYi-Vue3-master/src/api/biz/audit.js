import request from '@/utils/request'

export function listAudit(query) {
  return request({ url: '/biz/audit/list', method: 'get', params: query })
}

export function getAudit(recordId) {
  return request({ url: '/biz/audit/' + recordId, method: 'get' })
}

export function approveAudit(data) {
  return request({ url: '/biz/audit/approve', method: 'put', data: data })
}

export function rejectAudit(data) {
  return request({ url: '/biz/audit/reject', method: 'put', data: data })
}

export function disableAudit(recordId) {
  return request({ url: '/biz/audit/disable/' + recordId, method: 'put' })
}

export function enableAudit(recordId) {
  return request({ url: '/biz/audit/enable/' + recordId, method: 'put' })
}

export function deleteAudit(recordId) {
  return request({ url: '/biz/audit/' + recordId, method: 'delete' })
}
