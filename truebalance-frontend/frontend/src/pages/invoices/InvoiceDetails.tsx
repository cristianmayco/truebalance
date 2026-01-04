import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, CheckCircle, DollarSign, Settings, Edit2, X, Check } from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import {
  useInvoice,
  useInvoiceInstallments,
  useInvoicePartialPayments,
  useMarkInvoiceAsPaid,
  useDeletePartialPayment,
  useUpdateInvoiceUseAbsoluteValue,
  useUpdateInvoiceTotalAmount,
  useUpdateInvoiceRegisteredLimit,
} from '@/hooks/useInvoices'
import { useCreditCard } from '@/hooks/useCreditCards'
import { useToast } from '@/contexts/ToastContext'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { InstallmentsTable } from '@/components/invoices/InstallmentsTable'
import { PaymentsHistory } from '@/components/invoices/PaymentsHistory'
import { formatCurrency } from '@/utils/currency'

export function InvoiceDetails() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const invoiceId = id ? parseInt(id) : undefined
  const { showToast } = useToast()

  const [isEditingTotal, setIsEditingTotal] = useState(false)
  const [editedTotal, setEditedTotal] = useState('')

  const { data: invoice, isLoading: isLoadingInvoice } = useInvoice(invoiceId)
  const { data: creditCard, isLoading: isLoadingCard } = useCreditCard(invoice?.creditCardId)
  const { data: installments, isLoading: isLoadingInstallments } =
    useInvoiceInstallments(invoiceId)
  const { data: partialPayments, isLoading: isLoadingPayments } =
    useInvoicePartialPayments(invoiceId)

  const markAsPaidMutation = useMarkInvoiceAsPaid()
  const deletePartialPaymentMutation = useDeletePartialPayment()
  const updateUseAbsoluteValueMutation = useUpdateInvoiceUseAbsoluteValue()
  const updateTotalAmountMutation = useUpdateInvoiceTotalAmount()
  const updateRegisteredLimitMutation = useUpdateInvoiceRegisteredLimit()
  const isMarkingAsPaid = markAsPaidMutation.isPending
  const isDeletingPayment = deletePartialPaymentMutation.isPending
  const isUpdatingFlag = updateUseAbsoluteValueMutation.isPending
  const isUpdatingTotal = updateTotalAmountMutation.isPending
  const isUpdatingRegisteredLimit = updateRegisteredLimitMutation.isPending

  const isLoading =
    isLoadingInvoice || isLoadingCard || isLoadingInstallments || isLoadingPayments

  const handleMarkAsPaid = async () => {
    if (!invoiceId) return

    try {
      await markAsPaidMutation.mutateAsync(invoiceId)
      showToast('success', 'Fatura marcada como paga!')
    } catch (error: any) {
      showToast('error', error.response?.data?.message || 'Erro ao marcar fatura como paga')
    }
  }

  const handleDeletePartialPayment = async (paymentId: number) => {
    try {
      await deletePartialPaymentMutation.mutateAsync(paymentId)
      showToast('success', 'Pagamento parcial deletado com sucesso!')
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Erro ao deletar pagamento parcial'
      showToast('error', errorMessage)
    }
  }

  const handleToggleUseAbsoluteValue = async () => {
    if (!invoiceId || !invoice) return

    try {
      const newValue = !invoice.useAbsoluteValue
      await updateUseAbsoluteValueMutation.mutateAsync({
        invoiceId,
        useAbsoluteValue: newValue,
      })
      showToast(
        'success',
        newValue
          ? 'Fatura configurada para usar valor absoluto'
          : 'Fatura configurada para calcular pela soma das parcelas'
      )
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Erro ao atualizar configuração da fatura'
      showToast('error', errorMessage)
    }
  }

  const handleStartEditingTotal = () => {
    if (!invoice) return
    setEditedTotal(invoice.totalAmount.toString())
    setIsEditingTotal(true)
  }

  const handleCancelEditingTotal = () => {
    setIsEditingTotal(false)
    setEditedTotal('')
  }

  const handleSaveTotal = async () => {
    if (!invoiceId || !editedTotal) return

    const totalAmount = parseFloat(editedTotal)
    if (isNaN(totalAmount) || totalAmount < 0) {
      showToast('error', 'Valor inválido. Digite um número válido maior ou igual a zero.')
      return
    }

    try {
      await updateTotalAmountMutation.mutateAsync({
        invoiceId,
        totalAmount,
      })
      showToast('success', 'Valor total da fatura atualizado com sucesso!')
      setIsEditingTotal(false)
      setEditedTotal('')
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Erro ao atualizar valor total da fatura'
      showToast('error', errorMessage)
    }
  }

  const handleToggleRegisteredLimit = async () => {
    if (!invoiceId || !invoice) return

    // Só permite ativar se a fatura estiver fechada
    if (!invoice.closed && !invoice.registerAvailableLimit) {
      showToast('error', 'A fatura precisa estar fechada para cadastrar o limite disponível.')
      return
    }

    const newValue = !invoice.registerAvailableLimit

    // Se está ativando, pede o valor
    if (newValue) {
      const limit = prompt('Digite o limite disponível para esta fatura:')
      if (limit === null) return // Cancelou

      const registeredLimit = parseFloat(limit)
      if (isNaN(registeredLimit) || registeredLimit < 0) {
        showToast('error', 'Valor inválido. Digite um número válido maior ou igual a zero.')
        return
      }

      try {
        await updateRegisteredLimitMutation.mutateAsync({
          invoiceId,
          registerAvailableLimit: true,
          registeredAvailableLimit: registeredLimit,
        })
        showToast('success', 'Limite disponível cadastrado com sucesso!')
      } catch (error: any) {
        const errorMessage = error.response?.data || error.response?.data?.message || 'Erro ao cadastrar limite disponível'
        showToast('error', errorMessage)
      }
    } else {
      // Desativando
      try {
        await updateRegisteredLimitMutation.mutateAsync({
          invoiceId,
          registerAvailableLimit: false,
          registeredAvailableLimit: undefined,
        })
        showToast('success', 'Cadastro de limite disponível removido')
      } catch (error: any) {
        const errorMessage = error.response?.data || error.response?.data?.message || 'Erro ao remover cadastro de limite'
        showToast('error', errorMessage)
      }
    }
  }

  if (isLoading) {
    return (
      <AppShell title="Detalhes da Fatura">
        <LoadingSpinner fullScreen />
      </AppShell>
    )
  }

  if (!invoice || !creditCard) {
    return (
      <AppShell title="Detalhes da Fatura">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <p className="text-red-600 dark:text-red-400 mb-4">Fatura não encontrada</p>
            <Button onClick={() => navigate('/credit-cards')}>Voltar aos Cartões</Button>
          </div>
        </div>
      </AppShell>
    )
  }

  // Format reference month
  // Parse date string as local date to avoid timezone issues
  const [year, month, day] = invoice.referenceMonth.split('-').map(Number)
  const referenceDate = new Date(year, month - 1, day) // month is 0-indexed
  const monthName = referenceDate.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' })
  const formattedMonth = monthName.charAt(0).toUpperCase() + monthName.slice(1)

  // Calculate totals
  const totalPaid = partialPayments?.reduce((sum, p) => sum + p.amount, 0) || 0
  const remainingBalance = invoice.totalAmount - totalPaid

  return (
    <AppShell title={`Fatura ${formattedMonth}`}>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            onClick={() => navigate(`/credit-cards/${invoice.creditCardId}/invoices`)}
            aria-label="Voltar"
          >
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div className="flex-1">
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
              Fatura {formattedMonth}
            </h1>
            <p className="text-gray-600 dark:text-gray-400 mt-1">{creditCard.name}</p>
          </div>
          <div className="flex gap-2">
            {invoice.paid ? (
              <Badge variant="success">
                <CheckCircle className="w-3 h-3" />
                Paga
              </Badge>
            ) : invoice.closed ? (
              <Badge variant="warning">Fechada</Badge>
            ) : (
              <Badge variant="info">Aberta</Badge>
            )}
          </div>
        </div>

        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card>
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-violet-100 dark:bg-violet-900/20 flex items-center justify-center">
                <DollarSign className="w-6 h-6 text-violet-600 dark:text-violet-400" />
              </div>
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-sm text-gray-600 dark:text-gray-400">Valor Total</span>
                  {invoice.useAbsoluteValue && (
                    <Badge variant="warning" size="sm" className="text-xs">
                      Absoluto
                    </Badge>
                  )}
                </div>
                {isEditingTotal ? (
                  <div className="flex items-center gap-2">
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      value={editedTotal}
                      onChange={(e) => setEditedTotal(e.target.value)}
                      className="w-full px-3 py-2 text-lg font-bold border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:ring-2 focus:ring-violet-500 focus:border-transparent"
                      disabled={isUpdatingTotal}
                      autoFocus
                    />
                    <button
                      onClick={handleSaveTotal}
                      disabled={isUpdatingTotal}
                      className="p-2 text-green-600 hover:bg-green-50 dark:hover:bg-green-900/20 rounded-lg transition-colors"
                      title="Salvar"
                    >
                      <Check className="w-5 h-5" />
                    </button>
                    <button
                      onClick={handleCancelEditingTotal}
                      disabled={isUpdatingTotal}
                      className="p-2 text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
                      title="Cancelar"
                    >
                      <X className="w-5 h-5" />
                    </button>
                  </div>
                ) : (
                  <div className="flex items-center gap-2">
                    <div className="text-2xl font-bold text-gray-900 dark:text-white">
                      {formatCurrency(invoice.totalAmount)}
                    </div>
                    {invoice.useAbsoluteValue && !invoice.closed && (
                      <button
                        onClick={handleStartEditingTotal}
                        className="p-1 text-violet-600 hover:bg-violet-50 dark:hover:bg-violet-900/20 rounded transition-colors"
                        title="Editar valor total"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                    )}
                  </div>
                )}
              </div>
            </div>
          </Card>

          {invoice.previousBalance > 0 && (
            <Card>
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-full bg-orange-100 dark:bg-orange-900/20 flex items-center justify-center">
                  <DollarSign className="w-6 h-6 text-orange-600 dark:text-orange-400" />
                </div>
                <div>
                  <div className="text-sm text-gray-600 dark:text-gray-400">Saldo Anterior</div>
                  <div className="text-2xl font-bold text-gray-900 dark:text-white">
                    {formatCurrency(invoice.previousBalance)}
                  </div>
                </div>
              </div>
            </Card>
          )}

          {!invoice.paid && totalPaid > 0 && (
            <Card>
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-full bg-green-100 dark:bg-green-900/20 flex items-center justify-center">
                  <CheckCircle className="w-6 h-6 text-green-600 dark:text-green-400" />
                </div>
                <div>
                  <div className="text-sm text-gray-600 dark:text-gray-400">Saldo Restante</div>
                  <div className="text-2xl font-bold text-gray-900 dark:text-white">
                    {formatCurrency(remainingBalance)}
                  </div>
                </div>
              </div>
            </Card>
          )}
        </div>

        {/* Settings - Valor Absoluto */}
        <Card className="border-2 border-violet-200 dark:border-violet-800">
            <div className="flex items-start gap-4">
              <div className="w-12 h-12 rounded-full bg-violet-100 dark:bg-violet-900/20 flex items-center justify-center flex-shrink-0">
                <Settings className="w-6 h-6 text-violet-600 dark:text-violet-400" />
              </div>
              <div className="flex-1">
                <div className="flex items-center justify-between mb-2">
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                    Valor Absoluto da Fatura
                  </h3>
                  {invoice.useAbsoluteValue && (
                    <Badge variant="warning" className="text-xs">
                      Ativo
                    </Badge>
                  )}
                </div>
                <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
                  {invoice.useAbsoluteValue ? (
                    <>
                      Esta fatura está configurada para usar <strong>valor absoluto</strong>. 
                      O total não será recalculado automaticamente pela soma das parcelas. 
                      Útil para faturas antigas onde nem todas as contas foram cadastradas.
                    </>
                  ) : (
                    <>
                      Esta fatura calcula o total <strong>automaticamente</strong> pela soma das parcelas. 
                      Ao adicionar ou remover contas, o valor total será atualizado.
                    </>
                  )}
                  {invoice.closed && (
                    <span className="block mt-2 text-xs text-gray-500 dark:text-gray-400 italic">
                      Esta configuração não pode ser alterada pois a fatura está fechada.
                    </span>
                  )}
                </p>
                <div className="flex items-center gap-3">
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      checked={invoice.useAbsoluteValue || false}
                      onChange={handleToggleUseAbsoluteValue}
                      disabled={isUpdatingFlag || invoice.closed}
                      className="sr-only peer"
                    />
                    <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-violet-300 dark:peer-focus:ring-violet-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-violet-600 peer-disabled:opacity-50 peer-disabled:cursor-not-allowed"></div>
                  </label>
                  <div>
                    <span className="text-sm font-medium text-gray-700 dark:text-gray-300 block">
                      Usar Valor Absoluto
                    </span>
                    <span className="text-xs text-gray-500 dark:text-gray-500">
                      {invoice.useAbsoluteValue ? 'Ativado' : 'Desativado'}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </Card>

        {/* Cadastrar Limite Disponível */}
        <Card className="border-2 border-blue-200 dark:border-blue-800">
          <div className="flex items-start gap-4">
            <div className="w-12 h-12 rounded-full bg-blue-100 dark:bg-blue-900/20 flex items-center justify-center flex-shrink-0">
              <Settings className="w-6 h-6 text-blue-600 dark:text-blue-400" />
            </div>
            <div className="flex-1">
              <div className="flex items-center justify-between mb-2">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                  Cadastrar Limite Disponível
                </h3>
                {invoice.registerAvailableLimit && (
                  <Badge variant="info" className="text-xs">
                    Ativo
                  </Badge>
                )}
              </div>
              <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
                {invoice.registerAvailableLimit ? (
                  <>
                    Esta fatura está configurada como <strong>ponto de partida</strong> para cálculos de limite.
                    O limite disponível registrado é <strong>{formatCurrency(invoice.registeredAvailableLimit || 0)}</strong>.
                    Todas as faturas anteriores serão ignoradas no cálculo do limite disponível.
                  </>
                ) : (
                  <>
                    Ative esta opção para definir um limite disponível específico para esta fatura.
                    Isso é útil quando você quer começar a usar o sistema a partir desta fatura,
                    ignorando faturas anteriores no cálculo do limite.
                    {!invoice.closed && (
                      <span className="block mt-2 text-xs text-gray-500 dark:text-gray-400 italic">
                        Esta opção só pode ser ativada quando a fatura estiver fechada.
                      </span>
                    )}
                  </>
                )}
              </p>
              <div className="flex items-center gap-3">
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={invoice.registerAvailableLimit || false}
                    onChange={handleToggleRegisteredLimit}
                    disabled={isUpdatingRegisteredLimit || !invoice.closed}
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600 peer-disabled:opacity-50 peer-disabled:cursor-not-allowed"></div>
                </label>
                <div>
                  <span className="text-sm font-medium text-gray-700 dark:text-gray-300 block">
                    Cadastrar Limite
                  </span>
                  <span className="text-xs text-gray-500 dark:text-gray-500">
                    {invoice.registerAvailableLimit ? 'Ativado' : 'Desativado'}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </Card>

        {/* Actions */}
        {!invoice.paid && (
          <div className="flex gap-3">
            {invoice.closed && (
              <Button variant="primary" onClick={handleMarkAsPaid} loading={isMarkingAsPaid}>
                <CheckCircle className="w-4 h-4" />
                Marcar como Paga
              </Button>
            )}
            {creditCard.allowsPartialPayment && !invoice.closed && (
              <Button
                variant="secondary"
                onClick={() => navigate(`/invoices/${invoice.id}/payment`)}
              >
                <DollarSign className="w-4 h-4" />
                Adicionar Pagamento Parcial
              </Button>
            )}
          </div>
        )}

        {/* Installments */}
        <Card>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold text-gray-900 dark:text-white">
              Parcelas da Fatura
            </h2>
            {invoice.useAbsoluteValue && (
              <Badge variant="info" className="text-xs">
                Valor absoluto ativo
              </Badge>
            )}
          </div>
          {installments && installments.length > 0 ? (
            <InstallmentsTable installments={installments} />
          ) : (
            <div className="text-center py-8 text-gray-600 dark:text-gray-400">
              Nenhuma parcela encontrada
            </div>
          )}
          {invoice.useAbsoluteValue && (
            <div className="mt-4 p-4 bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded-lg">
              <div className="flex items-start gap-2">
                <div className="flex-shrink-0 mt-0.5">
                  <svg className="w-5 h-5 text-amber-600 dark:text-amber-400" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm font-medium text-amber-800 dark:text-amber-200 mb-1">
                    Valor Absoluto Ativo
                  </p>
                  <p className="text-sm text-amber-700 dark:text-amber-300">
                    O valor total desta fatura não será recalculado automaticamente quando novas parcelas forem adicionadas ou removidas. 
                    O total permanecerá fixo no valor definido.
                  </p>
                </div>
              </div>
            </div>
          )}
        </Card>

        {/* Partial Payments */}
        {creditCard.allowsPartialPayment && (
          <Card>
            <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
              Histórico de Pagamentos Parciais
            </h2>
            <PaymentsHistory 
              payments={partialPayments || []} 
              totalAmount={invoice.totalAmount}
              onDelete={handleDeletePartialPayment}
              canDelete={!invoice.closed}
              isLoading={isDeletingPayment}
            />
          </Card>
        )}
      </div>
    </AppShell>
  )
}
