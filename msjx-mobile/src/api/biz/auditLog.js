import request from '@/utils/request'

export function listEeAuditLog(query) {
  return request({ url: '/biz/auditLog/list', method: 'get', params: query })
}