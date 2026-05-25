import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// تأكد من بقاء الكود هكذا تماماً ليعمل الـ CSS والـ Proxy معاً
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
      },
    },
  },
});