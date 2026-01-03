# API Integration - TrueBalance

Padrões e boas práticas para integração com a API backend do TrueBalance.

## 🔗 Configuração Base

### Base URL

```typescript
// src/config/api.ts
export const API_CONFIG = {
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
}
```

### Environment Variables

```.env
# Development
VITE_API_URL=http://localhost:8080

# Production
VITE_API_URL=https://api.truebalance.com
```

---

## 📡 Cliente HTTP

### Axios Setup (Recomendado)

```typescript
// src/lib/axios.ts
import axios from 'axios'
import { API_CONFIG } from '@/config/api'

const apiClient = axios.create({
  baseURL: API_CONFIG.baseURL,
  timeout: API_CONFIG.timeout,
  headers: API_CONFIG.headers,
})

// Request Interceptor
apiClient.interceptors.request.use(
  (config) => {
    // Adicionar token se necessário (futuro)
    // const token = localStorage.getItem('token')
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`
    // }
    return config
  },
  (error) => Promise.reject(error)
)

// Response Interceptor
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Tratamento global de erros
    if (error.response?.status === 401) {
      // Redirect para login (futuro)
    }
    return Promise.reject(error)
  }
)

export default apiClient
```

---

## 🗂️ DTOs TypeScript

### Request DTOs

```typescript
// src/types/dtos/bill.dto.ts

export interface BillRequestDTO {
  name: string
  executionDate: string  // ISO 8601: "2025-01-15T14:30:00"
  totalAmount: number
  numberOfInstallments: number
  description?: string
  creditCardId?: number  // Opcional, futuro
}

export interface CreditCardRequestDTO {
  name: string
  creditLimit: number
  closingDay: number      // 1-31
  dueDay: number          // 1-31
  allowsPartialPayment: boolean
}

export interface PartialPaymentRequestDTO {
  amount: number
  description?: string
}
```

### Response DTOs

```typescript
export interface BillResponseDTO {
  id: number
  name: string
  executionDate: string
  totalAmount: number
  numberOfInstallments: number
  installmentAmount: number
  description: string | null
  createdAt: string
  updatedAt: string
}

export interface CreditCardResponseDTO {
  id: number
  name: string
  creditLimit: number
  closingDay: number
  dueDay: number
  allowsPartialPayment: boolean
  createdAt: string
  updatedAt: string
}

export interface InvoiceResponseDTO {
  id: number
  creditCardId: number
  referenceMonth: string  // "2025-01-01"
  totalAmount: number
  previousBalance: number
  closed: boolean
  paid: boolean
  createdAt: string
  updatedAt: string
}

export interface PaginatedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}
```

---

## 🔌 API Services

### Bills Service

```typescript
// src/services/bills.service.ts
import apiClient from '@/lib/axios'
import type { BillRequestDTO, BillResponseDTO, PaginatedResponse } from '@/types/dtos'

export const billsService = {
  // Listar todas as contas
  async getAll(params?: {
    page?: number
    limit?: number
    search?: string
    startDate?: string
    endDate?: string
  }): Promise<PaginatedResponse<BillResponseDTO>> {
    const { data } = await apiClient.get('/bills', { params })
    return data
  },

  // Buscar conta por ID
  async getById(id: number): Promise<BillResponseDTO> {
    const { data } = await apiClient.get(`/bills/${id}`)
    return data
  },

  // Criar nova conta
  async create(bill: BillRequestDTO): Promise<BillResponseDTO> {
    const { data } = await apiClient.post('/bills', bill)
    return data
  },

  // Atualizar conta
  async update(id: number, bill: BillRequestDTO): Promise<BillResponseDTO> {
    const { data } = await apiClient.put(`/bills/${id}`, bill)
    return data
  },

  // Deletar conta
  async delete(id: number): Promise<void> {
    await apiClient.delete(`/bills/${id}`)
  },
}
```

### Credit Cards Service

```typescript
// src/services/creditCards.service.ts
import apiClient from '@/lib/axios'
import type { CreditCardRequestDTO, CreditCardResponseDTO } from '@/types/dtos'

export const creditCardsService = {
  async getAll(): Promise<CreditCardResponseDTO[]> {
    const { data } = await apiClient.get('/credit-cards')
    return data
  },

  async getById(id: number): Promise<CreditCardResponseDTO> {
    const { data } = await apiClient.get(`/credit-cards/${id}`)
    return data
  },

  async getAvailableLimit(id: number) {
    const { data } = await apiClient.get(`/credit-cards/${id}/available-limit`)
    return data
  },

  async create(card: CreditCardRequestDTO): Promise<CreditCardResponseDTO> {
    const { data } = await apiClient.post('/credit-cards', card)
    return data
  },

  async update(id: number, card: CreditCardRequestDTO): Promise<CreditCardResponseDTO> {
    const { data } = await apiClient.put(`/credit-cards/${id}`, card)
    return data
  },

  async delete(id: number): Promise<void> {
    await apiClient.delete(`/credit-cards/${id}`)
  },
}
```

---

## 🎣 React Query (TanStack Query)

### Setup

```bash
npm install @tanstack/react-query @tanstack/react-query-devtools
```

```typescript
// src/lib/queryClient.ts
import { QueryClient } from '@tanstack/react-query'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 minutos
      cacheTime: 1000 * 60 * 10, // 10 minutos
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
})
```

```tsx
// src/main.tsx
import { QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import { queryClient } from './lib/queryClient'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  </React.StrictMode>,
)
```

### Custom Hooks

```typescript
// src/hooks/useBills.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { billsService } from '@/services/bills.service'
import type { BillRequestDTO } from '@/types/dtos'

