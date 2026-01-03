import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Calendar, TrendingUp, PieChart as PieChartIcon } from 'lucide-react';
import { reportsService } from '@/services/reports.service';
import ExpensesChart from '@/components/reports/ExpensesChart';
import CategoryPieChart from '@/components/reports/CategoryPieChart';
import MetricsCards from '@/components/reports/MetricsCards';
import Card from '@/components/ui/Card';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import Select from '@/components/ui/Select';
import { useExport } from '@/hooks/useExport';
import { formatMonthlyExpensesForExport, formatCategoryBreakdownForExport } from '@/utils/exportFormatters';
import { Download, Loader2 } from 'lucide-react';

export default function Reports() {
  const currentYear = new Date().getFullYear();
  const [selectedYear, setSelectedYear] = useState(currentYear);
  const { isExporting, exportMultiSheet } = useExport();

  // Fetch monthly expenses
  const {
    data: monthlyExpenses,
    isLoading: isLoadingMonthly,
    error: errorMonthly,
  } = useQuery({
    queryKey: ['monthlyExpenses', selectedYear],
    queryFn: () => reportsService.getMonthlyExpenses(selectedYear),
  });

  // Fetch expense metrics
  const {
    data: metrics,
    isLoading: isLoadingMetrics,
    error: errorMetrics,
  } = useQuery({
    queryKey: ['expenseMetrics', selectedYear],
    queryFn: () => reportsService.getExpenseMetrics(selectedYear),
  });

  // Fetch category breakdown
  const {
    data: categoryData,
    isLoading: isLoadingCategories,
    error: errorCategories,
  } = useQuery({
    queryKey: ['categoryBreakdown', selectedYear],
    queryFn: () => {
      const startDate = new Date(selectedYear, 0, 1);
      const endDate = new Date(selectedYear, 11, 31, 23, 59, 59);
      return reportsService.getCategoryBreakdown(startDate, endDate);
    },
  });

  // Generate year options (current year and 5 years back)
  const yearOptions = Array.from({ length: 6 }, (_, i) => {
    const year = currentYear - i;
    return { value: year.toString(), label: year.toString() };
  });

  const isLoading = isLoadingMonthly || isLoadingMetrics || isLoadingCategories;
  const hasError = errorMonthly || errorMetrics || errorCategories;

  const handleExportReport = () => {
    if (!monthlyExpenses || !categoryData || !metrics) return;

    const sheets = [
      {
        name: 'Gastos Mensais',
        data: formatMonthlyExpensesForExport(monthlyExpenses),
      },
      {
        name: 'Categorias',
        data: formatCategoryBreakdownForExport(categoryData),
      },
      {
        name: 'Resumo',
        data: [
          { Métrica: 'Total Gasto', Valor: `R$ ${metrics.totalExpenses?.toFixed(2) || '0.00'}` },
          { Métrica: 'Média Mensal', Valor: `R$ ${metrics.averageMonthly?.toFixed(2) || '0.00'}` },
          { Métrica: 'Maior Gasto', Valor: `R$ ${metrics.highestMonth?.amount?.toFixed(2) || '0.00'}` },
          { Métrica: 'Menor Gasto', Valor: `R$ ${metrics.lowestMonth?.amount?.toFixed(2) || '0.00'}` },
        ],
      },
    ];

    exportMultiSheet(sheets, `relatorio_financeiro_${selectedYear}`);
  };

  if (hasError) {
    return (
      <div className="container mx-auto px-4 py-8">
        <Card className="p-8 text-center">
          <p className="text-red-600 dark:text-red-400">
            Erro ao carregar relatórios. Tente novamente mais tarde.
          </p>
        </Card>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
            Relatórios Financeiros
          </h1>
          <p className="text-gray-600 dark:text-gray-400">
            Análise detalhada dos seus gastos e despesas
          </p>
        </div>

        {/* Actions */}
        <div className="flex flex-col sm:flex-row gap-3 items-stretch sm:items-center">
          {/* Export Button */}
          {!isLoading && monthlyExpenses && categoryData && metrics && (
            <button
              onClick={handleExportReport}
              disabled={isExporting}
              className="inline-flex items-center justify-center gap-2 px-4 py-2 text-sm font-medium rounded-lg border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-800 text-gray-700 dark:text-gray-300 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isExporting ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <Download className="w-4 h-4" />
              )}
              {isExporting ? 'Exportando...' : 'Exportar Relatório'}
            </button>
          )}

          {/* Year Selector */}
          <div className="w-full sm:w-48">
            <Select
              label="Ano"
              value={selectedYear.toString()}
              onChange={(e) => setSelectedYear(Number(e.target.value))}
              options={yearOptions}
            />
          </div>
        </div>
      </div>

      {/* Loading State */}
      {isLoading && (
        <div className="flex items-center justify-center min-h-[400px]">
          <LoadingSpinner size="lg" />
        </div>
      )}

      {/* Content */}
      {!isLoading && metrics && monthlyExpenses && categoryData && (
        <>
          {/* Metrics Cards */}
          <section>
            <MetricsCards metrics={metrics} />
          </section>

          {/* Monthly Expenses Chart */}
          <section>
            <Card className="p-6">
              <div className="flex items-center gap-3 mb-6">
                <div className="p-2 bg-violet-100 dark:bg-violet-900/30 rounded-lg">
                  <TrendingUp className="w-5 h-5 text-violet-600 dark:text-violet-400" />
                </div>
                <div>
                  <h2 className="text-xl font-semibold text-gray-900 dark:text-white">
                    Gastos Mensais
                  </h2>
                  <p className="text-sm text-gray-600 dark:text-gray-400">
                    Evolução dos gastos ao longo do ano
                  </p>
                </div>
              </div>

              {monthlyExpenses.length > 0 ? (
                <ExpensesChart data={monthlyExpenses} />
              ) : (
                <div className="flex items-center justify-center h-[400px] text-gray-500 dark:text-gray-400">
                  Nenhum dado disponível para o ano selecionado
                </div>
              )}
            </Card>
          </section>

          {/* Category Distribution */}
          <section>
            <Card className="p-6">
              <div className="flex items-center gap-3 mb-6">
                <div className="p-2 bg-purple-100 dark:bg-purple-900/30 rounded-lg">
                  <PieChartIcon className="w-5 h-5 text-purple-600 dark:text-purple-400" />
                </div>
                <div>
                  <h2 className="text-xl font-semibold text-gray-900 dark:text-white">
                    Distribuição por Categoria
                  </h2>
                  <p className="text-sm text-gray-600 dark:text-gray-400">
                    Breakdown dos gastos por tipo
                  </p>
                </div>
              </div>

              {categoryData.length > 0 ? (
                <CategoryPieChart data={categoryData} />
              ) : (
                <div className="flex items-center justify-center h-[400px] text-gray-500 dark:text-gray-400">
                  Nenhum dado disponível para exibir
                </div>
              )}
            </Card>
          </section>

          {/* Insights Section */}
          <section>
            <Card className="p-6 bg-gradient-to-br from-violet-50 to-purple-50 dark:from-violet-900/20 dark:to-purple-900/20">
              <div className="flex items-center gap-3 mb-4">
                <div className="p-2 bg-violet-600 rounded-lg">
                  <Calendar className="w-5 h-5 text-white" />
                </div>
                <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Insights</h2>
              </div>

              <div className="space-y-3 text-sm">
                <p className="text-gray-700 dark:text-gray-300">
                  Seu gasto médio mensal em {selectedYear} foi de{' '}
                  <strong className="text-violet-600 dark:text-violet-400">
                    {new Intl.NumberFormat('pt-BR', {
                      style: 'currency',
                      currency: 'BRL',
                    }).format(metrics.averageMonthly)}
                  </strong>
                  .
                </p>

                {metrics.periodComparison.percentageChange !== 0 && (
                  <p className="text-gray-700 dark:text-gray-300">
                    Comparado com o período anterior, seus gastos{' '}
                    {metrics.periodComparison.percentageChange > 0 ? (
                      <>
                        <strong className="text-red-600 dark:text-red-400">aumentaram</strong> em{' '}
                        {Math.abs(metrics.periodComparison.percentageChange).toFixed(1)}%
                      </>
                    ) : (
                      <>
                        <strong className="text-green-600 dark:text-green-400">diminuíram</strong> em{' '}
                        {Math.abs(metrics.periodComparison.percentageChange).toFixed(1)}%
                      </>
                    )}
                    .
                  </p>
                )}

                {categoryData.length > 0 && (
                  <p className="text-gray-700 dark:text-gray-300">
                    A maior parte dos seus gastos está em{' '}
                    <strong className="text-violet-600 dark:text-violet-400">
                      {categoryData[0].category}
                    </strong>
                    , representando {categoryData[0].percentage.toFixed(1)}% do total.
                  </p>
                )}
              </div>
            </Card>
          </section>
        </>
      )}
    </div>
  );
}
