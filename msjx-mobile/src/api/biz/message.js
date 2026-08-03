import request from '@/utils/request'

export function listEeMessage(query) {
  return request({ url: '/biz/message/list', method: 'get', params: query })
}

export function getEeMessage(messageId) {
  return request({ url: '/biz/message/' + messageId, method: 'get' })
}

export function addEeMessage(data) {
  return request({ url: '/biz/message', method: 'post', data: data })
}

export function sendMessage(data) {
  return request({ url: '/biz/message/send', method: 'post', data: data })
}

export function getMessageUsers() {
  return request({ url: '/biz/message/users', method: 'get' })
}

export function delEeMessage(messageId) {
  return request({ url: '/biz/message/' + messageId, method: 'delete' })
}

export function markRead(messageId) {
  return request({ url: '/biz/message/read/' + messageId, method: 'put' })
}

export function markAllRead() {
  return request({ url: '/biz/message/readAll', method: 'put' })
}

export function getUnreadCount() {
  return request({ url: '/biz/message/unreadCount', method: 'get' })
}
export function markUnread(messageId) {
  return request({ url: '/biz/message/unread/' + messageId, method: 'put' })
}
