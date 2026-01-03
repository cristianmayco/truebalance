import React from 'react'
import ReactDOM from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import './index.css'
import { queryClient } from './lib/queryClient'
import { ThemeProvider } from './contexts/ThemeContext'
import { ToastProvider } from './contexts/ToastContext'
import { DemoProvider } from './contexts/DemoContext'
import { ErrorBoundary } from './components/ErrorBoundary'
import { router } from './routes'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ErrorBoundary>
      <DemoProvider>
        <ThemeProvider>
          <ToastProvider>
            <QueryClientProvider client={queryClient}>
              <RouterProvider router={router} />
              <ReactQueryDevtools initialIsOpen={false} />
            </QueryClientProvider>
          </ToastProvider>
        </ThemeProvider>
      </DemoProvider>
    </ErrorBoundary>
  </React.StrictMode>,
)
