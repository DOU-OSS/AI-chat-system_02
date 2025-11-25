import request from '../utils/request'

export function sendMessage(data) {
  // 深度思考模式需要更长的超时时间（5分钟）
  // 检查是否启用了thinking模式
  const isThinkingMode = data.content && data.content.startsWith('[THINKING_MODE]')
  const timeout = isThinkingMode ? 300000 : 120000 // thinking模式5分钟，普通模式2分钟
  
  return request({
    url: '/chat/send',
    method: 'post',
    data,
    timeout: timeout
  })
}
