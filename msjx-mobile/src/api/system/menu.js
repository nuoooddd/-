import request from '@/utils/request'

export function treeselect() {
  return request({
    url: '/system/role/menuTreeselect',
    method: 'get'
  })
}

export function roleMenuTreeselect(roleId) {
  return request({
    url: '/system/role/roleMenuTreeselect/' + roleId,
    method: 'get'
  })
}