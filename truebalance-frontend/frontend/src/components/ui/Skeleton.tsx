import { cn } from '@/lib/utils';

interface SkeletonProps {
  className?: string;
  variant?: 'text' | 'circular' | 'rectangular';
  width?: string | number;
  height?: string | number;
  animation?: 'pulse' | 'wave' | 'none';
}

export function Skeleton({
  className,
  variant = 'rectangular',
  width,
  height,
  animation = 'pulse',
}: SkeletonProps) {
  const baseClasses = 'bg-gray-200 dark:bg-gray-700';

  const variantClasses = {
    text: 'rounded',
    circular: 'rounded-full',
    rectangular: 'rounded-lg',
  };

  const animationClasses = {
    pulse: 'animate-pulse',
    wave: 'animate-shimmer bg-gradient-to-r from-gray-200 via-gray-300 to-gray-200 dark:from-gray-700 dark:via-gray-600 dark:to-gray-700 bg-[length:200%_100%]',
    none: '',
  };

  const style = {
    width: width || '100%',
    height: height || (variant === 'text' ? '1em' : variant === 'circular' ? '40px' : '100%'),
  };

  return (
    <div
      className={cn(
        baseClasses,
        variantClasses[variant],
        animationClasses[animation],
        className
      )}
      style={style}
      aria-live="polite"
      aria-busy="true"
    />
  );
}

// Skeleton presets for common use cases

export function SkeletonCard() {
  return (
    <div className="p-6 bg-white dark:bg-slate-800 rounded-lg border border-gray-200 dark:border-slate-700">
      <div className="space-y-4">
        <Skeleton variant="rectangular" height={20} width="60%" />
        <Skeleton variant="text" />
        <Skeleton variant="text" />
        <Skeleton variant="text" width="80%" />
        <div className="flex gap-2 mt-4">
          <Skeleton variant="rectangular" width={80} height={32} />
          <Skeleton variant="rectangular" width={80} height={32} />
        </div>
      </div>
    </div>
  );
}

export function SkeletonTable({ rows = 5 }: { rows?: number }) {
  return (
    <div className="space-y-3">
      {/* Header */}
      <div className="flex gap-4 p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
        <Skeleton width="30%" height={20} />
        <Skeleton width="20%" height={20} />
        <Skeleton width="25%" height={20} />
        <Skeleton width="15%" height={20} />
        <Skeleton width="10%" height={20} />
      </div>

      {/* Rows */}
      {Array.from({ length: rows }).map((_, index) => (
        <div key={index} className="flex gap-4 p-4 border-b border-gray-200 dark:border-gray-700">
          <Skeleton width="30%" height={16} />
          <Skeleton width="20%" height={16} />
          <Skeleton width="25%" height={16} />
          <Skeleton width="15%" height={16} />
          <Skeleton width="10%" height={16} />
        </div>
      ))}
    </div>
  );
}

export function SkeletonForm() {
  return (
    <div className="space-y-6">
      {/* Form fields */}
      {Array.from({ length: 4 }).map((_, index) => (
        <div key={index} className="space-y-2">
          <Skeleton width="30%" height={16} /> {/* Label */}
          <Skeleton height={40} /> {/* Input */}
        </div>
      ))}

      {/* Buttons */}
      <div className="flex gap-3 mt-6">
        <Skeleton width={100} height={40} />
        <Skeleton width={100} height={40} />
      </div>
    </div>
  );
}

export function SkeletonMetricsCards() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      {Array.from({ length: 4 }).map((_, index) => (
        <div
          key={index}
          className="p-6 bg-white dark:bg-slate-800 rounded-lg border border-gray-200 dark:border-slate-700"
        >
          <div className="flex items-center justify-between mb-4">
            <Skeleton variant="circular" width={48} height={48} />
          </div>
          <Skeleton width="70%" height={14} className="mb-2" />
          <Skeleton width="50%" height={24} className="mb-2" />
          <Skeleton width="80%" height={12} />
        </div>
      ))}
    </div>
  );
}

export function SkeletonList({ items = 5 }: { items?: number }) {
  return (
    <div className="space-y-3">
      {Array.from({ length: items }).map((_, index) => (
        <div
          key={index}
          className="flex items-center gap-4 p-4 bg-white dark:bg-slate-800 rounded-lg border border-gray-200 dark:border-slate-700"
        >
          <Skeleton variant="circular" width={40} height={40} />
          <div className="flex-1 space-y-2">
            <Skeleton width="40%" height={16} />
            <Skeleton width="60%" height={14} />
          </div>
          <Skeleton width={80} height={32} />
        </div>
      ))}
    </div>
  );
}
