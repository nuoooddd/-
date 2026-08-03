import request from '@/utils/request'

export function login(username, password, code, uuid) {
  const data = {
    username,
    password,
    code,
    uuid
  }
  return request({
    url: '/login',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data: data
  })
}

export function smsLogin(phonenumber, smsCode) {
  const data = {
    loginType: 'sms',
    phonenumber,
    smsCode
  }
  return request({
    url: '/login',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data: data
  })
}

export function getInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

export function getCodeImg() {
  return request({
    url: '/captchaImage',
    headers: {
      isToken: false
    },
    method: 'get',
    timeout: 20000
  })
}

export function register(data) {
  return request({
    url: '/register',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}

export function sendSmsCode(phonenumber) {
  return request({
    url: '/sms/send',
    headers: {
      isToken: false
    },
    method: 'post',
    data: { phonenumber }
  })
}

export function faceLogin(faceImageBase64) {
  return request({
    url: '/login',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data: {
      loginType: 'face',
      faceImageBase64
    }
  })
}

export function faceVerify(imageBase64, idCardNumber, realName) {
  return request({
    url: '/face/verify',
    headers: {
      isToken: false
    },
    method: 'post',
    data: { imageBase64, idCardNumber, realName }
  })
}
