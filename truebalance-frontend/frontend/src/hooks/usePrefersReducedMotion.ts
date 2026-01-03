import { useEffect, useState } from 'react';

/**
 * usePrefersReducedMotion Hook
 *
 * Detects if user prefers reduced motion (accessibility preference).
 * Updates dynamically if user changes their system preference.
 *
 * Returns true if user prefers reduced motion, false otherwise.
 *
 * @example
 * ```tsx
 * const prefersReducedMotion = usePrefersReducedMotion();
 *
 * const duration = prefersReducedMotion ? 0.01 : 0.4;
 * ```
 */
export function usePrefersReducedMotion(): boolean {
  const [prefersReducedMotion, setPrefersReducedMotion] = useState(() => {
    return window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  });

  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');

    const handleChange = (event: MediaQueryListEvent) => {
      setPrefersReducedMotion(event.matches);
    };

    // Modern browsers
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', handleChange);
      return () => mediaQuery.removeEventListener('change', handleChange);
    }
    // Legacy browsers
    else {
      mediaQuery.addListener(handleChange);
      return () => mediaQuery.removeListener(handleChange);
    }
  }, []);

  return prefersReducedMotion;
}
