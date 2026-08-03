import request from '@/utils/request'

export function aiAsk(question) {
  return request({ url: '/ai/ask', method: 'post', data: { question } })
}

export function aiStatus() {
  return request({ url: '/ai/status', method: 'get' })
}

export function aiExtract(policyId) {
  return request({ url: '/ai/extract/' + policyId, method: 'post' })
}