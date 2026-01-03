import { ReactNode } from 'react'

interface Column {
  key: string
  label: string
  width?: string
}

interface TableProps {
  columns: Column[]
  data: any[]
  renderRow: (item: any, index: number) => ReactNode
  emptyMessage?: string
}

export function Table({ columns, data, renderRow, emptyMessage = 'Nenhum dado encontrado' }: TableProps) {
  if (data.length === 0) {
    return (
      <div className="text-center py-12 text-gray-500 dark:text-slate-400">
        {emptyMessage}
      </div>
    )
  }

  return (
    <div className="w-full overflow-x-auto">
      <table className="w-full">
        <thead className="bg-gray-50 dark:bg-slate-800 border-b border-gray-200 dark:border-slate-700">
          <tr>
            {columns.map((column) => (
              <th
                key={column.key}
                className="px-4 py-3 text-left text-sm font-semibold text-gray-700 dark:text-slate-300"
                style={{ width: column.width }}
              >
                {column.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200 dark:divide-slate-700">
          {data.map((item, index) => renderRow(item, index))}
        </tbody>
      </table>
    </div>
  )
}

interface TableRowProps {
  children: ReactNode
  onClick?: () => void
}

export function TableRow({ children, onClick }: TableRowProps) {
  return (
    <tr
      className={`bg-white dark:bg-slate-800 transition-colors ${
        onClick ? 'hover:bg-gray-50 dark:hover:bg-slate-700 cursor-pointer' : ''
      }`}
      onClick={onClick}
    >
      {children}
    </tr>
  )
}

interface TableCellProps {
  children: ReactNode
  className?: string
}

export function TableCell({ children, className = '' }: TableCellProps) {
  return (
    <td className={`px-4 py-3 text-sm text-gray-900 dark:text-white ${className}`}>
      {children}
    </td>
  )
}
