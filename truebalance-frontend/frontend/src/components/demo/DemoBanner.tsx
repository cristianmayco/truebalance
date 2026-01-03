import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { InfoIcon, CloseIcon } from '@/lib/icons';
import { useDemo } from '@/contexts/DemoContext';
import { Button } from '../ui/Button';

/**
 * DemoBanner Component
 *
 * Displays a banner at the top of the page when demo mode is active.
 * Users can dismiss it temporarily or exit demo mode entirely.
 *
 * Features:
 * - Informative message about demo mode
 * - Exit demo mode button
 * - Dismiss banner button
 * - Smooth slide-down animation
 * - Respects reduced motion preferences
 *
 * @example
 * ```tsx
 * import { DemoBanner } from '@/components/demo/DemoBanner';
 *
 * function App() {
 *   return (
 *     <>
 *       <DemoBanner />
 *       <MainContent />
 *     </>
 *   );
 * }
 * ```
 */
export function DemoBanner() {
  const { isDemoMode, disableDemoMode } = useDemo();
  const [isDismissed, setIsDismissed] = useState(false);

  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  if (!isDemoMode || isDismissed) {
    return null;
  }

  const bannerVariants = {
    hidden: {
      y: prefersReducedMotion ? 0 : -100,
      opacity: prefersReducedMotion ? 1 : 0,
    },
    visible: {
      y: 0,
      opacity: 1,
      transition: {
        duration: prefersReducedMotion ? 0.01 : 0.4,
        ease: [0.25, 0.1, 0.25, 1] as [number, number, number, number],
      },
    },
    exit: {
      y: prefersReducedMotion ? 0 : -100,
      opacity: prefersReducedMotion ? 1 : 0,
      transition: {
        duration: prefersReducedMotion ? 0.01 : 0.3,
        ease: 'easeIn' as const,
      },
    },
  };

  return (
    <AnimatePresence>
      <motion.div
        variants={bannerVariants}
        initial="hidden"
        animate="visible"
        exit="exit"
        className="bg-violet-600 dark:bg-violet-700 text-white shadow-lg relative z-50"
        role="alert"
        aria-live="polite"
      >
        <div className="container mx-auto px-4 py-3 sm:px-6 lg:px-8">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
            {/* Left side: Info icon + message */}
            <div className="flex items-start sm:items-center gap-3 flex-1">
              <InfoIcon
                className="w-5 h-5 flex-shrink-0 mt-0.5 sm:mt-0"
                aria-hidden="true"
              />
              <div className="flex-1">
                <p className="text-sm sm:text-base font-medium">
                  Modo de Demonstração Ativo
                </p>
                <p className="text-xs sm:text-sm opacity-90 mt-0.5">
                  Você está visualizando dados fictícios. Nenhuma informação será salva.
                </p>
              </div>
            </div>

            {/* Right side: Action buttons */}
            <div className="flex items-center gap-2 w-full sm:w-auto">
              <Button
                variant="ghost"
                size="sm"
                onClick={disableDemoMode}
                className="
                  text-white hover:bg-white/20
                  border border-white/30 hover:border-white/50
                  flex-1 sm:flex-initial
                "
              >
                Sair do Demo
              </Button>

              <button
                onClick={() => setIsDismissed(true)}
                className="
                  p-2 rounded-lg
                  hover:bg-white/20
                  transition-colors
                  focus:outline-none focus:ring-2 focus:ring-white/50
                "
                aria-label="Dispensar banner"
              >
                <CloseIcon className="w-5 h-5" aria-hidden="true" />
              </button>
            </div>
          </div>
        </div>
      </motion.div>
    </AnimatePresence>
  );
}
