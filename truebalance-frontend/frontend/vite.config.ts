import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'
import { VitePWA } from 'vite-plugin-pwa'
import { visualizer } from 'rollup-plugin-visualizer'

export default defineConfig({
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
    css: true,
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'src/test/',
        '**/*.d.ts',
        '**/*.config.*',
        '**/mockData',
        'dist/'
      ]
    }
  },
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico', 'robots.txt', 'icons/*.png'],
      manifest: {
        name: 'TrueBalance - Gerenciamento Financeiro',
        short_name: 'TrueBalance',
        description: 'Sistema de gerenciamento financeiro pessoal para controle de contas, cartões de crédito e relatórios',
        theme_color: '#9333ea',
        background_color: '#ffffff',
        display: 'standalone',
        orientation: 'portrait-primary',
        start_url: '/',
        scope: '/',
        icons: [
          {
            src: '/icons/icon-72x72.png',
            sizes: '72x72',
            type: 'image/png',
            purpose: 'any maskable'
          },
          {
            src: '/icons/icon-96x96.png',
            sizes: '96x96',
            type: 'image/png',
            purpose: 'any maskable'
          },
          {
            src: '/icons/icon-128x128.png',
            sizes: '128x128',
            type: 'image/png',
            purpose: 'any maskable'
          },
          {
            src: '/icons/icon-144x144.png',
            sizes: '144x144',
            type: 'image/png',
            purpose: 'any maskable'
          },
          {
            src: '/icons/icon-152x152.png',
            sizes: '152x152',
            type: 'image/png',
            purpose: 'any maskable'
          },
          {
            src: '/icons/icon-192x192.png',
            sizes: '192x192',
            type: 'image/png',
            purpose: 'any maskable'
          },
          {
            src: '/icons/icon-384x384.png',
            sizes: '384x384',
            type: 'image/png',
            purpose: 'any maskable'
          },
          {
            src: '/icons/icon-512x512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'any maskable'
          }
        ],
        shortcuts: [
          {
            name: 'Nova Conta',
            short_name: 'Nova Conta',
            description: 'Cadastrar nova conta',
            url: '/bills/new',
            icons: [{ src: '/icons/icon-96x96.png', sizes: '96x96' }]
          },
          {
            name: 'Relatórios',
            short_name: 'Relatórios',
            description: 'Ver relatórios financeiros',
            url: '/reports',
            icons: [{ src: '/icons/icon-96x96.png', sizes: '96x96' }]
          },
          {
            name: 'Visão 360°',
            short_name: '360°',
            description: 'Visão consolidada das finanças',
            url: '/consolidated',
            icons: [{ src: '/icons/icon-96x96.png', sizes: '96x96' }]
          }
        ]
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff,woff2}'],
        runtimeCaching: [
          {
            urlPattern: /^https:\/\/fonts\.googleapis\.com\/.*/i,
            handler: 'CacheFirst',
            options: {
              cacheName: 'google-fonts-cache',
              expiration: {
                maxEntries: 10,
                maxAgeSeconds: 60 * 60 * 24 * 365 // 1 year
              },
              cacheableResponse: {
                statuses: [0, 200]
              }
            }
          },
          {
            urlPattern: /^https:\/\/fonts\.gstatic\.com\/.*/i,
            handler: 'CacheFirst',
            options: {
              cacheName: 'gstatic-fonts-cache',
              expiration: {
                maxEntries: 10,
                maxAgeSeconds: 60 * 60 * 24 * 365 // 1 year
              },
              cacheableResponse: {
                statuses: [0, 200]
              }
            }
          },
          {
            urlPattern: /\/api\/.*/i,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'api-cache',
              expiration: {
                maxEntries: 50,
                maxAgeSeconds: 60 * 5 // 5 minutes
              },
              cacheableResponse: {
                statuses: [0, 200]
              },
              networkTimeoutSeconds: 10
            }
          }
        ],
        cleanupOutdatedCaches: true,
        skipWaiting: true,
        clientsClaim: true
      },
      devOptions: {
        enabled: false // Disable in development for faster HMR
      }
    }),
    // Bundle analyzer - generates stats.html after build
    visualizer({
      filename: './dist/stats.html',
      open: false,
      gzipSize: true,
      brotliSize: true,
      template: 'treemap', // 'treemap', 'sunburst', 'network'
    }) as any
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  build: {
    // Target modern browsers for smaller bundle
    target: 'es2015',

    // Minification settings
    minify: 'esbuild',

    // Source maps (disable in production for smaller bundle)
    sourcemap: false,

    // Rollup options for better code splitting
    rollupOptions: {
      output: {
        // Manual chunk splitting for better caching
        manualChunks: {
          // React and React DOM in separate chunk
          'react-vendor': ['react', 'react-dom', 'react-router-dom'],

          // Framer Motion in separate chunk (large library)
          'framer-motion': ['framer-motion'],

          // Charts library in separate chunk
          'charts': ['recharts'],

          // React Query in separate chunk
          'react-query': ['@tanstack/react-query', '@tanstack/react-query-devtools'],

          // Form libraries in separate chunk
          'forms': ['react-hook-form', 'zod'],

          // Date and utility libraries
          'utils': ['date-fns', 'axios'],
        },

        // Chunk file naming
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: 'assets/[ext]/[name]-[hash].[ext]',
      },
    },

    // Chunk size warning limit (500kb)
    chunkSizeWarningLimit: 500,

    // CSS code splitting
    cssCodeSplit: true,
  },
  server: {
    host: true,
    port: 3000,
    watch: {
      usePolling: true
    }
  }
})
