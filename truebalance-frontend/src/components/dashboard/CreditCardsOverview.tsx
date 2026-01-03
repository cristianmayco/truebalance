import { Link } from 'react-router-dom';
import { CreditCard, ChevronRight, AlertCircle } from 'lucide-react';
import { useCreditCards } from '../../hooks/useCreditCards';
import Card from '../ui/Card';
import ProgressBar from '../ui/ProgressBar';
import LoadingSpinner from '../ui/LoadingSpinner';
import EmptyState from '../ui/EmptyState';

export default function CreditCardsOverview() {
  const { data: creditCards, isLoading, error } = useCreditCards();

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value);
  };

  const calculateUsagePercentage = (card: any) => {
    if (!card.availableLimit || card.limit === 0) return 0;
    const used = card.limit - card.availableLimit;
    return (used / card.limit) * 100;
  };

  const getProgressVariant = (percentage: number): 'success' | 'warning' | 'danger' => {
    if (percentage >= 80) return 'danger';
    if (percentage >= 60) return 'warning';
    return 'success';
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
          <p>Erro ao carregar cartões</p>
        </div>
      </Card>
    );
  }

  if (!creditCards || creditCards.length === 0) {
    return (
      <Card className="p-6">
        <div className="flex items-center gap-3 mb-6">
          <div className="p-2 bg-purple-100 dark:bg-purple-900/30 rounded-lg">
            <CreditCard className="w-5 h-5 text-purple-600 dark:text-purple-400" />
          </div>
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
            Cartões de Crédito
          </h2>
        </div>

        <EmptyState
          icon={CreditCard}
          message="Nenhum cartão cadastrado"
          description="Adicione seus cartões para acompanhar os gastos"
          ctaText="Adicionar Cartão"
          ctaLink="/credit-cards/new"
        />
      </Card>
    );
  }

  // Calculate totals
  const totalLimit = creditCards.reduce((sum, card) => sum + card.limit, 0);
  const totalAvailable = creditCards.reduce((sum, card) => sum + (card.availableLimit || card.limit), 0);
  const totalUsed = totalLimit - totalAvailable;
  const totalUsagePercentage = totalLimit > 0 ? (totalUsed / totalLimit) * 100 : 0;

  return (
    <Card className="p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-purple-100 dark:bg-purple-900/30 rounded-lg">
            <CreditCard className="w-5 h-5 text-purple-600 dark:text-purple-400" />
          </div>
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
            Cartões de Crédito
          </h2>
        </div>

        <Link
          to="/credit-cards"
          className="text-sm text-violet-600 dark:text-violet-400 hover:text-violet-700 dark:hover:text-violet-300 font-medium flex items-center gap-1"
        >
          Ver todos
          <ChevronRight className="w-4 h-4" />
        </Link>
      </div>

      {/* Total Summary */}
      <div className="mb-6 p-4 bg-gradient-to-br from-purple-50 to-violet-50 dark:from-purple-900/20 dark:to-violet-900/20 rounded-lg">
        <div className="flex justify-between items-center mb-3">
          <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
            Total Utilizado
          </span>
          <span className="text-lg font-bold text-purple-600 dark:text-purple-400">
            {formatCurrency(totalUsed)}
          </span>
        </div>
        <div className="flex justify-between items-center text-xs text-gray-600 dark:text-gray-400 mb-2">
          <span>Disponível: {formatCurrency(totalAvailable)}</span>
          <span>Limite: {formatCurrency(totalLimit)}</span>
        </div>
        <ProgressBar
          percentage={totalUsagePercentage}
          variant={getProgressVariant(totalUsagePercentage)}
        />
      </div>

      {/* Cards List */}
      <div className="space-y-3">
        {creditCards.map((card) => {
          const usagePercentage = calculateUsagePercentage(card);
          const usedAmount = card.limit - (card.availableLimit || card.limit);

          return (
            <Link
              key={card.id}
              to={`/credit-cards/${card.id}`}
              className="block p-4 rounded-lg border border-gray-200 dark:border-gray-700 hover:border-purple-300 dark:hover:border-purple-600 hover:shadow-md transition-all group"
            >
              <div className="flex items-center justify-between mb-3">
                <h3 className="text-sm font-medium text-gray-900 dark:text-white group-hover:text-purple-600 dark:group-hover:text-purple-400">
                  {card.name}
                </h3>
                <span className="text-xs text-gray-500 dark:text-gray-400">
                  {usagePercentage.toFixed(0)}% usado
                </span>
              </div>

              <div className="space-y-2">
                <ProgressBar
                  percentage={usagePercentage}
                  variant={getProgressVariant(usagePercentage)}
                  size="sm"
                />

                <div className="flex justify-between text-xs text-gray-600 dark:text-gray-400">
                  <span>
                    Usado: <strong className="text-gray-900 dark:text-white">{formatCurrency(usedAmount)}</strong>
                  </span>
                  <span>
                    Disponível: <strong className="text-gray-900 dark:text-white">{formatCurrency(card.availableLimit || card.limit)}</strong>
                  </span>
                </div>

                <div className="flex justify-between text-xs text-gray-500 dark:text-gray-500 pt-2 border-t border-gray-200 dark:border-gray-700">
                  <span>Fecha dia {card.closingDay}</span>
                  <span>Vence dia {card.dueDay}</span>
                </div>
              </div>
            </Link>
          );
        })}
      </div>

      {/* Add Card CTA */}
      {creditCards.length < 5 && (
        <Link
          to="/credit-cards/new"
          className="mt-4 block text-center py-3 px-4 rounded-lg border-2 border-dashed border-gray-300 dark:border-gray-700 hover:border-purple-400 dark:hover:border-purple-600 text-sm font-medium text-gray-600 dark:text-gray-400 hover:text-purple-600 dark:hover:text-purple-400 transition-colors"
        >
          + Adicionar Novo Cartão
        </Link>
      )}
    </Card>
  );
}
