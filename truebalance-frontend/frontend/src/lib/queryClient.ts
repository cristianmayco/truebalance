import { QueryClient } from '@tanstack/react-query'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 minutos
      gcTime: 1000 * 60 * 10, // 10 minutos (anteriormente cacheTime)
      refetchOnWindowFocus: false,
      retry: (failureCount, error: any) => {
        // Não retry em erros 4xx (exceto 429)
        if (error.response?.status >= 400 && error.response?.status < 500) {
          if (error.response.status === 429) return failureCount < 3
          return false
        }
        // Retry até 3 vezes em erros 5xx
        return failureCount < 3
      },
      retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
    },
  },
})
