import { Link } from 'react-router-dom';
import { Calendar, Clock, AlertCircle, ChevronRight } from 'lucide-react';
import { useBills } from '@/hooks/useBills';
import Card from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import EmptyState from '@/components/ui/EmptyState';
import { isToday, isTomorrow, differenceInDays, formatDistanceToNow } from 'date-fns';
import { ptBR } from 'date-fns/locale';

export default function UpcomingBills() {
  const { data: billsResponse, isLoading, error } = useBills({
    sort: 'executionDate,asc',
  });

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value);
  };

  const formatDate = (date: string | Date) => {
    const d = typeof date === 'string' ? new Date(date) : date;
    return d.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' });
  };

  const getUrgencyBadge = (date: string | Date) => {
    const d = typeof date === 'string' ? new Date(date) : date;
    const today = new Date();

    if (isToday(d)) {
      return <Badge variant="error">Vence Hoje</Badge>;
    }

    if (isTomorrow(d)) {
      return <Badge variant="warning">Vence Amanhã</Badge>;
    }

    const days = differenceInDays(d, today);

    if (days < 0) {
      return <Badge variant="error">Vencida</Badge>;
    }

    if (days <= 7) {
      return <Badge variant="warning">{days} dias</Badge>;
    }

    return <Badge variant="info">{days} dias</Badge>;
  };

  const getRelativeTime = (date: string | Date) => {
    const d = typeof date === 'string' ? new Date(date) : date;
    return formatDistanceToNow(d, { addSuffix: true, locale: ptBR });
  };

  if (isLoading) {
    return (
      <Card className="p-6">
        <div className="flex items-center justify-center min-h-[200px]">
          <LoadingSpinner size="md" />
        </div>
      </Card>
    );
  }

  if (error) {
    return (
      <Card className="p-6">
        <div className="text-center text-red-600 dark:text-red-400">
          <AlertCircle className="w-12 h-12 mx-auto mb-2" />
          <p>Erro ao carregar contas</p>
        </div>
      </Card>
    );
  }

  // Filter and sort upcoming bills (next 30 days, not paid)
  const today = new Date();
  const thirtyDaysFromNow = new Date(today);
  thirtyDaysFromNow.setDate(thirtyDaysFromNow.getDate() + 30);

  const bills = billsResponse?.content || [];
  
  const upcomingBills = bills
    .filter((bill: any) => {
      const billDate = new Date(bill.billDate || bill.date || bill.executionDate);
      return billDate >= today && billDate <= thirtyDaysFromNow && !bill.isPaid;
    })
    .sort((a: any, b: any) => {
      const dateA = new Date(a.billDate || a.date || a.executionDate).getTime();
      const dateB = new Date(b.billDate || b.date || b.executionDate).getTime();
      return dateA - dateB;
    })
    .slice(0, 5);

  if (upcomingBills.length === 0) {
    return (
      <Card className="p-6">
        <div className="flex items-center gap-3 mb-6">
          <div className="p-2 bg-violet-100 dark:bg-violet-900/30 rounded-lg">
            <Calendar className="w-5 h-5 text-violet-600 dark:text-violet-400" />
          </div>
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Próximas Contas</h2>
        </div>

        <EmptyState
          icon={Calendar as any}
          message="Nenhuma conta próxima"
          description="Você não tem contas a vencer nos próximos 30 dias"
        />
      </Card>
    );
  }

  return (
    <Card className="p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-violet-100 dark:bg-violet-900/30 rounded-lg">
            <Calendar className="w-5 h-5 text-violet-600 dark:text-violet-400" />
          </div>
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Próximas Contas</h2>
        </div>

        <Link
          to="/bills"
          className="text-sm text-violet-600 dark:text-violet-400 hover:text-violet-700 dark:hover:text-violet-300 font-medium flex items-center gap-1"
        >
          Ver todas
          <ChevronRight className="w-4 h-4" />
        </Link>
      </div>

      {/* Bills List */}
      <div className="space-y-3">
        {upcomingBills.map((bill: any) => {
          const installmentValue = bill.totalAmount / bill.numberOfInstallments;
          const billDate = bill.billDate || bill.date || bill.executionDate;

          return (
            <Link
              key={bill.id}
              to={`/bills/${bill.id}`}
              className="block p-4 rounded-lg border border-gray-200 dark:border-gray-700 hover:border-violet-300 dark:hover:border-violet-600 hover:shadow-md transition-all group"
            >
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <h3 className="text-sm font-medium text-gray-900 dark:text-white truncate group-hover:text-violet-600 dark:group-hover:text-violet-400">
                      {bill.name}
                    </h3>
                  </div>

                  <div className="flex items-center gap-3 text-xs text-gray-500 dark:text-gray-400">
                    <span className="flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      {formatDate(billDate)}
                    </span>
                    {bill.numberOfInstallments > 1 && (
                      <span className="text-gray-400 dark:text-gray-600">•</span>
                    )}
                    {bill.numberOfInstallments > 1 && (
                      <span>Parcela 1/{bill.numberOfInstallments}</span>
                    )}
                  </div>

                  <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                    {getRelativeTime(billDate)}
                  </p>
                </div>

                <div className="flex flex-col items-end gap-2">
                  <p className="text-sm font-semibold text-gray-900 dark:text-white whitespace-nowrap">
                    {formatCurrency(installmentValue)}
                  </p>
                  {getUrgencyBadge(billDate)}
                </div>
              </div>
            </Link>
          );
        })}
      </div>

      {/* Total */}
      <div className="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
        <div className="flex justify-between items-center">
          <span className="text-sm font-medium text-gray-600 dark:text-gray-400">
            Total a pagar (próximos 30 dias)
          </span>
          <span className="text-lg font-bold text-violet-600 dark:text-violet-400">
            {formatCurrency(
              upcomingBills.reduce((sum: number, bill: any) => sum + bill.totalAmount / bill.numberOfInstallments, 0)
            )}
          </span>
        </div>
      </div>
    </Card>
  );
}
