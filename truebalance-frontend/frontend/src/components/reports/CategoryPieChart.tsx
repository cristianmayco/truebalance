import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';
import type { CategoryExpense } from '@/services/reports.service';

interface CategoryPieChartProps {
  data: CategoryExpense[];
}

const COLORS = [
  '#8b5cf6', // violet
  '#a78bfa', // light violet
  '#6366f1', // indigo
  '#818cf8', // light indigo
  '#c084fc', // purple
  '#e879f9', // light purple
  '#f472b6', // pink
  '#fb923c', // orange
];

export default function CategoryPieChart({ data }: CategoryPieChartProps) {
  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload;
      return (
        <div className="bg-white dark:bg-gray-800 p-4 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700">
          <p className="text-sm font-semibold text-gray-900 dark:text-white mb-2">{data.category}</p>
          <div className="space-y-1">
            <p className="text-sm text-gray-600 dark:text-gray-400">
              Valor: {formatCurrency(data.amount)}
            </p>
            <p className="text-sm text-gray-600 dark:text-gray-400">
              Porcentagem: {data.percentage.toFixed(1)}%
            </p>
            <p className="text-sm text-gray-600 dark:text-gray-400">
              Quantidade: {data.count} {data.count === 1 ? 'item' : 'itens'}
            </p>
          </div>
        </div>
      );
    }
    return null;
  };

  const CustomLabel = ({ cx, cy, midAngle, innerRadius, outerRadius, percent }: any) => {
    const RADIAN = Math.PI / 180;
    const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
    const x = cx + radius * Math.cos(-midAngle * RADIAN);
    const y = cy + radius * Math.sin(-midAngle * RADIAN);

    if (percent < 0.05) return null; // Don't show label if less than 5%

    return (
      <text
        x={x}
        y={y}
        fill="white"
        textAnchor={x > cx ? 'start' : 'end'}
        dominantBaseline="central"
        className="text-sm font-semibold"
      >
        {`${(percent * 100).toFixed(0)}%`}
      </text>
    );
  };

  const CustomLegend = ({ payload }: any) => {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 gap-2 mt-6">
        {payload?.map((entry: any, index: number) => (
          <div
            key={`legend-${index}`}
            className="flex items-center gap-2 p-2 rounded hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors"
          >
            <div
              className="w-4 h-4 rounded-full flex-shrink-0"
              style={{ backgroundColor: entry.color }}
            />
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-gray-900 dark:text-white truncate">
                {entry.value}
              </p>
              <p className="text-xs text-gray-600 dark:text-gray-400">
                {formatCurrency(entry.payload.amount)}
              </p>
            </div>
          </div>
        ))}
      </div>
    );
  };

  if (!data || data.length === 0) {
    return (
      <div className="flex items-center justify-center h-[400px] text-gray-500 dark:text-gray-400">
        Nenhum dado disponível para exibir
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <ResponsiveContainer width="100%" height={400}>
        <PieChart>
          <Pie
            data={data as any}
            cx="50%"
            cy="50%"
            labelLine={false}
            label={CustomLabel}
            outerRadius={120}
            fill="#8884d8"
            dataKey="amount"
            nameKey="category"
          >
            {data.map((_entry, index) => (
              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip content={<CustomTooltip />} />
          <Legend content={<CustomLegend />} />
        </PieChart>
      </ResponsiveContainer>

      {/* Summary Table */}
      <div className="mt-6 overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
            <tr>
              <th className="px-4 py-3 text-left font-semibold text-gray-700 dark:text-gray-300">
                Categoria
              </th>
              <th className="px-4 py-3 text-right font-semibold text-gray-700 dark:text-gray-300">
                Valor
              </th>
              <th className="px-4 py-3 text-right font-semibold text-gray-700 dark:text-gray-300">
                %
              </th>
              <th className="px-4 py-3 text-right font-semibold text-gray-700 dark:text-gray-300">
                Qtd
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
            {data.map((item, index) => (
              <tr key={item.category} className="hover:bg-gray-50 dark:hover:bg-gray-800/50">
                <td className="px-4 py-3">
                  <div className="flex items-center gap-2">
                    <div
                      className="w-3 h-3 rounded-full flex-shrink-0"
                      style={{ backgroundColor: COLORS[index % COLORS.length] }}
                    />
                    <span className="text-gray-900 dark:text-white">{item.category}</span>
                  </div>
                </td>
                <td className="px-4 py-3 text-right text-gray-900 dark:text-white font-medium">
                  {formatCurrency(item.amount)}
                </td>
                <td className="px-4 py-3 text-right text-gray-600 dark:text-gray-400">
                  {item.percentage.toFixed(1)}%
                </td>
                <td className="px-4 py-3 text-right text-gray-600 dark:text-gray-400">
                  {item.count}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
