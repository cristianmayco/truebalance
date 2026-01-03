import { motion, HTMLMotionProps } from 'framer-motion';
import { ReactNode } from 'react';

interface PageTransitionProps extends Omit<HTMLMotionProps<'div'>, 'children'> {
  children: ReactNode;
  className?: string;
}

/**
 * PageTransition Component
 *
 * Wrapper component that adds smooth fade-in animation to pages.
 * Respects user's reduced motion preferences.
 *
 * @example
 * ```tsx
 * <PageTransition>
 *   <div>Your page content</div>
 * </PageTransition>
 * ```
 */
export function PageTransition({ children, className = '', ...props }: PageTransitionProps) {
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  const variants = {
    initial: {
      opacity: prefersReducedMotion ? 1 : 0,
      y: prefersReducedMotion ? 0 : 20,
    },
    animate: {
      opacity: 1,
      y: 0,
      transition: {
        duration: prefersReducedMotion ? 0.01 : 0.4,
        ease: 'easeOut' as const,
      },
    },
    exit: {
      opacity: prefersReducedMotion ? 1 : 0,
      y: prefersReducedMotion ? 0 : -20,
      transition: {
        duration: prefersReducedMotion ? 0.01 : 0.3,
        ease: 'easeIn' as const,
      },
    },
  };

  return (
    <motion.div
      initial="initial"
      animate="animate"
      exit="exit"
      variants={variants}
      className={className}
      {...props}
    >
      {children}
    </motion.div>
  );
}
