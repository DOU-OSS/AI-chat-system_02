
/*
* import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
})
* */
// vite.config.js

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
    plugins: [vue()],
    server: {
        proxy: {
            // 这里的 '/api' 是一个标识，代表所有以 '/api' 开头的请求都会被代理
            '/api': {
                // 目标地址，填写你后端服务的实际地址和端口
                // 后端运行在 http://localhost:8082
                target: 'http://localhost:8082',
                // 改变源地址，这在处理跨域问题时非常重要
                changeOrigin: true
                // 不需要路径重写，因为后端控制器本身就使用 /api 前缀
            }
        }
    }
})