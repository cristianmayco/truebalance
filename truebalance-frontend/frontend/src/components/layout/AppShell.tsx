import { ReactNode } from 'react'
import { Sidebar } from './Sidebar'
import { TopBar } from './TopBar'
import { BottomNav } from './BottomNav'
import { PWAInstallPrompt } from '../pwa/PWAInstallPrompt'
import { PWAUpdatePrompt } from '../pwa/PWAUpdatePrompt'

interface AppShellProps {
  children: ReactNode
  title?: string
}

export function AppShell({ children, title }: AppShellProps) {
  return (
    <div className="min-h-screen bg-gray-50 dark:bg-slate-900">
      {/* Skip to content - for keyboard navigation */}
      <a href="#main-content" className="skip-to-content">
        Pular para o conteúdo principal
      </a>

      {/* Sidebar - Desktop only */}
      <Sidebar />

      {/* Main Content Area */}
      <div className="lg:ml-64 min-h-screen">
        {/* TopBar */}
        <TopBar title={title} />

        {/* Page Content */}
        <main id="main-content" className="p-4 lg:p-6 pb-20 lg:pb-6">
          {children}
        </main>
      </div>

      {/* Bottom Navigation - Mobile only */}
      <BottomNav />

      {/* PWA Prompts */}
      <PWAUpdatePrompt />
      <PWAInstallPrompt />
    </div>
  )
}
