import { useDemo } from '@/contexts/DemoContext';
import { Button } from '../ui/Button';
import { InfoIcon } from '@/lib/icons';

/**
 * DemoModeToggle Component
 *
 * Toggle button to enable/disable demo mode.
 * Can be placed in settings, footer, or anywhere appropriate.
 *
 * @example
 * ```tsx
 * import { DemoModeToggle } from '@/components/demo/DemoModeToggle';
 *
 * function SettingsPage() {
 *   return (
 *     <div>
 *       <h2>Configurações</h2>
 *       <DemoModeToggle />
 *     </div>
 *   );
 * }
 * ```
 */
export function DemoModeToggle() {
  const { isDemoMode, toggleDemoMode } = useDemo();

  return (
    <div className="bg-violet-50 dark:bg-violet-900/20 rounded-xl p-6 border border-violet-200 dark:border-violet-800">
      <div className="flex items-start gap-4">
        <div className="p-3 bg-violet-100 dark:bg-violet-900/50 rounded-lg">
          <InfoIcon className="w-6 h-6 text-violet-600 dark:text-violet-400" />
        </div>

        <div className="flex-1">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2">
            Modo de Demonstração
          </h3>

          <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
            {isDemoMode ? (
              <>
                Você está no modo demo com dados fictícios. Perfeito para testar o sistema sem
                afetar seus dados reais.
              </>
            ) : (
              <>
                Ative o modo demo para explorar o sistema com dados de exemplo. Nenhuma informação
                será salva e você pode sair a qualquer momento.
              </>
            )}
          </p>

          <div className="flex items-center gap-3">
            <Button
              variant={isDemoMode ? 'danger' : 'primary'}
              size="md"
              onClick={toggleDemoMode}
            >
              {isDemoMode ? 'Desativar Modo Demo' : 'Ativar Modo Demo'}
            </Button>

            {isDemoMode && (
              <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-violet-100 dark:bg-violet-900/50 text-violet-800 dark:text-violet-200">
                Ativo
              </span>
            )}
          </div>

          {!isDemoMode && (
            <p className="text-xs text-gray-500 dark:text-gray-500 mt-3">
              <strong>Nota:</strong> A página será recarregada ao ativar o modo demo.
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
