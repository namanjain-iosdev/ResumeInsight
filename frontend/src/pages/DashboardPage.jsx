import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  FileText, BarChart2, MessageSquare, TrendingUp, Upload,
  ArrowRight, Sparkles, CheckCircle, Zap, Activity
} from 'lucide-react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Cell
} from 'recharts';
import useAuthStore from '../store/authStore';
import { resumeService } from '../services/resumeService';
import { analysisService } from '../services/analysisService';
import { adminService } from '../services/adminService';
import StatsCard from '../components/dashboard/StatsCard';
import RecentActivity from '../components/dashboard/RecentActivity';
import { StatSkeleton, CardSkeleton } from '../components/ui/SkeletonLoader';
import AppLayout from '../components/layout/AppLayout';

const CustomTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    return (
      <div className="glass-card p-3 border border-white/20 shadow-xl">
        <p className="text-slate-400 text-xs">{label}</p>
        <p className="text-white font-bold text-sm">{payload[0].value} / 100</p>
      </div>
    );
  }
  return null;
};

const getBarColor = (score) => {
  if (score >= 70) return '#22c55e';
  if (score >= 40) return '#f59e0b';
  return '#ef4444';
};

const DashboardPage = () => {
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const [stats, setStats] = useState(null);
  const [analyses, setAnalyses] = useState([]);
  const [resumes, setResumes] = useState([]);
  const [aiStatus, setAiStatus] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const [resumesData, analysesData] = await Promise.allSettled([
          resumeService.getAll(0, 5),
          analysisService.getHistory(0, 10),
        ]);

        const unwrap = (v) => (v && typeof v === 'object' && 'data' in v ? v.data : v);
        const toArray = (v) => {
          const u = unwrap(v);
          if (Array.isArray(u)) return u;
          if (Array.isArray(u?.content)) return u.content;
          return [];
        };

        const resumesArr = resumesData.status === 'fulfilled' ? toArray(resumesData.value) : [];
        setResumes(resumesArr);

        if (analysesData.status === 'fulfilled') {
          const analysisArr = toArray(analysesData.value);
          setAnalyses(analysisArr);

          const total = analysisArr.length;
          const avgScore = total > 0
            ? Math.round(analysisArr.reduce((sum, a) => sum + (a.atsScore || 0), 0) / total)
            : 0;
          const unwrappedAnalyses = unwrap(analysesData.value);
          const unwrappedResumes = unwrap(resumesData.value);
          setStats({
            totalResumes: unwrappedResumes?.totalElements ?? resumesArr.length,
            totalAnalyses: unwrappedAnalyses?.totalElements ?? total,
            avgAtsScore: avgScore,
            chatCount: 0,
          });
        }

        try {
          const aiData = await adminService.getAIStatus();
          setAiStatus(unwrap(aiData));
        } catch {
          setAiStatus(null);
        }
      } catch (err) {
        console.error('Dashboard data fetch error:', err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  const firstName = user?.fullName?.split(' ')[0] || user?.name?.split(' ')[0] || 'there';

  // Build chart data from analyses
  const chartData = analyses.slice(0, 8).reverse().map((a, i) => ({
    name: `#${i + 1}`,
    score: a.atsScore || 0,
    label: a.resumeFileName || `Analysis ${i + 1}`,
  }));

  // Build activity feed
  const activities = analyses.slice(0, 5).map(a => ({
    id: a.id,
    type: 'ANALYSIS',
    title: `Analysis: ${a.resumeFileName || 'Resume'}`,
    subtitle: `ATS Score: ${a.atsScore || 0}`,
    timestamp: a.createdAt || a.analyzedAt,
    badge: a.atsScore,
    link: `/analyses/${a.id}`,
  }));

  return (
    <AppLayout>
      <div className="page-container py-8 space-y-8">

        {/* Welcome Banner */}
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          className="glass-card p-6 border border-violet-500/20 relative overflow-hidden"
        >
          <div className="absolute inset-0 bg-gradient-to-r from-violet-600/10 to-transparent pointer-events-none" />
          <div className="relative flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold text-white">
                Good morning, <span className="gradient-text">{firstName}</span>! 👋
              </h1>
              <p className="text-slate-400 mt-1">
                Here's an overview of your resume analysis activity.
              </p>
            </div>
            <div className="flex gap-3">
              <Link to="/upload" className="btn-primary">
                <Upload size={16} />
                Upload Resume
              </Link>
              <Link to="/chat" className="btn-secondary">
                <MessageSquare size={16} />
                Chat
              </Link>
            </div>
          </div>
        </motion.div>

        {/* AI Status */}
        {aiStatus && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.1 }}
            className="flex items-center gap-3 glass-card px-4 py-3 w-fit"
          >
            <div className={`w-2 h-2 rounded-full ${aiStatus.available ? 'bg-green-400 animate-pulse' : 'bg-red-400'}`} />
            <span className="text-sm text-slate-300">
              AI Provider: <span className="text-white font-medium">{aiStatus.provider || 'Connected'}</span>
            </span>
            <Activity size={14} className={aiStatus.available ? 'text-green-400' : 'text-red-400'} />
          </motion.div>
        )}

        {/* Stats Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {isLoading ? (
            Array.from({ length: 4 }).map((_, i) => <StatSkeleton key={i} />)
          ) : (
            <>
              <StatsCard
                title="Total Resumes"
                value={stats?.totalResumes || 0}
                icon={FileText}
                iconBg="bg-blue-500/20"
                iconColor="text-blue-400"
                delay={0}
              />
              <StatsCard
                title="Total Analyses"
                value={stats?.totalAnalyses || 0}
                icon={BarChart2}
                iconBg="bg-violet-500/20"
                iconColor="text-violet-400"
                delay={0.05}
              />
              <StatsCard
                title="Avg. ATS Score"
                value={stats?.avgAtsScore || 0}
                icon={TrendingUp}
                suffix="/100"
                iconBg="bg-amber-500/20"
                iconColor="text-amber-400"
                delay={0.1}
              />
              <StatsCard
                title="Chat Sessions"
                value={stats?.chatCount || 0}
                icon={MessageSquare}
                iconBg="bg-emerald-500/20"
                iconColor="text-emerald-400"
                delay={0.15}
              />
            </>
          )}
        </div>

        {/* Chart + Activity */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* ATS Score Chart */}
          <div className="lg:col-span-2 glass-card p-6">
            <div className="flex items-center justify-between mb-6">
              <div>
                <h2 className="section-title text-lg">ATS Score History</h2>
                <p className="text-sm text-slate-500 mt-1">Your recent analysis scores</p>
              </div>
              <Link to="/analyses" className="btn-ghost text-sm">
                View all <ArrowRight size={14} />
              </Link>
            </div>

            {isLoading ? (
              <div className="h-48 shimmer rounded-xl" />
            ) : chartData.length > 0 ? (
              <ResponsiveContainer width="100%" height={200}>
                <BarChart data={chartData} barSize={28}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
                  <XAxis dataKey="name" axisLine={false} tickLine={false}
                    tick={{ fill: '#64748b', fontSize: 12 }} />
                  <YAxis domain={[0, 100]} axisLine={false} tickLine={false}
                    tick={{ fill: '#64748b', fontSize: 12 }} />
                  <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(255,255,255,0.03)' }} />
                  <Bar dataKey="score" radius={[6, 6, 0, 0]}>
                    {chartData.map((entry, index) => (
                      <Cell key={index} fill={getBarColor(entry.score)} fillOpacity={0.85} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-48 flex flex-col items-center justify-center text-center">
                <BarChart2 size={32} className="text-slate-700 mb-3" />
                <p className="text-slate-500 text-sm">No analyses yet</p>
                <Link to="/upload" className="btn-primary mt-4 text-sm">Upload Your First Resume</Link>
              </div>
            )}
          </div>

          {/* Recent Activity */}
          <div className="glass-card p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="section-title text-lg">Recent Activity</h2>
            </div>
            {isLoading ? (
              <div className="space-y-3">
                {Array.from({ length: 3 }).map((_, i) => (
                  <div key={i} className="h-14 shimmer rounded-xl" />
                ))}
              </div>
            ) : (
              <RecentActivity activities={activities} />
            )}
          </div>
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          {[
            {
              icon: Upload,
              title: 'Upload Resume',
              desc: 'Add a new resume for analysis',
              to: '/upload',
              color: 'from-violet-600/20 to-violet-600/5',
              border: 'border-violet-500/20',
              iconBg: 'bg-violet-600',
            },
            {
              icon: Sparkles,
              title: 'Analyze Resume',
              desc: 'Get AI-powered insights and score',
              to: '/resumes',
              color: 'from-blue-600/20 to-blue-600/5',
              border: 'border-blue-500/20',
              iconBg: 'bg-blue-600',
            },
            {
              icon: MessageSquare,
              title: 'Chat Assistant',
              desc: 'Get career advice and interview prep',
              to: '/chat',
              color: 'from-emerald-600/20 to-emerald-600/5',
              border: 'border-emerald-500/20',
              iconBg: 'bg-emerald-600',
            },
          ].map((action, i) => (
            <motion.div
              key={action.to}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3 + i * 0.1 }}
            >
              <Link
                to={action.to}
                className={`block glass-card p-6 border ${action.border} hover:scale-105 transition-all duration-300
                           bg-gradient-to-br ${action.color} hover:shadow-lg group`}
              >
                <div className={`w-10 h-10 ${action.iconBg} rounded-xl flex items-center justify-center mb-4`}>
                  <action.icon size={20} className="text-white" />
                </div>
                <h3 className="font-semibold text-white mb-1">{action.title}</h3>
                <p className="text-slate-500 text-sm">{action.desc}</p>
                <div className="flex items-center gap-1 text-violet-400 text-sm mt-3 opacity-0 group-hover:opacity-100 transition-opacity">
                  <span>Go</span> <ArrowRight size={12} />
                </div>
              </Link>
            </motion.div>
          ))}
        </div>
      </div>
    </AppLayout>
  );
};

export default DashboardPage;
