import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { DollarSign } from 'lucide-react';
import { partialPaymentSchema, fullPaymentSchema, type PartialPaymentFormData, type FullPaymentFormData } from '@/schemas/partialPayment.schema';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';

interface PaymentFormProps {
  totalAmount: number;
  remainingAmount: number;
  onSubmit: (data: { amount?: number; description?: string }) => void;
  isLoading?: boolean;
}

type PaymentType = 'full' | 'partial';

export default function PaymentForm({ totalAmount, remainingAmount, onSubmit, isLoading = false }: PaymentFormProps) {
  const [paymentType, setPaymentType] = useState<PaymentType>('full');

  const {
    register: registerPartial,
    handleSubmit: handleSubmitPartial,
    formState: { errors: errorsPartial },
    reset: resetPartial,
  } = useForm({
    resolver: zodResolver(partialPaymentSchema) as any,
    defaultValues: {
      amount: 0,
      description: '',
    },
  });

  const {
    register: registerFull,
    handleSubmit: handleSubmitFull,
    formState: { errors: errorsFull },
    reset: resetFull,
  } = useForm({
    resolver: zodResolver(fullPaymentSchema) as any,
    defaultValues: {
      description: '',
    },
  });

  const handlePaymentTypeChange = (type: PaymentType) => {
    setPaymentType(type);
    // Reset both forms when switching
    resetPartial();
    resetFull();
  };

  const onSubmitPartial = (data: PartialPaymentFormData) => {
    // Backend sets paymentDate automatically, we don't send it
    onSubmit({
      amount: data.amount,
      description: data.description,
    });
  };

  const onSubmitFull = (data: FullPaymentFormData) => {
    // For full payment, amount is the remaining amount
    onSubmit({
      amount: remainingAmount,
      description: data.description,
    });
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value);
  };

  return (
    <div className="space-y-6">
      {/* Payment Type Toggle */}
      <div className="flex gap-4">
        <button
          type="button"
          onClick={() => handlePaymentTypeChange('full')}
          className={`flex-1 py-3 px-4 rounded-lg font-medium transition-all ${
            paymentType === 'full'
              ? 'bg-violet-600 text-white shadow-lg'
              : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'
          }`}
        >
          Pagamento Integral
        </button>
        <button
          type="button"
          onClick={() => handlePaymentTypeChange('partial')}
          className={`flex-1 py-3 px-4 rounded-lg font-medium transition-all ${
            paymentType === 'partial'
              ? 'bg-violet-600 text-white shadow-lg'
              : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'
          }`}
        >
          Pagamento Parcial
        </button>
      </div>

      {/* Payment Summary */}
      <div className="bg-violet-50 dark:bg-violet-900/20 rounded-lg p-4 space-y-2">
        <div className="flex justify-between items-center">
          <span className="text-sm text-gray-600 dark:text-gray-400">Valor Total:</span>
          <span className="text-lg font-semibold text-gray-900 dark:text-white">
            {formatCurrency(totalAmount)}
          </span>
        </div>
        <div className="flex justify-between items-center">
          <span className="text-sm text-gray-600 dark:text-gray-400">Valor Restante:</span>
          <span className="text-lg font-bold text-violet-600 dark:text-violet-400">
            {formatCurrency(remainingAmount)}
          </span>
        </div>
        {paymentType === 'full' && (
          <div className="pt-2 border-t border-violet-200 dark:border-violet-800">
            <div className="flex justify-between items-center">
              <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
                Valor a Pagar:
              </span>
              <span className="text-xl font-bold text-green-600 dark:text-green-400">
                {formatCurrency(remainingAmount)}
              </span>
            </div>
          </div>
        )}
      </div>

      {/* Full Payment Form */}
      {paymentType === 'full' && (
        <form onSubmit={handleSubmitFull(onSubmitFull as any)} className="space-y-4">
          {/* Note: Payment date is set automatically by the backend when marking as paid */}
          
          <div className="space-y-2">
            <label htmlFor="description-full" className="block text-sm font-medium text-gray-700 dark:text-gray-300">
              Descrição (opcional)
            </label>
            <textarea
              id="description-full"
              {...registerFull('description')}
              rows={3}
              placeholder="Adicione uma observação sobre este pagamento..."
              className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-violet-500 focus:border-transparent bg-white dark:bg-gray-800 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
            />
            {errorsFull.description && (
              <p className="text-sm text-red-600 dark:text-red-400">{errorsFull.description.message}</p>
            )}
          </div>

          <Button
            type="submit"
            variant="primary"
            size="lg"
            className="w-full"
            loading={isLoading}
            disabled={isLoading}
          >
            <DollarSign className="w-5 h-5 mr-2" />
            Confirmar Pagamento Integral
          </Button>
        </form>
      )}

      {/* Partial Payment Form */}
      {paymentType === 'partial' && (
        <form onSubmit={handleSubmitPartial(onSubmitPartial as any)} className="space-y-4">
          <Input
            label="Valor do Pagamento"
            type="number"
            step="0.01"
            min="0.01"
            icon={<DollarSign className="w-4 h-4" />}
            placeholder="0,00"
            {...registerPartial('amount', { 
              valueAsNumber: true,
              validate: (value) => {
                if (!value || value <= 0) {
                  return 'O valor deve ser maior que zero'
                }
                // Note: Backend allows exceeding invoice balance (creates credit)
                // So we don't validate max value here
                return true
              }
            })}
            error={errorsPartial.amount?.message}
            helpText={`Valor sugerido: ${formatCurrency(remainingAmount)} (pode exceder para criar crédito)`}
          />

          {/* Note: Payment date is set automatically by the backend */}

          <div className="space-y-2">
            <label htmlFor="description-partial" className="block text-sm font-medium text-gray-700 dark:text-gray-300">
              Descrição (opcional)
            </label>
            <textarea
              id="description-partial"
              {...registerPartial('description')}
              rows={3}
              placeholder="Adicione uma observação sobre este pagamento..."
              className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-violet-500 focus:border-transparent bg-white dark:bg-gray-800 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500"
            />
            {errorsPartial.description && (
              <p className="text-sm text-red-600 dark:text-red-400">{errorsPartial.description.message}</p>
            )}
          </div>

          <Button
            type="submit"
            variant="primary"
            size="lg"
            className="w-full"
            loading={isLoading}
            disabled={isLoading}
          >
            <DollarSign className="w-5 h-5 mr-2" />
            Confirmar Pagamento Parcial
          </Button>
        </form>
      )}
    </div>
  );
}
