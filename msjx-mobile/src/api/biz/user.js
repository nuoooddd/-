import request from '@/utils/request'

export function getUserDashboard() {
  return request({ url: '/biz/user/dashboard', method: 'get' })
}

export function getMyRecords(query) {
  return request({ url: '/biz/user/myRecords', method: 'get', params: query })
}

export function getMyProfile() {
  return request({ url: '/biz/user/myProfile', method: 'get' })
}

export function getMyPolicies() {
  return request({ url: '/biz/user/myPolicies', method: 'get' })
}

export function userConfirm(recordId) {
  return request({ url: '/biz/user/confirm/' + recordId, method: 'put' })
}

export function updateProfile(data) {
  return request({ url: '/biz/user/updateProfile', method: 'put', data: data })
}

export function getAllPolicies() {
  return request({ url: '/biz/user/allPolicies', method: 'get' })
}

export function applyForPush(recordId) {
  return request({ url: '/biz/user/apply/' + recordId, method: 'put' })
}

export function getMatchDetails() {
  return request({ url: '/biz/user/matchDetails', method: 'get' })
}

export function applyPolicy(data) {
  return request({ url: '/biz/user/applyPolicy', method: 'post', data: data })
}
