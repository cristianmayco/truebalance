import { useQuery } from '@tanstack/react-query';
import { LayoutDashboard, DollarSign, TrendingUp, Calendar } from 'lucide-react';
import { reportsService } from '@/services/reports.service';
import UpcomingBills from '@/components/dashboard/UpcomingBills';
import CreditCardsOverview from '@/components/dashboard/CreditCardsOverview';
import ExpensesTimeline from '@/components/dashboard/ExpensesTimeline';
import Card from '@/components/ui/Card';
import LoadingSpinner from '@/components/ui/LoadingSpinner';

export default function ConsolidatedView() {
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth();

  // Fetch consolidated data
  const {
    data: consolidatedData,
    isLoading: isLoadingConsolidated,
  } = useQuery({
    queryKey: ['consolidatedSummary'],
    queryFn: () => reportsService.getConsolidatedSummary(),
  });

  // Fetch expense metrics
  const {
    data: metrics,
    isLoading: isLoadingMetrics,
  } = useQuery({
    queryKey: ['expenseMetrics', currentYear],
    queryFn: () => reportsService.getExpenseMetrics(currentYear),
  });

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value);
  };

  const isLoading = isLoadingConsolidated || isLoadingMetrics;

  // Calculate summary data
  const calculateSummary = () => {
    if (!consolidatedData) return null;

    const { bills, invoices, creditCards } = consolidatedData;

    // Total bills this month
    const thisMonthBills = bills.filter((bill: any) => {
      const billDate = new Date(bill.billDate);
      return billDate.getMonth() === currentMonth && billDate.getFullYear() === currentYear;
    });

    const totalBillsThisMonth = thisMonthBills.reduce(
      (sum: number, bill: any) => sum + bill.totalAmount / bill.installments,
      0
    );

    // Total invoices this month
    const thisMonthInvoices = invoices.filter((invoice: any) => {
      // Parse date string as local date to avoid timezone issues
      const [year, month, day] = invoice.referenceMonth.split('-').map(Number);
      const invoiceDate = new Date(year, month - 1, day); // month is 0-indexed
      return invoiceDate.getMonth() === currentMonth && invoiceDate.getFullYear() === currentYear;
    });

    const totalInvoicesThisMonth = thisMonthInvoices.reduce(
      (sum: number, invoice: any) => sum + invoice.totalAmount,
      0
    );

    // Total credit limit
    const totalCreditLimit = creditCards.reduce((sum: number, card: any) => sum + card.limit, 0);
    const totalAvailableLimit = creditCards.reduce(
      (sum: number, card: any) => sum + (card.availableLimit || card.limit),
      0
    );
    const totalUsedCredit = totalCreditLimit - totalAvailableLimit;

    // Pending bills
    const today = new Date();
    const pendingBills = bills.filter((bill: any) => {
      return new Date(bill.billDate) >= today && !bill.isPaid;
    });

    return {
      totalBillsThisMonth,
      totalInvoicesThisMonth,
      totalThisMonth: totalBillsThisMonth + totalInvoicesThisMonth,
      totalCreditLimit,
      totalAvailableLimit,
      totalUsedCredit,
      creditUsagePercentage: totalCreditLimit > 0 ? (totalUsedCredit / totalCreditLimit) * 100 : 0,
      pendingBillsCount: pendingBills.length,
      totalBillsCount: bills.length,
      totalInvoicesCount: invoices.length,
      totalCardsCount: creditCards.length,
    };
  };

  const summary = calculateSummary();

  return (
    <div className="container mx-auto px-4 py-8 space-y-8">
      {/* Header */}
      <div className="flex items-center gap-4">
        <div className="p-3 bg-gradient-to-br from-violet-500 to-purple-600 rounded-xl shadow-lg">
          <LayoutDashboard className="w-8 h-8 text-white" />
        </div>
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Visão 360°</h1>
          <p className="text-gray-600 dark:text-gray-400">
            Panorama completo das suas finanças
          </p>
        </div>
      </div>

      {/* Loading State */}
      {isLoading && (
        <div className="flex items-center justify-center min-h-[400px]">
          <LoadingSpinner size="lg" />
        </div>
      )}

      {/* Content */}
      {!isLoading && summary && metrics && (
        <>
          {/* Summary Cards Row */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {/* This Month Total */}
            <Card className="p-6 bg-gradient-to-br from-violet-50 to-purple-50 dark:from-violet-900/20 dark:to-purple-900/20 hover:shadow-lg transition-shadow">
              <div className="flex items-center gap-3 mb-4">
                <div className="p-3 bg-violet-600 rounded-lg">
                  <DollarSign className="w-6 h-6 text-white" />
                </div>
                <h3 className="text-sm font-medium text-gray-600 dark:text-gray-400">
                  Gastos Este Mês
                </h3>
              </div>
              <p className="text-3xl font-bold text-violet-600 dark:text-violet-400">
                {formatCurrency(summary.totalThisMonth)}
              </p>
              <div className="mt-3 text-xs text-gray-600 dark:text-gray-400 space-y-1">
                <p>Contas: {formatCurrency(summary.totalBillsThisMonth)}</p>
                <p>Cartões: {formatCurrency(summary.totalInvoicesThisMonth)}</p>
              </div>
            </Card>

            {/* Credit Usage */}
            <Card className="p-6 bg-gradient-to-br from-purple-50 to-pink-50 dark:from-purple-900/20 dark:to-pink-900/20 hover:shadow-lg transition-shadow">
              <div className="flex items-center gap-3 mb-4">
                <div className="p-3 bg-purple-600 rounded-lg">
                  <TrendingUp className="w-6 h-6 text-white" />
                </div>
                <h3 className="text-sm font-medium text-gray-600 dark:text-gray-400">
                  Limite Utilizado
                </h3>
              </div>
              <p className="text-3xl font-bold text-purple-600 dark:text-purple-400">
                {summary.creditUsagePercentage.toFixed(1)}%
              </p>
              <div className="mt-3 text-xs text-gray-600 dark:text-gray-400 space-y-1">
                <p>Usado: {formatCurrency(summary.totalUsedCredit)}</p>
                <p>Disponível: {formatCurrency(summary.totalAvailableLimit)}</p>
              </div>
            </Card>

            {/* Average Monthly */}
            <Card className="p-6 bg-gradient-to-br from-blue-50 to-cyan-50 dark:from-blue-900/20 dark:to-cyan-900/20 hover:shadow-lg transition-shadow">
              <div className="flex items-center gap-3 mb-4">
                <div className="p-3 bg-blue-600 rounded-lg">
                  <Calendar className="w-6 h-6 text-white" />
                </div>
                <h3 className="text-sm font-medium text-gray-600 dark:text-gray-400">
                  Média Mensal
                </h3>
              </div>
              <p className="text-3xl font-bold text-blue-600 dark:text-blue-400">
                {formatCurrency(metrics.averageMonthly)}
              </p>
              <p className="mt-3 text-xs text-gray-600 dark:text-gray-400">
                Baseado em {new Date().getMonth() + 1} meses
              </p>
            </Card>

            {/* Pending Items */}
            <Card className="p-6 bg-gradient-to-br from-orange-50 to-amber-50 dark:from-orange-900/20 dark:to-amber-900/20 hover:shadow-lg transition-shadow">
              <div className="flex items-center gap-3 mb-4">
                <div className="p-3 bg-orange-600 rounded-lg">
                  <Calendar className="w-6 h-6 text-white" />
                </div>
                <h3 className="text-sm font-medium text-gray-600 dark:text-gray-400">
                  Itens Pendentes
                </h3>
              </div>
              <p className="text-3xl font-bold text-orange-600 dark:text-orange-400">
                {summary.pendingBillsCount}
              </p>
              <p className="mt-3 text-xs text-gray-600 dark:text-gray-400">
                {summary.pendingBillsCount === 1 ? 'conta a pagar' : 'contas a pagar'}
              </p>
            </Card>
          </div>

          {/* Main Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Upcoming Bills */}
            <div className="lg:col-span-1">
              <UpcomingBills />
            </div>

            {/* Credit Cards Overview */}
            <div className="lg:col-span-1">
              <CreditCardsOverview />
            </div>
          </div>

          {/* Expenses Timeline - Full Width */}
          <div>
            <ExpensesTimeline />
          </div>

          {/* Quick Stats Footer */}
          <Card className="p-6 bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-800 dark:to-gray-900">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
              Estatísticas Rápidas
            </h3>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
              <div className="text-center">
                <p className="text-2xl font-bold text-violet-600 dark:text-violet-400">
                  {summary.totalBillsCount}
                </p>
                <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                  {summary.totalBillsCount === 1 ? 'Conta' : 'Contas'}
                </p>
              </div>
              <div className="text-center">
                <p className="text-2xl font-bold text-purple-600 dark:text-purple-400">
                  {summary.totalCardsCount}
                </p>
                <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                  {summary.totalCardsCount === 1 ? 'Cartão' : 'Cartões'}
                </p>
              </div>
              <div className="text-center">
                <p className="text-2xl font-bold text-blue-600 dark:text-blue-400">
                  {summary.totalInvoicesCount}
                </p>
                <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                  {summary.totalInvoicesCount === 1 ? 'Fatura' : 'Faturas'}
                </p>
              </div>
              <div className="text-center">
                <p className="text-2xl font-bold text-green-600 dark:text-green-400">
                  {formatCurrency(summary.totalAvailableLimit)}
                </p>
                <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">Limite Disponível</p>
              </div>
            </div>
          </Card>
        </>
      )}
    </div>
  );
}
