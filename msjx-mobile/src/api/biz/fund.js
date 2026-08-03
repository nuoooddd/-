import request from '@/utils/request'
export function listEeFund(query) { return request({ url: '/biz/fund/list', method: 'get', params: query }) }
export function getEeFund(fundId) { return request({ url: '/biz/fund/' + fundId, method: 'get' }) }
export function addEeFund(data) { return request({ url: '/biz/fund', method: 'post', data: data }) }
export function updateEeFund(data) { return request({ url: '/biz/fund', method: 'put', data: data }) }
export function delEeFund(fundId) { return request({ url: '/biz/fund/' + fundId, method: 'delete' }) }
