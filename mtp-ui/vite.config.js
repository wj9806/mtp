import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { Agent } from 'http'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:9091',
        changeOrigin: true,
        agent: new Agent({ keepAlive: true, maxSockets: 50 })
      }
    }
  }
})