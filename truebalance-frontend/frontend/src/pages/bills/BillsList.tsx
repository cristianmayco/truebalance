import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Plus } from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import { useBills } from '@/hooks/useBills'
import { Button } from '@/components/ui/Button'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { EmptyState } from '@/components/ui/EmptyState'
import { Pagination } from '@/components/ui/Pagination'
import { BillsTable } from '@/components/bills/BillsTable'
import { BillCard } from '@/components/bills/BillCard'
import { BillFilters } from '@/components/bills/BillFilters'
import { ImportExport } from '@/components/ui/ImportExport'
import type { BillFiltersDTO, BillResponseDTO } from '@/types/dtos/bill.dto'

export function BillsList() {
  const navigate = useNavigate()
  const [filters, setFilters] = useState<BillFiltersDTO>({ page: 0, size: 10 })

  const { data, isLoading, error } = useBills(filters)

  const handlePageChange = (page: number) => {
    setFilters((prev) => ({ ...prev, page }))
  }

  const handleFilterChange = (newFilters: BillFiltersDTO) => {
    setFilters((prev) => ({ ...prev, ...newFilters, page: 0 }))
  }

  const handleEdit = (bill: BillResponseDTO) => {
    navigate(`/bills/${bill.id}/edit`)
  }

  const handleNew = () => {
    navigate('/bills/new')
  }

  if (isLoading) {
    return (
      <AppShell title="Contas">
        <LoadingSpinner fullScreen />
      </AppShell>
    )
  }

  if (error) {
    return (
      <AppShell title="Contas">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <p className="text-red-600 dark:text-red-400 mb-4">
              Erro ao carregar contas: {(error as any).message}
            </p>
            <Button onClick={() => window.location.reload()}>Tentar novamente</Button>
          </div>
        </div>
      </AppShell>
    )
  }

  const bills = data?.content || []
  const isEmpty = bills.length === 0

  return (
    <AppShell title="Contas">
      <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Contas</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">
            Gerencie suas contas e parcelas
          </p>
        </div>
        <div className="flex items-center gap-3">
          <ImportExport />
          <Button onClick={handleNew}>
            <Plus className="w-4 h-4" />
            Nova Conta
          </Button>
        </div>
      </div>

      {/* Filters */}
      <BillFilters onFilterChange={handleFilterChange} initialFilters={filters} />

      {/* Content */}
      {isEmpty ? (
        <EmptyState
          icon={<Plus className="w-12 h-12" />}
          message="Nenhuma conta encontrada"
          description="Comece criando sua primeira conta para gerenciar suas finanças."
          actionLabel="Nova Conta"
          onAction={handleNew}
        />
      ) : (
        <>
          {/* Desktop Table */}
          <BillsTable bills={bills} onEdit={handleEdit} />

          {/* Mobile Cards */}
          <div className="lg:hidden grid gap-4">
            {bills.map((bill) => (
              <BillCard key={bill.id} bill={bill} onEdit={handleEdit} />
            ))}
          </div>

          {/* Pagination */}
          {data && data.totalPages > 1 && (
            <Pagination
              currentPage={data.page}
              totalPages={data.totalPages}
              onPageChange={handlePageChange}
            />
          )}
        </>
      )}
      </div>
    </AppShell>
  )
}
