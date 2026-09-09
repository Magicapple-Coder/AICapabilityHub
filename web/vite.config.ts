import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from 'node:path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '')
  const gatewayUrl = env.VITE_DEV_GATEWAY_URL || 'http://localhost:8080'
  return {
    plugins: [react(), tailwindcss()],
    resolve: { alias: { '@': path.resolve(__dirname, './src') } },
    server: {
      port: Number(env.VITE_PORT || 5173),
      host: 'localhost',
      proxy: {
        '/api': {
          target: gatewayUrl,
          changeOrigin: true,
        },
      },
    },
  }
})
