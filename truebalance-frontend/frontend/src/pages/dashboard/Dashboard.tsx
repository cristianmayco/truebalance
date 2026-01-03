import { useNavigate } from 'react-router-dom'
import { Plus, DollarSign, Calendar, TrendingUp } from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import { useBills } from '@/hooks/useBills'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { EmptyState } from '@/components/ui/EmptyState'
import { BillCard } from '@/components/bills/BillCard'
import { formatCurrency } from '@/utils/currency'
import type { BillResponseDTO } from '@/types/dtos/bill.dto'

export function Dashboard() {
  const navigate = useNavigate()
  const { data, isLoading } = useBills({ page: 0, size: 5, sort: 'date,desc' })

  const bills = data?.content || []

  // Calculate summary metrics
  const totalAmount = bills.reduce((sum, bill) => sum + bill.totalAmount, 0)
  const totalBills = data?.totalElements || 0

  // Get current month bills
  const currentDate = new Date()
  const currentMonth = currentDate.getMonth()
  const currentYear = currentDate.getFullYear()

  const currentMonthBills = bills.filter((bill) => {
    const billDateStr = bill.date || bill.executionDate || bill.billDate;
    if (!billDateStr) return false;
    const billDate = new Date(billDateStr)
    return billDate.getMonth() === currentMonth && billDate.getFullYear() === currentYear
  })

  const currentMonthTotal = currentMonthBills.reduce((sum, bill) => sum + bill.totalAmount, 0)

  const handleEdit = (bill: BillResponseDTO) => {
    navigate(`/bills/${bill.id}/edit`)
  }

  const handleNewBill = () => {
    navigate('/bills/new')
  }

  const handleViewAll = () => {
    navigate('/bills')
  }

  if (isLoading) {
    return (
      <AppShell title="Dashboard">
        <LoadingSpinner fullScreen />
      </AppShell>
    )
  }

  return (
    <AppShell title="Dashboard">
      <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Dashboard</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">
            Visão geral das suas finanças
          </p>
        </div>
        <Button onClick={handleNewBill}>
          <Plus className="w-4 h-4" />
          Nova Conta
        </Button>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card>
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-full bg-violet-100 dark:bg-violet-900/20 flex items-center justify-center">
              <DollarSign className="w-6 h-6 text-violet-600 dark:text-violet-400" />
            </div>
            <div>
              <div className="text-sm text-gray-600 dark:text-gray-400">Total em Contas</div>
              <div className="text-2xl font-bold text-gray-900 dark:text-white">
                {formatCurrency(totalAmount)}
              </div>
            </div>
          </div>
        </Card>

        <Card>
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-full bg-blue-100 dark:bg-blue-900/20 flex items-center justify-center">
              <Calendar className="w-6 h-6 text-blue-600 dark:text-blue-400" />
            </div>
            <div>
              <div className="text-sm text-gray-600 dark:text-gray-400">Gastos do Mês</div>
              <div className="text-2xl font-bold text-gray-900 dark:text-white">
                {formatCurrency(currentMonthTotal)}
              </div>
            </div>
          </div>
        </Card>

        <Card>
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-full bg-green-100 dark:bg-green-900/20 flex items-center justify-center">
              <TrendingUp className="w-6 h-6 text-green-600 dark:text-green-400" />
            </div>
            <div>
              <div className="text-sm text-gray-600 dark:text-gray-400">Total de Contas</div>
              <div className="text-2xl font-bold text-gray-900 dark:text-white">
                {totalBills}
              </div>
            </div>
          </div>
        </Card>
      </div>

      {/* Recent Bills */}
      <Card>
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white">
            Contas Recentes
          </h2>
          {bills.length > 0 && (
            <Button variant="ghost" size="sm" onClick={handleViewAll}>
              Ver todas
            </Button>
          )}
        </div>

        {bills.length === 0 ? (
          <EmptyState
            icon={<Plus className="w-12 h-12" />}
            message="Nenhuma conta cadastrada"
            description="Comece criando sua primeira conta para gerenciar suas finanças."
            actionLabel="Nova Conta"
            onAction={handleNewBill}
          />
        ) : (
          <div className="space-y-4">
            {bills.map((bill) => (
              <BillCard key={bill.id} bill={bill} onEdit={handleEdit} />
            ))}
          </div>
        )}
      </Card>
      </div>
    </AppShell>
  )
}
