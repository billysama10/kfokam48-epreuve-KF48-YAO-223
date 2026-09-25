import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// Les appels /api sont relayés vers le backend Spring Boot : pas de CORS à gérer.
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
