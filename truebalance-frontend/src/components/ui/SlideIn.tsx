import { motion, HTMLMotionProps } from 'framer-motion';
import { ReactNode } from 'react';

type Direction = 'top' | 'bottom' | 'left' | 'right';

interface SlideInProps extends Omit<HTMLMotionProps<'div'>, 'children'> {
  children: ReactNode;
  direction?: Direction;
  className?: string;
  delay?: number;
}

/**
 * SlideIn Component
 *
 * Wrapper component that adds slide-in animation from specified direction.
 * Commonly used for modals, sidebars, and notifications.
 * Respects user's reduced motion preferences.
 *
 * @example
 * ```tsx
 * <SlideIn direction="bottom">
 *   <div>Modal content</div>
 * </SlideIn>
 * ```
 */
export function SlideIn({
  children,
  direction = 'bottom',
  className = '',
  delay = 0,
  ...props
}: SlideInProps) {
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  const getInitialPosition = () => {
    if (prefersReducedMotion) return { x: 0, y: 0 };

    switch (direction) {
      case 'top':
        return { x: 0, y: -100 };
      case 'bottom':
        return { x: 0, y: 100 };
      case 'left':
        return { x: -100, y: 0 };
      case 'right':
        return { x: 100, y: 0 };
      default:
        return { x: 0, y: 0 };
    }
  };

  const variants = {
    initial: {
      ...getInitialPosition(),
      opacity: prefersReducedMotion ? 1 : 0,
    },
    animate: {
      x: 0,
      y: 0,
      opacity: 1,
      transition: {
        duration: prefersReducedMotion ? 0.01 : 0.4,
        delay: prefersReducedMotion ? 0 : delay,
        ease: [0.25, 0.1, 0.25, 1], // Custom easing for smooth feel
      },
    },
    exit: {
      ...getInitialPosition(),
      opacity: prefersReducedMotion ? 1 : 0,
      transition: {
        duration: prefersReducedMotion ? 0.01 : 0.3,
        ease: 'easeIn',
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
