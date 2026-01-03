import { RefreshCw, X } from 'lucide-react';
import { usePWA } from '@/hooks/usePWA';

export function PWAUpdatePrompt() {
  const { showReloadPrompt, reloadPage, dismissPrompt } = usePWA();

  if (!showReloadPrompt) {
    return null;
  }

  return (
    <div className="fixed top-4 left-4 right-4 lg:left-auto lg:right-4 lg:max-w-md z-50 animate-slide-up">
      <div className="bg-primary-600 dark:bg-primary-700 rounded-lg shadow-2xl p-4">
        <div className="flex items-start gap-3">
          <div className="flex-shrink-0 w-10 h-10 bg-white/20 rounded-lg flex items-center justify-center">
            <RefreshCw className="w-5 h-5 text-white" />
          </div>

          <div className="flex-1 min-w-0">
            <h3 className="text-sm font-semibold text-white mb-1">
              Nova versão disponível
            </h3>
            <p className="text-xs text-white/90 mb-3">
              Uma atualização está pronta. Recarregue para usar a versão mais recente.
            </p>

            <div className="flex gap-2">
              <button
                onClick={reloadPage}
                className="flex-1 px-4 py-2 bg-white hover:bg-gray-100 text-primary-600 text-sm font-medium rounded-lg transition-colors"
              >
                Recarregar agora
              </button>
              <button
                onClick={dismissPrompt}
                className="px-4 py-2 bg-white/20 hover:bg-white/30 text-white text-sm font-medium rounded-lg transition-colors"
              >
                Mais tarde
              </button>
            </div>
          </div>

          <button
            onClick={dismissPrompt}
            className="flex-shrink-0 text-white/80 hover:text-white transition-colors"
            aria-label="Fechar"
          >
            <X className="w-5 h-5" />
          </button>
        </div>
      </div>
    </div>
  );
}
