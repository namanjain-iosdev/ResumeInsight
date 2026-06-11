import { useEffect, useRef, useState } from 'react';
import { motion } from 'framer-motion';
import { TrendingUp, TrendingDown, Minus } from 'lucide-react';

const StatsCard = ({ title, value, icon: Icon, iconBg = 'bg-violet-600/20', iconColor = 'text-violet-400', 
                     suffix = '', trend, trendLabel, delay = 0 }) => {
  const [displayValue, setDisplayValue] = useState(0);
  const isNumber = typeof value === 'number';

  useEffect(() => {
    if (!isNumber) return;
    
    const duration = 1000;
    const startTime = Date.now();
    const timer = setInterval(() => {
      const elapsed = Date.now() - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 2);
      setDisplayValue(Math.round(eased * value));
      if (progress >= 1) clearInterval(timer);
    }, 16);

    return () => clearInterval(timer);
  }, [value, isNumber]);

  const TrendIcon = trend > 0 ? TrendingUp : trend < 0 ? TrendingDown : Minus;
  const trendColor = trend > 0 ? 'text-green-400' : trend < 0 ? 'text-red-400' : 'text-slate-400';

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay }}
      className="glass-card p-6 hover:bg-white/8 transition-all duration-300 hover:shadow-lg hover:shadow-violet-500/5 cursor-default"
    >
      <div className="flex items-start justify-between">
        <div className="flex flex-col gap-3">
          <p className="text-sm font-medium text-slate-400">{title}</p>
          <div className="flex items-baseline gap-1">
            <span className="text-3xl font-bold text-white">
              {isNumber ? displayValue.toLocaleString() : value}
            </span>
            {suffix && <span className="text-lg text-slate-400 font-medium">{suffix}</span>}
          </div>
          {trendLabel && (
            <div className={`flex items-center gap-1 text-xs ${trendColor}`}>
              <TrendIcon size={12} />
              <span>{trendLabel}</span>
            </div>
          )}
        </div>

        <div className={`w-12 h-12 rounded-xl ${iconBg} flex items-center justify-center flex-shrink-0`}>
          <Icon size={22} className={iconColor} />
        </div>
      </div>
    </motion.div>
  );
};

export default StatsCard;
