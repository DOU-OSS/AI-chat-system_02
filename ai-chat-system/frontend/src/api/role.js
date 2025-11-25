import request from '../utils/request'

export function createRole(data) {
  return request({
    url: '/roles',
    method: 'post',
    data
  })
}

export function getUserRoles() {
  return request({
    url: '/roles/mine',
    method: 'get'
  })
}

export function getPublicRoles() {
  return request({
    url: '/roles/public',
    method: 'get'
  })
}

export function updateRole(id, data) {
  return request({
    url: `/roles/${id}`,
    method: 'put',
    data
  })
}

export function deleteRole(id) {
  return request({
    url: `/roles/${id}`,
    method: 'delete'
  })
}
