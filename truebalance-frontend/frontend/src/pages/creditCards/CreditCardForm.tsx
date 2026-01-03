import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { ArrowLeft, Save } from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import { useCreditCard, useCreateCreditCard, useUpdateCreditCard } from '@/hooks/useCreditCards'
import { useToast } from '@/contexts/ToastContext'
import { creditCardSchema } from '@/schemas/creditCard.schema'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Card } from '@/components/ui/Card'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'

export function CreditCardForm() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const isEditMode = !!id
  const creditCardId = id ? parseInt(id) : undefined

  const { showToast } = useToast()
  const { data: creditCard, isLoading: isLoadingCard } = useCreditCard(creditCardId)
  const { mutate: createCreditCard, isPending: isCreating } = useCreateCreditCard()
  const { mutate: updateCreditCard, isPending: isUpdating } = useUpdateCreditCard()

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(creditCardSchema) as any,
    defaultValues: {
      name: '',
      creditLimit: 0,
      closingDay: 1,
      dueDay: 10,
      allowsPartialPayment: false,
    },
  })

  // Load credit card data when editing
  useEffect(() => {
    if (creditCard && isEditMode) {
      setValue('name', creditCard.name)
      setValue('creditLimit', creditCard.creditLimit)
      setValue('closingDay', creditCard.closingDay)
      setValue('dueDay', creditCard.dueDay)
      setValue('allowsPartialPayment', creditCard.allowsPartialPayment)
    }
  }, [creditCard, isEditMode, setValue])

  const onSubmit = (data: any) => {
    const payload = {
      name: data.name,
      creditLimit: data.creditLimit,
      closingDay: data.closingDay,
      dueDay: data.dueDay,
      allowsPartialPayment: data.allowsPartialPayment,
    }

    if (isEditMode && creditCardId) {
      updateCreditCard(
        { id: creditCardId, creditCard: payload },
        {
          onSuccess: () => {
            showToast('success', 'Cartão atualizado com sucesso!')
            navigate('/credit-cards')
          },
          onError: (error: any) => {
            showToast('error', error.response?.data?.message || 'Erro ao atualizar cartão')
          },
        }
      )
    } else {
      createCreditCard(payload, {
        onSuccess: () => {
          showToast('success', 'Cartão criado com sucesso!')
          navigate('/credit-cards')
        },
        onError: (error: any) => {
          showToast('error', error.response?.data?.message || 'Erro ao criar cartão')
        },
      })
    }
  }

  if (isLoadingCard && isEditMode) {
    return (
      <AppShell title={isEditMode ? 'Editar Cartão' : 'Novo Cartão'}>
        <LoadingSpinner fullScreen />
      </AppShell>
    )
  }

  const isSubmitting = isCreating || isUpdating

  return (
    <AppShell title={isEditMode ? 'Editar Cartão' : 'Novo Cartão'}>
      <div className="max-w-2xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => navigate('/credit-cards')} aria-label="Voltar">
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
              {isEditMode ? 'Editar Cartão' : 'Novo Cartão'}
            </h1>
            <p className="text-gray-600 dark:text-gray-400 mt-1">
              {isEditMode
                ? 'Atualize as informações do cartão'
                : 'Cadastre um novo cartão de crédito'}
            </p>
          </div>
        </div>

        {/* Form */}
        <Card>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            <Input
              label="Nome do cartão"
              placeholder="Ex: Mastercard Black, Visa Gold, etc."
              error={errors.name?.message}
              required
              {...register('name')}
            />

            <Input
              type="number"
              label="Limite de crédito"
              placeholder="0.00"
              step="0.01"
              min="0"
              error={errors.creditLimit?.message}
              required
              {...register('creditLimit', { valueAsNumber: true })}
            />

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                type="number"
                label="Dia de fechamento"
                placeholder="1"
                min="1"
                max="31"
                error={errors.closingDay?.message}
                required
                {...register('closingDay', { valueAsNumber: true })}
              />

              <Input
                type="number"
                label="Dia de vencimento"
                placeholder="10"
                min="1"
                max="31"
                error={errors.dueDay?.message}
                required
                {...register('dueDay', { valueAsNumber: true })}
              />
            </div>

            {/* Toggle for Partial Payment */}
            <div className="flex items-center gap-3 p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
              <input
                type="checkbox"
                id="allowsPartialPayment"
                className="w-5 h-5 text-violet-600 bg-white dark:bg-gray-700 border-gray-300 dark:border-gray-600 rounded focus:ring-violet-500 focus:ring-2"
                {...register('allowsPartialPayment')}
              />
              <label
                htmlFor="allowsPartialPayment"
                className="flex-1 cursor-pointer select-none"
              >
                <div className="font-medium text-gray-900 dark:text-white">
                  Permite pagamento parcial
                </div>
                <div className="text-sm text-gray-600 dark:text-gray-400">
                  Habilite se este cartão aceita pagamentos parciais da fatura
                </div>
              </label>
            </div>

            {/* Info Box */}
            <div className="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg border border-blue-200 dark:border-blue-800">
              <h4 className="font-medium text-blue-900 dark:text-blue-100 mb-2">
                Informações importantes
              </h4>
              <ul className="text-sm text-blue-800 dark:text-blue-200 space-y-1">
                <li>• O dia de fechamento é quando a fatura fecha e não aceita mais compras</li>
                <li>• O dia de vencimento é a data limite para pagar a fatura sem juros</li>
                <li>
                  • Os dias devem estar entre 1 e 31 (considere que nem todos os meses têm 31
                  dias)
                </li>
              </ul>
            </div>

            {/* Actions */}
            <div className="flex justify-end gap-3 pt-4">
              <Button
                type="button"
                variant="secondary"
                onClick={() => navigate('/credit-cards')}
                disabled={isSubmitting}
              >
                Cancelar
              </Button>
              <Button type="submit" loading={isSubmitting}>
                <Save className="w-4 h-4" />
                {isEditMode ? 'Atualizar' : 'Criar'} Cartão
              </Button>
            </div>
          </form>
        </Card>
      </div>
    </AppShell>
  )
}
