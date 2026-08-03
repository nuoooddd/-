import request from '@/utils/request'
export function listEePolicy(query) { return request({ url: '/biz/policy/list', method: 'get', params: query }) }
export function getEePolicy(policyId) { return request({ url: '/biz/policy/' + policyId, method: 'get' }) }
export function addEePolicy(data) { return request({ url: '/biz/policy', method: 'post', data: data }) }
export function updateEePolicy(data) { return request({ url: '/biz/policy', method: 'put', data: data }) }
export function delEePolicy(policyId) { return request({ url: '/biz/policy/' + policyId, method: 'delete' }) }
