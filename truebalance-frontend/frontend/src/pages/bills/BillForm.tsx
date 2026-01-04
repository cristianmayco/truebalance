import { useEffect, useMemo, useState } from 'react'
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
import { useCategories } from '@/hooks/useCategories'
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
  const { data: categories = [], isLoading: isLoadingCategories } = useCategories()
  const { mutate: createBill, isPending: isCreating } = useCreateBill()
  const { mutate: updateBill, isPending: isUpdating } = useUpdateBill()

  // Estado para controlar se o usuário está informando o valor total ou o valor da parcela
  const [isInputTotal, setIsInputTotal] = useState(true)

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
      category: '',
      categoryId: null,
      creditCardId: null,
      isRecurring: false,
    },
  })

  // Limpar o campo de valor quando alternar entre os modos
  const handleToggleInputMode = (checked: boolean) => {
    setIsInputTotal(checked)
    setValue('totalAmount', 0)
  }

  // Load bill data when editing
  useEffect(() => {
    if (bill && isEditMode && categories.length > 0) {
      setValue('name', bill.name)
      setValue('date', formatDateInput(bill.date || bill.executionDate || new Date()))
      setValue('totalAmount', bill.totalAmount)
      setValue('numberOfInstallments', bill.numberOfInstallments)
      setValue('description', bill.description || '')
      setValue('category', bill.category || '')
      // Se a categoria existe, tentar encontrar o ID correspondente
      if (bill.category) {
        const foundCategory = categories.find(cat => cat.name === bill.category)
        if (foundCategory) {
          setValue('categoryId', foundCategory.id)
        }
      }
      setValue('isRecurring', bill.isRecurring || false)
      setValue('creditCardId', bill.creditCardId || null)
    }
  }, [bill, isEditMode, categories, setValue])

  // Watch form values
  const totalAmount = watch('totalAmount')
  const numberOfInstallments = watch('numberOfInstallments')

  // Calculate installment amount or total amount based on input mode
  const calculatedValue = useMemo(() => {
    if (isInputTotal) {
      // Se o usuário está informando o valor total, calcula o valor da parcela
      if (totalAmount > 0 && numberOfInstallments > 0) {
        return totalAmount / numberOfInstallments
      }
    } else {
      // Se o usuário está informando o valor da parcela, calcula o valor total
      if (totalAmount > 0 && numberOfInstallments > 0) {
        return totalAmount * numberOfInstallments
      }
    }
    return 0
  }, [totalAmount, numberOfInstallments, isInputTotal])

  const onSubmit = (data: BillFormData) => {
    // Convert date string to ISO 8601 format for executionDate
    const executionDate = new Date(data.date).toISOString()

    // Calculate the correct totalAmount based on input mode
    const finalTotalAmount = isInputTotal
      ? data.totalAmount  // Usuário informou o valor total
      : data.totalAmount * data.numberOfInstallments  // Usuário informou o valor da parcela

    // Se categoryId foi selecionado, usar o nome da categoria correspondente
    let categoryName = data.category
    if (data.categoryId && categories.length > 0) {
      const selectedCategory = categories.find(cat => cat.id === data.categoryId)
      if (selectedCategory) {
        categoryName = selectedCategory.name
      }
    }

    const payload = {
      name: data.name,
      executionDate: executionDate,
      totalAmount: finalTotalAmount,
      numberOfInstallments: data.numberOfInstallments,
      description: data.description || undefined,
      category: categoryName || undefined,
      categoryId: data.categoryId || undefined,
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

          {/* Input Mode Selection */}
          <div className="flex items-start gap-3 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg border border-blue-200 dark:border-blue-800">
            <input
              type="checkbox"
              id="isInputTotal"
              checked={isInputTotal}
              onChange={(e) => handleToggleInputMode(e.target.checked)}
              className="mt-1 w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500 focus:ring-2 dark:bg-slate-700 dark:border-slate-600 dark:focus:ring-blue-400"
            />
            <div className="flex-1">
              <label
                htmlFor="isInputTotal"
                className="block text-sm font-medium text-blue-900 dark:text-blue-100 cursor-pointer"
              >
                Informar valor total
              </label>
              <p className="mt-1 text-sm text-blue-700 dark:text-blue-300">
                {isInputTotal
                  ? 'Você vai informar o valor total da conta e o sistema calculará o valor de cada parcela.'
                  : 'Você vai informar o valor de cada parcela e o sistema calculará o valor total.'}
              </p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              type="number"
              label={isInputTotal ? 'Valor total' : 'Valor de cada parcela'}
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

          {/* Calculated Value Display */}
          {calculatedValue > 0 && (
            <div className="p-4 bg-violet-50 dark:bg-violet-900/20 rounded-lg border border-violet-200 dark:border-violet-800">
              <div className="text-sm text-violet-700 dark:text-violet-300">
                {isInputTotal ? 'Valor de cada parcela' : 'Valor total'}
              </div>
              <div className="text-2xl font-bold text-violet-900 dark:text-violet-100 mt-1">
                {formatCurrency(calculatedValue)}
              </div>
              {!isInputTotal && (
                <div className="text-xs text-violet-600 dark:text-violet-400 mt-2">
                  {numberOfInstallments}x de {formatCurrency(totalAmount)} = {formatCurrency(calculatedValue)}
                </div>
              )}
            </div>
          )}

          <Input
            label="Descrição (opcional)"
            placeholder="Observações sobre esta conta..."
            error={errors.description?.message}
            {...register('description')}
          />

          {/* Category Selection */}
          {!isLoadingCategories && (
            <div className="space-y-2">
              <Select
                label="Categoria (opcional)"
                error={errors.categoryId?.message}
                options={[
                  { value: '', label: 'Sem categoria' },
                  ...categories.map((cat) => ({
                    value: cat.id,
                    label: cat.name,
                  })),
                ]}
                {...register('categoryId', {
                  setValueAs: (value) => {
                    if (value === '' || value === null || value === undefined) {
                      return null
                    }
                    return parseInt(value as string, 10)
                  },
                })}
              />
              <div className="text-xs text-gray-500 dark:text-gray-400">
                Não encontrou a categoria?{' '}
                <button
                  type="button"
                  onClick={() => navigate('/categories/new')}
                  className="text-primary-600 hover:text-primary-700 dark:text-primary-400 dark:hover:text-primary-300 underline"
                >
                  Criar nova categoria
                </button>
              </div>
            </div>
          )}

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
