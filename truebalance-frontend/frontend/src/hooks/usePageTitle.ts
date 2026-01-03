import { useEffect } from 'react';

/**
 * usePageTitle Hook
 *
 * Dynamically sets the page title and updates it when component mounts/unmounts.
 * Useful for SEO and browser tab labels.
 *
 * @param title - The page title (will be appended with " | TrueBalance")
 * @param options - Optional configuration
 * @param options.restoreOnUnmount - Whether to restore previous title on unmount (default: true)
 * @param options.suffix - Custom suffix instead of "TrueBalance" (default: "TrueBalance")
 *
 * @example
 * ```tsx
 * function BillsPage() {
 *   usePageTitle('Minhas Contas');
 *   // Document title will be: "Minhas Contas | TrueBalance"
 *
 *   return <div>...</div>;
 * }
 * ```
 *
 * @example
 * ```tsx
 * function ReportsPage() {
 *   usePageTitle('Relatórios Financeiros', {
 *     suffix: 'TrueBalance - Finanças Pessoais'
 *   });
 *   // Document title will be: "Relatórios Financeiros | TrueBalance - Finanças Pessoais"
 *
 *   return <div>...</div>;
 * }
 * ```
 */
export function usePageTitle(
  title: string,
  options: {
    restoreOnUnmount?: boolean;
    suffix?: string;
  } = {}
) {
  const { restoreOnUnmount = true, suffix = 'TrueBalance' } = options;

  useEffect(() => {
    // Store previous title
    const previousTitle = document.title;

    // Set new title
    document.title = title ? `${title} | ${suffix}` : suffix;

    // Restore previous title on unmount if option is enabled
    return () => {
      if (restoreOnUnmount) {
        document.title = previousTitle;
      }
    };
  }, [title, suffix, restoreOnUnmount]);
}
