import { useEffect, useMemo } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { ArrowLeft, Save } from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import { useBill, useCreateBill, useUpdateBill } from '@/hooks/useBills'
import { useToast } from '@/contexts/ToastContext'
import { billSchema, type BillFormData } from '@/schemas/bill.schema'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Card } from '@/components/ui/Card'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { useCreditCards } from '@/hooks/useCreditCards'
import { formatCurrency } from '@/utils/currency'
import { formatDateInput } from '@/utils/date'

export function BillForm() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const isEditMode = !!id
  const billId = id ? parseInt(id) : undefined

  const { showToast } = useToast()
  const { data: bill, isLoading: isLoadingBill } = useBill(billId)
  const { data: creditCards = [], isLoading: isLoadingCreditCards } = useCreditCards()
  const { mutate: createBill, isPending: isCreating } = useCreateBill()
  const { mutate: updateBill, isPending: isUpdating } = useUpdateBill()

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm<BillFormData>({
    resolver: zodResolver(billSchema) as any,
    defaultValues: {
      name: '',
      date: formatDateInput(new Date()),
      totalAmount: 0,
      numberOfInstallments: 1,
      description: '',
      creditCardId: null,
      isRecurring: false,
    },
  })

  // Load bill data when editing
  useEffect(() => {
    if (bill && isEditMode) {
      setValue('name', bill.name)
      setValue('date', formatDateInput(bill.date || bill.executionDate || new Date()))
      setValue('totalAmount', bill.totalAmount)
      setValue('numberOfInstallments', bill.numberOfInstallments)
      setValue('description', bill.description || '')
      setValue('isRecurring', bill.isRecurring || false)
      setValue('creditCardId', bill.creditCardId || null)
    }
  }, [bill, isEditMode, setValue])

  // Calculate installment amount
  const totalAmount = watch('totalAmount')
  const numberOfInstallments = watch('numberOfInstallments')
  const installmentAmount = useMemo(() => {
    if (totalAmount > 0 && numberOfInstallments > 0) {
      return totalAmount / numberOfInstallments
    }
    return 0
  }, [totalAmount, numberOfInstallments])

  const onSubmit = (data: BillFormData) => {
    // Convert date string to ISO 8601 format for executionDate
    const executionDate = new Date(data.date).toISOString()
    
    const payload = {
      name: data.name,
      executionDate: executionDate,
      totalAmount: data.totalAmount,
      numberOfInstallments: data.numberOfInstallments,
      description: data.description || undefined,
      isRecurring: data.isRecurring || false,
      creditCardId: data.creditCardId || undefined,
    }

    if (isEditMode && billId) {
      updateBill(
        { id: billId, bill: payload },
        {
          onSuccess: () => {
            showToast('success', 'Conta atualizada com sucesso!')
            navigate('/bills')
          },
          onError: (error: any) => {
            showToast('error', error.response?.data?.message || 'Erro ao atualizar conta')
          },
        }
      )
    } else {
      createBill(payload, {
        onSuccess: () => {
          showToast('success', 'Conta criada com sucesso!')
          // Navigate after a short delay to ensure query invalidation completes
          setTimeout(() => {
            navigate('/bills', { replace: true })
          }, 100)
        },
        onError: (error: any) => {
          showToast('error', error.response?.data?.message || 'Erro ao criar conta')
        },
      })
    }
  }

  if (isLoadingBill && isEditMode) {
    return (
      <AppShell title={isEditMode ? 'Editar Conta' : 'Nova Conta'}>
        <LoadingSpinner fullScreen />
      </AppShell>
    )
  }

  const isSubmitting = isCreating || isUpdating

  return (
    <AppShell title={isEditMode ? 'Editar Conta' : 'Nova Conta'}>
      <div className="max-w-2xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" onClick={() => navigate('/bills')} aria-label="Voltar">
          <ArrowLeft className="w-5 h-5" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
            {isEditMode ? 'Editar Conta' : 'Nova Conta'}
          </h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">
            {isEditMode ? 'Atualize as informações da conta' : 'Cadastre uma nova conta'}
          </p>
        </div>
      </div>

      {/* Form */}
      <Card>
        <form onSubmit={handleSubmit(onSubmit as any)} className="space-y-6">
          <Input
            label="Nome da conta"
            placeholder="Ex: Conta de luz, Internet, etc."
            error={errors.name?.message}
            required
            {...register('name')}
          />

          <Input
            type="date"
            label="Data da conta"
            error={errors.date?.message}
            required
            {...register('date')}
          />

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              type="number"
              label="Valor total"
              placeholder="0.00"
              step="0.01"
              min="0"
              error={errors.totalAmount?.message}
              required
              {...register('totalAmount', { valueAsNumber: true })}
            />

            <Input
              type="number"
              label="Número de parcelas"
              placeholder="1"
              min="1"
              error={errors.numberOfInstallments?.message}
              required
              {...register('numberOfInstallments', { valueAsNumber: true })}
            />
          </div>

          {/* Installment Amount Display */}
          {installmentAmount > 0 && (
            <div className="p-4 bg-violet-50 dark:bg-violet-900/20 rounded-lg">
              <div className="text-sm text-violet-700 dark:text-violet-300">
                Valor da parcela
              </div>
              <div className="text-2xl font-bold text-violet-900 dark:text-violet-100 mt-1">
                {formatCurrency(installmentAmount)}
              </div>
            </div>
          )}

          <Input
            label="Descrição (opcional)"
            placeholder="Observações sobre esta conta..."
            error={errors.description?.message}
            {...register('description')}
          />

          {/* Recurring Bill Checkbox */}
          <div className="flex items-start gap-3">
            <input
              type="checkbox"
              id="isRecurring"
              {...register('isRecurring')}
              className="mt-1 w-4 h-4 text-primary-600 bg-gray-100 border-gray-300 rounded focus:ring-primary-500 focus:ring-2 dark:bg-slate-700 dark:border-slate-600 dark:focus:ring-primary-400"
            />
            <div className="flex-1">
              <label
                htmlFor="isRecurring"
                className="block text-sm font-medium text-gray-700 dark:text-slate-300 cursor-pointer"
              >
                Conta recorrente
              </label>
              <p className="mt-1 text-sm text-gray-500 dark:text-slate-400">
                Marque esta opção se a conta se repete mensalmente (ex: conta de internet, assinatura, etc.)
              </p>
              {errors.isRecurring && (
                <p className="mt-1 text-sm text-error" role="alert">
                  {errors.isRecurring.message}
                </p>
              )}
            </div>
          </div>

          {/* Credit Card Selection */}
          {!isLoadingCreditCards && creditCards.length > 0 && (
            <Select
              label="Associar ao cartão de crédito (opcional)"
              error={errors.creditCardId?.message}
              options={[
                { value: '', label: 'Não associar a cartão' },
                ...creditCards.map((card) => ({
                  value: card.id,
                  label: card.name,
                })),
              ]}
              {...register('creditCardId', {
                setValueAs: (value) => {
                  if (value === '' || value === null || value === undefined) {
                    return null
                  }
                  return parseInt(value as string, 10)
                },
              })}
            />
          )}

          {/* Actions */}
          <div className="flex justify-end gap-3 pt-4">
            <Button
              type="button"
              variant="secondary"
              onClick={() => navigate('/bills')}
              disabled={isSubmitting}
            >
              Cancelar
            </Button>
            <Button type="submit" loading={isSubmitting}>
              <Save className="w-4 h-4" />
              {isEditMode ? 'Atualizar' : 'Criar'} Conta
            </Button>
          </div>
        </form>
      </Card>
      </div>
    </AppShell>
  )
}
