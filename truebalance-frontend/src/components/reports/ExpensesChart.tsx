import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { useState } from 'react';
import type { MonthlyExpense } from '../../services/reports.service';

interface ExpensesChartProps {
  data: MonthlyExpense[];
  type?: 'line' | 'bar';
}

export default function ExpensesChart({ data, type = 'bar' }: ExpensesChartProps) {
  const [chartType, setChartType] = useState<'line' | 'bar'>(type);

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white dark:bg-gray-800 p-4 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700">
          <p className="text-sm font-semibold text-gray-900 dark:text-white mb-2">{label}</p>
          <div className="space-y-1">
            <p className="text-sm text-violet-600 dark:text-violet-400">
              Contas: {formatCurrency(payload[0]?.value || 0)}
            </p>
            <p className="text-sm text-purple-600 dark:text-purple-400">
              Cartões: {formatCurrency(payload[1]?.value || 0)}
            </p>
            <p className="text-sm font-semibold text-gray-900 dark:text-white border-t border-gray-200 dark:border-gray-700 pt-1 mt-1">
              Total: {formatCurrency(payload[2]?.value || 0)}
            </p>
          </div>
        </div>
      );
    }
    return null;
  };

  // Format data for chart
  const chartData = data.map((item) => ({
    name: `${item.month.substring(0, 3)}/${item.year}`,
    Contas: item.bills,
    'Cartões de Crédito': item.creditCards,
    Total: item.total,
  }));

  return (
    <div className="space-y-4">
      {/* Chart Type Toggle */}
      <div className="flex justify-end gap-2">
        <button
          onClick={() => setChartType('bar')}
          className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
            chartType === 'bar'
              ? 'bg-violet-600 text-white'
              : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'
          }`}
        >
          Barras
        </button>
        <button
          onClick={() => setChartType('line')}
          className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
            chartType === 'line'
              ? 'bg-violet-600 text-white'
              : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'
          }`}
        >
          Linha
        </button>
      </div>

      {/* Chart */}
      <ResponsiveContainer width="100%" height={400}>
        {chartType === 'bar' ? (
          <BarChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" className="stroke-gray-200 dark:stroke-gray-700" />
            <XAxis
              dataKey="name"
              className="text-sm"
              tick={{ fill: 'currentColor', className: 'fill-gray-600 dark:fill-gray-400' }}
            />
            <YAxis
              className="text-sm"
              tick={{ fill: 'currentColor', className: 'fill-gray-600 dark:fill-gray-400' }}
              tickFormatter={formatCurrency}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend
              wrapperStyle={{
                paddingTop: '20px',
              }}
              iconType="circle"
            />
            <Bar dataKey="Contas" fill="#8b5cf6" radius={[8, 8, 0, 0]} />
            <Bar dataKey="Cartões de Crédito" fill="#a78bfa" radius={[8, 8, 0, 0]} />
            <Bar dataKey="Total" fill="#6366f1" radius={[8, 8, 0, 0]} />
          </BarChart>
        ) : (
          <LineChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" className="stroke-gray-200 dark:stroke-gray-700" />
            <XAxis
              dataKey="name"
              className="text-sm"
              tick={{ fill: 'currentColor', className: 'fill-gray-600 dark:fill-gray-400' }}
            />
            <YAxis
              className="text-sm"
              tick={{ fill: 'currentColor', className: 'fill-gray-600 dark:fill-gray-400' }}
              tickFormatter={formatCurrency}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend
              wrapperStyle={{
                paddingTop: '20px',
              }}
              iconType="circle"
            />
            <Line
              type="monotone"
              dataKey="Contas"
              stroke="#8b5cf6"
              strokeWidth={2}
              dot={{ fill: '#8b5cf6', r: 4 }}
              activeDot={{ r: 6 }}
            />
            <Line
              type="monotone"
              dataKey="Cartões de Crédito"
              stroke="#a78bfa"
              strokeWidth={2}
              dot={{ fill: '#a78bfa', r: 4 }}
              activeDot={{ r: 6 }}
            />
            <Line
              type="monotone"
              dataKey="Total"
              stroke="#6366f1"
              strokeWidth={3}
              dot={{ fill: '#6366f1', r: 5 }}
              activeDot={{ r: 7 }}
            />
          </LineChart>
        )}
      </ResponsiveContainer>
    </div>
  );
}
