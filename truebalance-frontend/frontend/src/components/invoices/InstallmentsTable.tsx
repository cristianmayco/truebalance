import { useMemo } from 'react'
import { Card } from '@/components/ui/Card'
import { formatCurrency } from '@/utils/currency'
import { formatDate } from '@/utils/date'
import { useBills } from '@/hooks/useBills'
import type { InstallmentResponseDTO } from '@/types/dtos/installment.dto'

interface InstallmentsTableProps {
  installments: InstallmentResponseDTO[]
}

export function InstallmentsTable({ installments }: InstallmentsTableProps) {
  // Extrair billIds únicos das parcelas
  const uniqueBillIds = useMemo(() => {
    return Array.from(new Set(installments.map(inst => inst.billId)))
  }, [installments])

  // Buscar todas as contas para criar um mapeamento billId -> nome
  // Usar um tamanho grande para buscar todas as contas de uma vez
  const { data: billsResponse, isLoading: isLoadingBills } = useBills({ size: 10000, page: 0 })
  
  // Criar mapeamento de billId para nome da conta
  const billNameMap = useMemo(() => {
    const map = new Map<number | string, string>()
    if (billsResponse?.content) {
      billsResponse.content.forEach((bill) => {
        // Mapear tanto como number quanto como string para garantir compatibilidade
        const billId = bill.id
        map.set(billId, bill.name)
        // Também mapear como string e number para garantir compatibilidade
        if (typeof billId === 'number') {
          map.set(String(billId), bill.name)
        } else {
          map.set(Number(billId), bill.name)
        }
      })
      
      // Log para debug se não encontrou todas as contas necessárias
      if (uniqueBillIds.length > 0 && billsResponse.content.length > 0) {
        const foundIds = uniqueBillIds.filter(id => {
          return map.has(id) || map.has(String(id)) || map.has(Number(id))
        })
        if (foundIds.length !== uniqueBillIds.length) {
          console.warn('[InstallmentsTable] Some bills not found:', {
            needed: uniqueBillIds,
            found: foundIds,
            available: billsResponse.content.map(b => b.id),
            totalAvailable: billsResponse.content.length
          })
        }
      }
    }
    return map
  }, [billsResponse, uniqueBillIds])

  // Função auxiliar para obter o nome da conta
  const getBillName = (billId: number | string): string => {
    // Tentar buscar tanto como number quanto como string
    const name = billNameMap.get(billId) || billNameMap.get(String(billId)) || billNameMap.get(Number(billId))
    
    // Se não encontrou e as contas já foram carregadas, logar para debug
    if (!name && !isLoadingBills && billsResponse?.content) {
      console.log('[InstallmentsTable] Bill not found:', {
        billId,
        billIdType: typeof billId,
        totalBills: billsResponse.content.length,
        availableBillIds: billsResponse.content.slice(0, 5).map(b => ({ id: b.id, idType: typeof b.id, name: b.name })),
        mapSize: billNameMap.size,
        installmentBillIds: installments.map(i => i.billId)
      })
    }
    
    return name || `Conta #${billId}`
  }

  // Group installments by billId if needed
  const totalAmount = installments.reduce((sum, inst) => sum + inst.amount, 0)

  return (
    <div className="space-y-4">
      {/* Desktop Table */}
      <div className="hidden lg:block overflow-x-auto">
        <div className="w-full overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 dark:bg-slate-800 border-b border-gray-200 dark:border-slate-700">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700 dark:text-slate-300">Descrição</th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700 dark:text-slate-300">Parcela</th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700 dark:text-slate-300">Vencimento</th>
                <th className="px-4 py-3 text-right text-sm font-semibold text-gray-700 dark:text-slate-300">Valor</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 dark:divide-slate-700">
              {installments.map((installment) => (
                <tr key={installment.id} className="bg-white dark:bg-slate-800 transition-colors">
                  <td className="px-4 py-3 text-sm text-gray-900 dark:text-white">
                    <div className="font-medium text-gray-900 dark:text-white">
                      {getBillName(installment.billId)}
                    </div>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-900 dark:text-white">
                    <span className="text-gray-700 dark:text-gray-300">
                      {installment.installmentNumber}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-900 dark:text-white">{formatDate(installment.dueDate)}</td>
                  <td className="px-4 py-3 text-sm text-gray-900 dark:text-white text-right font-medium">{formatCurrency(installment.amount)}</td>
                </tr>
              ))}
            </tbody>
            <tfoot className="bg-gray-50 dark:bg-slate-800 border-t-2 border-gray-300 dark:border-gray-600">
              <tr>
                <td colSpan={3} className="px-4 py-3 text-sm font-bold text-gray-900 dark:text-white">
                  Total
                </td>
                <td className="px-4 py-3 text-sm text-gray-900 dark:text-white text-right font-bold">
                  {formatCurrency(totalAmount)}
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>

      {/* Mobile Cards */}
      <div className="lg:hidden space-y-3">
        {installments.map((installment) => (
          <Card key={installment.id}>
            <div className="space-y-2">
              <div className="flex justify-between items-start">
                <div>
                  <div className="font-medium text-gray-900 dark:text-white">
                    {getBillName(installment.billId)}
                  </div>
                  <div className="text-sm text-gray-600 dark:text-gray-400">
                    Parcela {installment.installmentNumber}
                  </div>
                </div>
                <div className="text-right">
                  <div className="font-bold text-gray-900 dark:text-white">
                    {formatCurrency(installment.amount)}
                  </div>
                  <div className="text-sm text-gray-600 dark:text-gray-400">
                    {formatDate(installment.dueDate)}
                  </div>
                </div>
              </div>
            </div>
          </Card>
        ))}

        {/* Total Card */}
        <Card className="bg-violet-50 dark:bg-violet-900/20 border-violet-200 dark:border-violet-800">
          <div className="flex justify-between items-center">
            <span className="font-bold text-gray-900 dark:text-white">Total</span>
            <span className="text-xl font-bold text-violet-900 dark:text-violet-100">
              {formatCurrency(totalAmount)}
            </span>
          </div>
        </Card>
      </div>
    </div>
  )
}
