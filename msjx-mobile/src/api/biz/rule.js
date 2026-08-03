import request from '@/utils/request'
export function listEeRule(query) { return request({ url: '/biz/rule/list', method: 'get', params: query }) }
export function getEeRule(ruleId) { return request({ url: '/biz/rule/' + ruleId, method: 'get' }) }
export function addEeRule(data) { return request({ url: '/biz/rule', method: 'post', data: data }) }
export function updateEeRule(data) { return request({ url: '/biz/rule', method: 'put', data: data }) }
export function delEeRule(ruleId) { return request({ url: '/biz/rule/' + ruleId, method: 'delete' }) }
