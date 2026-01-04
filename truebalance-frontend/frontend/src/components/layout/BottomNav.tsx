import { NavLink } from 'react-router-dom'
import { Home, FileText, CreditCard, BarChart3, Tag } from 'lucide-react'

interface NavItem {
  label: string
  icon: React.ReactNode
  href: string
}

const navItems: NavItem[] = [
  { label: 'Início', icon: <Home className="w-5 h-5" />, href: '/' },
  { label: 'Contas', icon: <FileText className="w-5 h-5" />, href: '/bills' },
  { label: 'Categorias', icon: <Tag className="w-5 h-5" />, href: '/categories' },
  { label: 'Cartões', icon: <CreditCard className="w-5 h-5" />, href: '/credit-cards' },
  { label: 'Relatórios', icon: <BarChart3 className="w-5 h-5" />, href: '/reports' },
]

export function BottomNav() {
  return (
    <nav className="lg:hidden fixed bottom-0 left-0 right-0 z-50 bg-white dark:bg-slate-800 border-t border-gray-200 dark:border-slate-700 h-16">
      <div className="flex justify-around items-center h-full">
        {navItems.map((item) => (
          <NavLink
            key={item.href}
            to={item.href}
            className={({ isActive }) => `
              flex flex-col items-center justify-center gap-1 px-3 py-2 min-w-[60px]
              transition-colors duration-200
              ${
                isActive
                  ? 'text-primary-600 dark:text-primary-400'
                  : 'text-gray-600 dark:text-slate-400'
              }
            `}
          >
            {item.icon}
            <span className="text-xs font-medium">{item.label}</span>
          </NavLink>
        ))}
      </div>
    </nav>
  )
}
