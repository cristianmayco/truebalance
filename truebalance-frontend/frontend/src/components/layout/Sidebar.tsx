import { NavLink } from 'react-router-dom'
import { Home, FileText, CreditCard, BarChart3, Layers } from 'lucide-react'

interface NavItem {
  label: string
  icon: React.ReactNode
  href: string
}

const navItems: NavItem[] = [
  { label: 'Dashboard', icon: <Home className="w-5 h-5" />, href: '/' },
  { label: 'Contas', icon: <FileText className="w-5 h-5" />, href: '/bills' },
  { label: 'Cartões', icon: <CreditCard className="w-5 h-5" />, href: '/credit-cards' },
  { label: 'Relatórios', icon: <BarChart3 className="w-5 h-5" />, href: '/reports' },
  { label: 'Consolidado', icon: <Layers className="w-5 h-5" />, href: '/consolidated' },
]

export function Sidebar() {
  return (
    <aside className="hidden lg:flex lg:flex-col lg:w-64 lg:h-screen lg:fixed lg:left-0 lg:top-0 bg-white dark:bg-slate-800 border-r border-gray-200 dark:border-slate-700">
      {/* Logo/Branding */}
      <div className="p-6 border-b border-gray-200 dark:border-slate-700">
        <h1 className="text-2xl font-bold text-primary-600 dark:text-primary-400">
          TrueBalance
        </h1>
        <p className="text-sm text-gray-600 dark:text-slate-400 mt-1">
          Gestão Financeira
        </p>
      </div>

      {/* Navigation Links */}
      <nav className="flex-1 p-4 space-y-1">
        {navItems.map((item) => (
          <NavLink
            key={item.href}
            to={item.href}
            className={({ isActive }) => `
              flex items-center gap-3 px-4 py-3 rounded-lg
              transition-colors duration-200
              ${
                isActive
                  ? 'bg-primary-100 dark:bg-primary-900/30 text-primary-700 dark:text-primary-300'
                  : 'text-gray-700 dark:text-slate-300 hover:bg-gray-100 dark:hover:bg-slate-700'
              }
            `}
          >
            {item.icon}
            <span className="font-medium">{item.label}</span>
          </NavLink>
        ))}
      </nav>

      {/* Footer */}
      <div className="p-4 border-t border-gray-200 dark:border-slate-700">
        <p className="text-xs text-center text-gray-500 dark:text-slate-500">
          v1.0.0
        </p>
      </div>
    </aside>
  )
}
