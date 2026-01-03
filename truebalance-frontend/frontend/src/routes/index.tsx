import { lazy, Suspense } from 'react'
import { createBrowserRouter } from 'react-router-dom'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { RootLayout } from '@/layouts/RootLayout'

// Lazy load all page components for code splitting
const Dashboard = lazy(() => import('@/pages/dashboard/Dashboard').then(m => ({ default: m.Dashboard })))
const BillsList = lazy(() => import('@/pages/bills/BillsList').then(m => ({ default: m.BillsList })))
const BillForm = lazy(() => import('@/pages/bills/BillForm').then(m => ({ default: m.BillForm })))
const CreditCardsList = lazy(() => import('@/pages/creditCards/CreditCardsList').then(m => ({ default: m.CreditCardsList })))
const CreditCardForm = lazy(() => import('@/pages/creditCards/CreditCardForm').then(m => ({ default: m.CreditCardForm })))
const InvoicesList = lazy(() => import('@/pages/invoices/InvoicesList').then(m => ({ default: m.InvoicesList })))
const InvoiceDetails = lazy(() => import('@/pages/invoices/InvoiceDetails').then(m => ({ default: m.InvoiceDetails })))
const InvoicePayment = lazy(() => import('@/pages/invoices/InvoicePayment'))
const Reports = lazy(() => import('@/pages/Reports'))
const ConsolidatedView = lazy(() => import('@/pages/ConsolidatedView'))
const NotFound = lazy(() => import('@/pages/NotFound').then(m => ({ default: m.NotFound })))

// Wrapper component for Suspense
function LazyRoute({ children }: { children: React.ReactNode }) {
  return (
    <Suspense
      fallback={
        <div className="flex items-center justify-center min-h-screen">
          <LoadingSpinner size="lg" />
        </div>
      }
    >
      {children}
    </Suspense>
  )
}

export const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    children: [
      {
        index: true,
        element: (
          <LazyRoute>
            <Dashboard />
          </LazyRoute>
        ),
      },
      // Rotas de Contas (Fase 2)
      {
        path: 'bills',
        element: (
          <LazyRoute>
            <BillsList />
          </LazyRoute>
        ),
      },
      {
        path: 'bills/new',
        element: (
          <LazyRoute>
            <BillForm />
          </LazyRoute>
        ),
      },
      {
        path: 'bills/:id/edit',
        element: (
          <LazyRoute>
            <BillForm />
          </LazyRoute>
        ),
      },

      // Rotas de Cartões (Fase 3)
      {
        path: 'credit-cards',
        element: (
          <LazyRoute>
            <CreditCardsList />
          </LazyRoute>
        ),
      },
      {
        path: 'credit-cards/new',
        element: (
          <LazyRoute>
            <CreditCardForm />
          </LazyRoute>
        ),
      },
      {
        path: 'credit-cards/:id/edit',
        element: (
          <LazyRoute>
            <CreditCardForm />
          </LazyRoute>
        ),
      },
      {
        path: 'credit-cards/:creditCardId/invoices',
        element: (
          <LazyRoute>
            <InvoicesList />
          </LazyRoute>
        ),
      },

      // Rotas de Faturas (Fase 3)
      {
        path: 'invoices/:id',
        element: (
          <LazyRoute>
            <InvoiceDetails />
          </LazyRoute>
        ),
      },
      {
        path: 'invoices/:id/payment',
        element: (
          <LazyRoute>
            <InvoicePayment />
          </LazyRoute>
        ),
      },

      // Rotas de Relatórios (Fase 4)
      {
        path: 'reports',
        element: (
          <LazyRoute>
            <Reports />
          </LazyRoute>
        ),
      },

      // Rota de Visão Consolidada (Fase 4)
      {
        path: 'consolidated',
        element: (
          <LazyRoute>
            <ConsolidatedView />
          </LazyRoute>
        ),
      },

      // 404
      {
        path: '*',
        element: (
          <LazyRoute>
            <NotFound />
          </LazyRoute>
        ),
      },
    ],
  },
])
