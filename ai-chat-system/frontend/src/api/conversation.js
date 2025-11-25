import request from '../utils/request'

export function createConversation(data) {
  return request({
    url: '/conversations',
    method: 'post',
    data
  })
}

export function getUserConversations() {
  return request({
    url: '/conversations',
    method: 'get'
  })
}

export function getConversation(id) {
  return request({
    url: `/conversations/${id}`,
    method: 'get'
  })
}

export function deleteConversation(id, permanent = false) {
  return request({
    url: `/conversations/${id}`,
    method: 'delete',
    params: { permanent }
  })
}

export function getDeletedConversations() {
  return request({
    url: '/conversations/deleted',
    method: 'get'
  })
}

export function restoreConversation(id) {
  return request({
    url: `/conversations/${id}/restore`,
    method: 'post'
  })
}

export function emptyRecycleBin() {
  return request({
    url: '/conversations/recycle-bin/empty',
    method: 'delete'
  })
}

export function updateConversationModel(id, selectedModel) {
  return request({
    url: `/conversations/${id}/model`,
    method: 'put',
    data: { selectedModel }
  })
}