// Query: Listar contas
export function useBills(params?: {
  page?: number
  limit?: number
  search?: string
}) {
  return useQuery({
    queryKey: ['bills', params],
    queryFn: () => billsService.getAll(params),
  })
}

// Query: Buscar conta por ID
export function useBill(id: number) {
  return useQuery({
    queryKey: ['bills', id],
    queryFn: () => billsService.getById(id),
    enabled: !!id, // Só executa se ID existir
  })
}

// Mutation: Criar conta
export function useCreateBill() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (bill: BillRequestDTO) => billsService.create(bill),
    onSuccess: () => {
      // Invalida cache para recarregar lista
      queryClient.invalidateQueries({ queryKey: ['bills'] })
    },
  })
}

// Mutation: Atualizar conta
export function useUpdateBill() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, bill }: { id: number; bill: BillRequestDTO }) =>
      billsService.update(id, bill),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['bills'] })
      queryClient.invalidateQueries({ queryKey: ['bills', variables.id] })
    },
  })
}

// Mutation: Deletar conta
export function useDeleteBill() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => billsService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['bills'] })
    },
  })
}
```

### Uso em Componentes

```tsx
// src/pages/BillsList.tsx
import { useBills, useDeleteBill } from '@/hooks/useBills'

export function BillsList() {
  const { data, isLoading, error } = useBills({ page: 1, limit: 20 })
  const deleteMutation = useDeleteBill()

  if (isLoading) return <LoadingSpinner />
  if (error) return <ErrorMessage error={error} />

  const handleDelete = async (id: number) => {
    if (confirm('Deletar conta?')) {
      await deleteMutation.mutateAsync(id)
    }
  }

  return (
    <div>
      {data?.content.map(bill => (
        <BillCard
          key={bill.id}
          bill={bill}
          onDelete={() => handleDelete(bill.id)}
        />
      ))}
    </div>
  )
}
```

---

## 🎯 Estados e Loading

### Loading States

```tsx
function BillsList() {
  const { data, isLoading, isFetching, error } = useBills()

  return (
    <div>
      {/* Loading inicial */}
      {isLoading && <Skeleton count={5} />}

      {/* Erro */}
      {error && <ErrorBanner message={error.message} />}

      {/* Dados carregados */}
      {data && (
        <>
          {/* Indicator de background fetching */}
          {isFetching && <RefreshIndicator />}

          {data.content.length === 0 ? (
            <EmptyState message="Nenhuma conta cadastrada" />
          ) : (
            data.content.map(bill => <BillCard key={bill.id} bill={bill} />)
          )}
        </>
      )}
    </div>
  )
}
```

### Optimistic Updates

```typescript
export function useUpdateBill() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, bill }: { id: number; bill: BillRequestDTO }) =>
      billsService.update(id, bill),

    // Atualização otimista
    onMutate: async ({ id, bill }) => {
      // Cancela queries em andamento
      await queryClient.cancelQueries({ queryKey: ['bills', id] })

      // Snapshot do valor anterior
      const previousBill = queryClient.getQueryData(['bills', id])

      // Atualiza otimisticamente
      queryClient.setQueryData(['bills', id], bill)

      // Retorna contexto para rollback
      return { previousBill }
    },

    // Rollback em caso de erro
    onError: (err, variables, context) => {
      if (context?.previousBill) {
        queryClient.setQueryData(['bills', variables.id], context.previousBill)
      }
    },

    // Sempre revalida no final
    onSettled: (_, __, variables) => {
      queryClient.invalidateQueries({ queryKey: ['bills', variables.id] })
    },
  })
}
```

---

## ⚠️ Tratamento de Erros

### Error Types

```typescript
// src/types/errors.ts
export interface APIError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
}

export class APIException extends Error {
  status: number
  error: string
  path: string

  constructor(apiError: APIError) {
    super(apiError.message)
    this.status = apiError.status
    this.error = apiError.error
    this.path = apiError.path
  }
}
```

### Error Handling Component

```tsx
// src/components/ErrorBoundary.tsx
import { useQueryErrorResetBoundary } from '@tanstack/react-query'
import { ErrorBoundary as ReactErrorBoundary } from 'react-error-boundary'

function ErrorFallback({ error, resetErrorBoundary }: any) {
  return (
    <div className="flex flex-col items-center justify-center min-h-[400px]">
      <h2 className="text-2xl font-bold mb-4">Algo deu errado</h2>
      <p className="text-gray-600 dark:text-slate-400 mb-4">
        {error.message}
      </p>
      <button onClick={resetErrorBoundary} className="btn-primary">
        Tentar novamente
      </button>
    </div>
  )
}

export function ErrorBoundary({ children }: { children: React.ReactNode }) {
  const { reset } = useQueryErrorResetBoundary()

  return (
    <ReactErrorBoundary onReset={reset} FallbackComponent={ErrorFallback}>
      {children}
    </ReactErrorBoundary>
  )
}
```

---

## 🔄 Retry Strategies

```typescript
// Retry com backoff exponencial
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
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
```

---

## 📚 Referências

- [TanStack Query Documentation](https://tanstack.com/query/latest)
- [Axios Documentation](https://axios-http.com/docs/intro)
- [Backend API Documentation](../truebalance/docs/api-doc.md)

---

**Última atualização**: Dezembro 2025
