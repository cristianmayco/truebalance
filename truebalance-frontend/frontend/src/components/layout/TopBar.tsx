import { ThemeToggle } from './ThemeToggle'

interface TopBarProps {
  title?: string
}

export function TopBar({ title = 'Dashboard' }: TopBarProps) {
  return (
    <header className="h-16 bg-white dark:bg-slate-800 border-b border-gray-200 dark:border-slate-700 flex items-center justify-between px-4 lg:px-6">
      {/* Page Title */}
      <div>
        <h2 className="text-xl font-semibold text-gray-900 dark:text-white">
          {title}
        </h2>
      </div>

      {/* Actions */}
      <div className="flex items-center gap-4">
        <ThemeToggle />
      </div>
    </header>
  )
}
