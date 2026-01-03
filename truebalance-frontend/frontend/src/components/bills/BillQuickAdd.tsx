import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Plus } from 'lucide-react'
import { Modal } from '@/components/ui/Modal'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import { useCreateBill } from '@/hooks/useBills'
import { useToast } from '@/contexts/ToastContext'
import { billSchema, type BillFormData } from '@/schemas/bill.schema'
import { formatDateInput } from '@/utils/date'

interface BillQuickAddProps {
  isOpen: boolean
  onClose: () => void
}

export function BillQuickAdd({ isOpen, onClose }: BillQuickAddProps) {
  const { showToast } = useToast()
  const { mutate: createBill, isPending } = useCreateBill()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<BillFormData>({
    resolver: zodResolver(billSchema) as any,
    defaultValues: {
      name: '',
      date: formatDateInput(new Date()),
      totalAmount: 0,
      numberOfInstallments: 1,
      description: '',
      isRecurring: false,
    },
  })

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
    }

    createBill(payload, {
      onSuccess: () => {
        showToast('success', 'Conta criada com sucesso!')
        reset()
        onClose()
      },
      onError: (error: any) => {
        showToast('error', error.response?.data?.message || 'Erro ao criar conta')
      },
    })
  }

  const handleClose = () => {
    reset()
    onClose()
  }

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Cadastro Rápido de Conta">
      <form onSubmit={handleSubmit(onSubmit as any)} className="space-y-4">
        <Input
          label="Nome da conta"
          placeholder="Ex: Conta de luz"
          error={errors.name?.message}
          required
          {...register('name')}
        />

        <div className="grid grid-cols-2 gap-4">
          <Input
            type="date"
            label="Data"
            error={errors.date?.message}
            required
            {...register('date')}
          />

          <Input
            type="number"
            label="Parcelas"
            placeholder="1"
            min="1"
            error={errors.numberOfInstallments?.message}
            required
            {...register('numberOfInstallments', { valueAsNumber: true })}
          />
        </div>

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

        <div className="flex justify-end gap-3 pt-4">
          <Button type="button" variant="secondary" onClick={handleClose} disabled={isPending}>
            Cancelar
          </Button>
          <Button type="submit" loading={isPending}>
            <Plus className="w-4 h-4" />
            Criar Conta
          </Button>
        </div>
      </form>
    </Modal>
  )
}
