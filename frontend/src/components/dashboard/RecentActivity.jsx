import { motion } from 'framer-motion';
import { Upload, BarChart2, FileText, Clock } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const activityIcons = {
  UPLOAD: { icon: Upload, color: 'text-blue-400', bg: 'bg-blue-500/20' },
  ANALYSIS: { icon: BarChart2, color: 'text-violet-400', bg: 'bg-violet-500/20' },
  IMPROVE: { icon: FileText, color: 'text-green-400', bg: 'bg-green-500/20' },
};

const formatRelativeTime = (timestamp) => {
  if (!timestamp) return '';
  const now = new Date();
  const date = new Date(timestamp);
  const diff = now - date;
  const mins = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (mins < 1) return 'Just now';
  if (mins < 60) return `${mins}m ago`;
  if (hours < 24) return `${hours}h ago`;
  return `${days}d ago`;
};

const RecentActivity = ({ activities = [] }) => {
  const navigate = useNavigate();

  if (activities.length === 0) {
    return (
      <div className="glass-card p-8 text-center">
        <Clock size={32} className="text-slate-600 mx-auto mb-3" />
        <p className="text-slate-400 font-medium">No recent activity</p>
        <p className="text-slate-600 text-sm mt-1">Upload your first resume to get started</p>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      {activities.map((activity, i) => {
        const config = activityIcons[activity.type] || activityIcons.ANALYSIS;
        const { icon: Icon, color, bg } = config;

        return (
          <motion.div
            key={activity.id || i}
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: i * 0.05 }}
            onClick={() => activity.link && navigate(activity.link)}
            className={`flex items-center gap-4 p-4 glass-card hover:bg-white/8 transition-all duration-200
                        ${activity.link ? 'cursor-pointer' : ''}`}
          >
            {/* Timeline dot */}
            <div className="relative flex-shrink-0">
              <div className={`w-10 h-10 rounded-xl ${bg} flex items-center justify-center`}>
                <Icon size={16} className={color} />
              </div>
              {i < activities.length - 1 && (
                <div className="absolute top-10 left-1/2 -translate-x-1/2 w-px h-2 bg-white/10" />
              )}
            </div>

            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-white truncate">{activity.title}</p>
              {activity.subtitle && (
                <p className="text-xs text-slate-500 truncate mt-0.5">{activity.subtitle}</p>
              )}
            </div>

            <div className="flex-shrink-0 text-right">
              <span className="text-xs text-slate-500">{formatRelativeTime(activity.timestamp)}</span>
              {activity.badge && (
                <div className="mt-1">
                  <span className={`text-xs font-bold px-2 py-0.5 rounded-full
                    ${activity.badge >= 70 ? 'text-green-400 bg-green-500/20'
                      : activity.badge >= 40 ? 'text-amber-400 bg-amber-500/20'
                      : 'text-red-400 bg-red-500/20'}`}
                  >
                    {activity.badge}
                  </span>
                </div>
              )}
            </div>
          </motion.div>
        );
      })}
    </div>
  );
};

export default RecentActivity;
