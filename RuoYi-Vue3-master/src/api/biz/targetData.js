import request from '@/utils/request'
export function listEeTargetData(query) { return request({ url: '/biz/targetData/list', method: 'get', params: query }) }
export function getEeTargetData(targetId) { return request({ url: '/biz/targetData/' + targetId, method: 'get' }) }
export function addEeTargetData(data) { return request({ url: '/biz/targetData', method: 'post', data: data }) }
export function updateEeTargetData(data) { return request({ url: '/biz/targetData', method: 'put', data: data }) }
export function delEeTargetData(targetId) { return request({ url: '/biz/targetData/' + targetId, method: 'delete' }) }
