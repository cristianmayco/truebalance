import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, CheckCircle, AlertCircle, XCircle } from 'lucide-react';
import { useInvoice, useAddPartialPayment, useMarkInvoiceAsPaid } from '@/hooks/useInvoices';
import { useCreditCard } from '@/hooks/useCreditCards';
import PaymentForm from '@/components/invoices/PaymentForm';
import Card from '@/components/ui/Card';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Button from '@/components/ui/Button';
import { useToast } from '@/contexts/ToastContext';
import { formatCurrency } from '@/utils/currency';

export default function InvoicePayment() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const { data: invoice, isLoading, error } = useInvoice(id ? parseInt(id) : undefined);
  const { data: creditCard } = useCreditCard(invoice?.creditCardId);
  const addPartialPaymentMutation = useAddPartialPayment();
  const markAsPaidMutation = useMarkInvoiceAsPaid();

  const handlePayment = async (data: { amount?: number; description?: string }) => {
    if (!invoice) return;

    try {
      const isFullPayment = data.amount === undefined || data.amount === remainingAmount;

      if (isFullPayment) {
        // Mark invoice as paid
        await markAsPaidMutation.mutateAsync(invoice.id);

        showToast('success', 'Fatura paga com sucesso!', 'A fatura foi marcada como paga.');
      } else {
        // Add partial payment
        const result = await addPartialPaymentMutation.mutateAsync({
          invoiceId: invoice.id,
          payment: {
            amount: data.amount!,
            description: data.description,
          },
        });

        const message = result.creditCardAvailableLimit 
          ? `Valor de ${formatCurrency(data.amount!)} foi adicionado. Limite disponível: ${formatCurrency(result.creditCardAvailableLimit)}`
          : `Valor de ${formatCurrency(data.amount!)} foi adicionado aos pagamentos.`
        
        showToast('success', 'Pagamento parcial registrado!', message);
      }

      // Navigate back to invoice details
      navigate(`/invoices/${invoice.id}`);
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Não foi possível processar o pagamento. Tente novamente.'
      showToast('error', 'Erro ao processar pagamento', errorMessage);
      console.error('Payment error:', err);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (error || !invoice) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-8">
        <Card className="p-8 text-center">
          <AlertCircle className="w-16 h-16 mx-auto text-red-500 mb-4" />
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
            Fatura não encontrada
          </h2>
          <p className="text-gray-600 dark:text-gray-400 mb-6">
            Não foi possível carregar os dados da fatura.
          </p>
          <Button onClick={() => navigate('/invoices')} variant="primary">
            Voltar para Faturas
          </Button>
        </Card>
      </div>
    );
  }

  // Check if credit card allows partial payments
  if (creditCard && !creditCard.allowsPartialPayment && !invoice.closed) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-8">
        <Card className="p-8 text-center">
          <XCircle className="w-16 h-16 mx-auto text-orange-500 mb-4" />
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
            Pagamento Parcial Não Permitido
          </h2>
          <p className="text-gray-600 dark:text-gray-400 mb-6">
            Este cartão de crédito não permite pagamentos parciais. Você só pode marcar a fatura como paga após ela ser fechada.
          </p>
          <Button onClick={() => navigate(`/invoices/${invoice.id}`)} variant="primary">
            Voltar para Detalhes da Fatura
          </Button>
        </Card>
      </div>
    );
  }

  // Check if invoice is closed (can't add partial payments to closed invoices)
  if (invoice.closed && !invoice.paid) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-8">
        <Card className="p-8 text-center">
          <AlertCircle className="w-16 h-16 mx-auto text-orange-500 mb-4" />
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
            Fatura Fechada
          </h2>
          <p className="text-gray-600 dark:text-gray-400 mb-6">
            Esta fatura já está fechada. Não é possível adicionar pagamentos parciais em faturas fechadas.
          </p>
          <Button onClick={() => navigate(`/invoices/${invoice.id}`)} variant="primary">
            Voltar para Detalhes da Fatura
          </Button>
        </Card>
      </div>
    );
  }

  // Calculate remaining amount
  const paidAmount = invoice.partialPayments?.reduce((sum, payment) => sum + payment.amount, 0) || 0;
  const remainingAmount = invoice.totalAmount - paidAmount;

  // Check if invoice is already fully paid
  if (invoice.paid || remainingAmount <= 0) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-8">
        <Card className="p-8 text-center">
          <CheckCircle className="w-16 h-16 mx-auto text-green-500 mb-4" />
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
            Fatura já foi paga
          </h2>
          <p className="text-gray-600 dark:text-gray-400 mb-6">
            Esta fatura já está totalmente paga.
          </p>
          <Button onClick={() => navigate(`/invoices/${invoice.id}`)} variant="primary">
            Ver Detalhes da Fatura
          </Button>
        </Card>
      </div>
    );
  }

  const formatMonth = (dateString: string | Date) => {
    let date: Date;
    if (typeof dateString === 'string') {
      // Parse date string as local date to avoid timezone issues
      const [year, month, day] = dateString.split('-').map(Number);
      date = new Date(year, month - 1, day); // month is 0-indexed
    } else {
      date = dateString;
    }
    return date.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
  };

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <button
          onClick={() => navigate(`/invoices/${invoice.id}`)}
          className="flex items-center gap-2 text-violet-600 dark:text-violet-400 hover:text-violet-700 dark:hover:text-violet-300 mb-4"
        >
          <ArrowLeft className="w-4 h-4" />
          Voltar para Detalhes
        </button>

        <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
          Pagamento de Fatura
        </h1>
        <p className="text-gray-600 dark:text-gray-400">
          Fatura de {formatMonth(invoice.referenceMonth)} - {invoice.creditCard?.name}
        </p>
      </div>

      {/* Invoice Summary Card */}
      <Card className="mb-6 p-6">
        <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
          Resumo da Fatura
        </h2>
        <div className="space-y-3">
          <div className="flex justify-between items-center pb-3 border-b border-gray-200 dark:border-gray-700">
            <span className="text-gray-600 dark:text-gray-400">Valor Total da Fatura:</span>
            <span className="text-lg font-semibold text-gray-900 dark:text-white">
              {formatCurrency(invoice.totalAmount)}
            </span>
          </div>

          {paidAmount > 0 && (
            <div className="flex justify-between items-center pb-3 border-b border-gray-200 dark:border-gray-700">
              <span className="text-gray-600 dark:text-gray-400">Já Pago:</span>
              <span className="text-lg font-semibold text-green-600 dark:text-green-400">
                {formatCurrency(paidAmount)}
              </span>
            </div>
          )}

          <div className="flex justify-between items-center pt-2">
            <span className="text-lg font-medium text-gray-900 dark:text-white">
              Valor Restante:
            </span>
            <span className="text-2xl font-bold text-violet-600 dark:text-violet-400">
              {formatCurrency(remainingAmount)}
            </span>
          </div>
        </div>
      </Card>

      {/* Payment Form Card */}
      <Card className="p-6">
        <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-6">
          Realizar Pagamento
        </h2>

        <PaymentForm
          totalAmount={invoice.totalAmount}
          remainingAmount={remainingAmount}
          onSubmit={handlePayment}
          isLoading={addPartialPaymentMutation.isPending || markAsPaidMutation.isPending}
        />
      </Card>

      {/* Help Text */}
      <div className="mt-6 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
        <p className="text-sm text-blue-800 dark:text-blue-200">
          <strong>Dica:</strong> Você pode realizar pagamentos parciais e acompanhar o saldo restante
          na página de detalhes da fatura. O histórico de todos os pagamentos ficará registrado.
        </p>
      </div>
    </div>
  );
}
