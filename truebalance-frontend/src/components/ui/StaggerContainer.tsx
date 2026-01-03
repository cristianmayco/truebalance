import { ReactNode } from 'react';
import { motion, HTMLMotionProps } from 'framer-motion';

interface StaggerContainerProps extends Omit<HTMLMotionProps<'div'>, 'children'> {
  children: ReactNode;
  staggerDelay?: number;
  className?: string;
}

/**
 * StaggerContainer Component
 *
 * Container for creating staggered animations in lists.
 * Children will animate in sequence with a delay between each.
 * Respects user's reduced motion preferences.
 *
 * @example
 * ```tsx
 * <StaggerContainer staggerDelay={0.1}>
 *   <Card>Item 1</Card>
 *   <Card>Item 2</Card>
 *   <Card>Item 3</Card>
 * </StaggerContainer>
 * ```
 */
export function StaggerContainer({
  children,
  staggerDelay = 0.1,
  className = '',
  ...props
}: StaggerContainerProps) {
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  const containerVariants = {
    hidden: { opacity: prefersReducedMotion ? 1 : 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: prefersReducedMotion ? 0 : staggerDelay,
      },
    },
  };

  return (
    <motion.div
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      className={className}
      {...props}
    >
      {children}
    </motion.div>
  );
}

interface StaggerItemProps extends Omit<HTMLMotionProps<'div'>, 'children'> {
  children: ReactNode;
  className?: string;
}

/**
 * StaggerItem Component
 *
 * Individual item within a StaggerContainer.
 * Should be used as direct children of StaggerContainer for proper stagger effect.
 *
 * @example
 * ```tsx
 * <StaggerContainer>
 *   <StaggerItem>
 *     <Card>Item 1</Card>
 *   </StaggerItem>
 *   <StaggerItem>
 *     <Card>Item 2</Card>
 *   </StaggerItem>
 * </StaggerContainer>
 * ```
 */
export function StaggerItem({ children, className = '', ...props }: StaggerItemProps) {
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  const itemVariants = {
    hidden: {
      opacity: prefersReducedMotion ? 1 : 0,
      y: prefersReducedMotion ? 0 : 20,
    },
    visible: {
      opacity: 1,
      y: 0,
      transition: {
        duration: prefersReducedMotion ? 0.01 : 0.4,
        ease: [0.25, 0.1, 0.25, 1],
      },
    },
  };

  return (
    <motion.div variants={itemVariants} className={className} {...props}>
      {children}
    </motion.div>
  );
}
