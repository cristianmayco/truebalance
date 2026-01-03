import { ReactNode, forwardRef } from 'react';
import { motion, HTMLMotionProps } from 'framer-motion';

type CardVariant = 'default' | 'gradient' | 'outlined';

interface CardProps extends Omit<HTMLMotionProps<'div'>, 'children'> {
  children: ReactNode;
  variant?: CardVariant;
  hover?: boolean;
  className?: string;
}

/**
 * Card Component
 *
 * Flexible card component with smooth hover animations.
 * Respects user's reduced motion preferences.
 *
 * Variants:
 * - default: Standard card with background and shadow
 * - gradient: Card with subtle gradient background
 * - outlined: Card with border and no background
 *
 * @example
 * ```tsx
 * <Card variant="default" hover>
 *   <h2>Card Title</h2>
 *   <p>Card content</p>
 * </Card>
 * ```
 */
const Card = forwardRef<HTMLDivElement, CardProps>(
  ({ children, variant = 'default', hover = true, className = '', ...props }, ref) => {
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    const variantClasses = {
      default: 'bg-white dark:bg-gray-800 shadow-md',
      gradient: 'bg-gradient-to-br from-violet-50 to-purple-50 dark:from-gray-800 dark:to-gray-900 shadow-md',
      outlined: 'bg-transparent border-2 border-gray-200 dark:border-gray-700',
    };

    const hoverVariants = hover && !prefersReducedMotion
      ? {
          rest: {
            scale: 1,
            boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
          },
          hover: {
            scale: 1.02,
            boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
            transition: {
              duration: 0.3,
              ease: [0.25, 0.1, 0.25, 1],
            },
          },
        }
      : undefined;

    return (
      <motion.div
        ref={ref}
        initial="rest"
        whileHover={hover ? 'hover' : undefined}
        variants={hoverVariants}
        className={`
          rounded-xl
          ${variantClasses[variant]}
          ${className}
        `}
        {...props}
      >
        {children}
      </motion.div>
    );
  }
);

Card.displayName = 'Card';

export default Card;
