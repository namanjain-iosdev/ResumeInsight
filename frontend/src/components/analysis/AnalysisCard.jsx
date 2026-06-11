import { motion } from 'framer-motion';
import { ChevronRight } from 'lucide-react';

const AnalysisCard = ({ title, icon: Icon, children, iconColor = 'text-violet-400', delay = 0, action, onAction }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay }}
      className="glass-card p-6 space-y-4"
    >
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          {Icon && (
            <div className={`w-9 h-9 rounded-xl bg-white/5 flex items-center justify-center ${iconColor}`}>
              <Icon size={18} />
            </div>
          )}
          <h3 className="font-semibold text-white text-base">{title}</h3>
        </div>
        {action && (
          <button
            onClick={onAction}
            className="flex items-center gap-1 text-sm text-violet-400 hover:text-violet-300 transition-colors"
          >
            {action} <ChevronRight size={14} />
          </button>
        )}
      </div>

      {/* Content */}
      <div>{children}</div>
    </motion.div>
  );
};

export default AnalysisCard;
