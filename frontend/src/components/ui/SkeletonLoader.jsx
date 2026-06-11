const SkeletonLoader = ({ className = '', lines = 1 }) => {
  if (lines > 1) {
    return (
      <div className="space-y-3">
        {Array.from({ length: lines }).map((_, i) => (
          <div
            key={i}
            className={`shimmer rounded-lg bg-white/5 ${i === lines - 1 ? 'w-3/4' : 'w-full'} h-4 ${className}`}
          />
        ))}
      </div>
    );
  }

  return (
    <div className={`shimmer rounded-lg bg-white/5 ${className}`} />
  );
};

export const CardSkeleton = () => (
  <div className="glass-card p-6 space-y-4">
    <div className="flex items-center gap-3">
      <SkeletonLoader className="w-10 h-10 rounded-xl" />
      <div className="flex-1 space-y-2">
        <SkeletonLoader className="w-1/3 h-4" />
        <SkeletonLoader className="w-1/2 h-3" />
      </div>
    </div>
    <SkeletonLoader lines={3} />
    <div className="flex gap-2">
      <SkeletonLoader className="w-20 h-8 rounded-lg" />
      <SkeletonLoader className="w-20 h-8 rounded-lg" />
    </div>
  </div>
);

export const TableSkeleton = ({ rows = 5, cols = 4 }) => (
  <div className="space-y-2">
    {Array.from({ length: rows }).map((_, r) => (
      <div key={r} className="flex gap-4 p-4 glass-card">
        {Array.from({ length: cols }).map((_, c) => (
          <SkeletonLoader key={c} className="flex-1 h-4" />
        ))}
      </div>
    ))}
  </div>
);

export const StatSkeleton = () => (
  <div className="glass-card p-6 space-y-3">
    <SkeletonLoader className="w-8 h-8 rounded-lg" />
    <SkeletonLoader className="w-1/2 h-8" />
    <SkeletonLoader className="w-2/3 h-4" />
  </div>
);

export default SkeletonLoader;
