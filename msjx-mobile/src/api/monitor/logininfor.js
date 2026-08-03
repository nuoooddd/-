import request from '@/utils/request'

export function list(query) {
  return request({ url: '/monitor/logininfor/list', method: 'get', params: query })
}

export function delLogininfor(infoIds) {
  return request({ url: '/monitor/logininfor/' + infoIds, method: 'delete' })
}

export function cleanLogininfor() {
  return request({ url: '/monitor/logininfor/clean', method: 'delete' })
}

export function exportLogininfor(query) {
  return request({ url: '/monitor/logininfor/export', method: 'post', params: query, responseType: 'blob' })
}