import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import {
  Sparkles, FileText, BarChart2, MessageSquare, Download,
  CheckCircle, ArrowRight, Star, Zap, Shield, Brain
} from 'lucide-react';
import Navbar from '../components/layout/Navbar';

const features = [
  {
    icon: BarChart2,
    title: 'ATS Score Analysis',
    description: 'Get an instant compatibility score showing how well your resume performs against Applicant Tracking Systems.',
    color: 'text-violet-400',
    bg: 'bg-violet-500/10',
    border: 'border-violet-500/20',
  },
  {
    icon: Sparkles,
    title: 'AI Resume Improvement',
    description: 'Let our AI rewrite and enhance your resume with industry-specific language and optimized formatting.',
    color: 'text-blue-400',
    bg: 'bg-blue-500/10',
    border: 'border-blue-500/20',
  },
  {
    icon: Download,
    title: 'PDF Export',
    description: 'Download your AI-improved resume as a clean, professional PDF ready to submit to employers.',
    color: 'text-emerald-400',
    bg: 'bg-emerald-500/10',
    border: 'border-emerald-500/20',
  },
  {
    icon: MessageSquare,
    title: 'Career Chat Assistant',
    description: 'Chat with our AI about interview prep, career guidance, salary negotiation, and more.',
    color: 'text-amber-400',
    bg: 'bg-amber-500/10',
    border: 'border-amber-500/20',
  },
];

const steps = [
  {
    step: '01',
    title: 'Upload Your Resume',
    description: 'Drag and drop your PDF or DOCX resume. Our system processes it instantly.',
    icon: FileText,
  },
  {
    step: '02',
    title: 'Get AI Analysis',
    description: 'Receive a detailed ATS score, skill gaps, keyword analysis, and improvement recommendations.',
    icon: Brain,
  },
  {
    step: '03',
    title: 'Download & Apply',
    description: 'Get your AI-improved resume as a PDF and start landing more interviews.',
    icon: Zap,
  },
];

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: { staggerChildren: 0.1 },
  },
};

const itemVariants = {
  hidden: { opacity: 0, y: 30 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.5 } },
};

const LandingPage = () => {
  return (
    <div className="min-h-screen">
      <Navbar />

      {/* Hero Section */}
      <section className="relative pt-32 pb-20 px-4 overflow-hidden">
        {/* Animated background orbs */}
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          <motion.div
            animate={{ scale: [1, 1.1, 1], opacity: [0.3, 0.5, 0.3] }}
            transition={{ duration: 8, repeat: Infinity }}
            className="absolute -top-40 -left-40 w-96 h-96 bg-violet-600/20 rounded-full blur-3xl"
          />
          <motion.div
            animate={{ scale: [1.1, 1, 1.1], opacity: [0.2, 0.4, 0.2] }}
            transition={{ duration: 10, repeat: Infinity, delay: 2 }}
            className="absolute -bottom-40 -right-40 w-96 h-96 bg-purple-600/20 rounded-full blur-3xl"
          />
          <motion.div
            animate={{ y: [0, -20, 0] }}
            transition={{ duration: 6, repeat: Infinity, ease: 'easeInOut' }}
            className="absolute top-1/3 right-1/4 w-64 h-64 bg-blue-600/10 rounded-full blur-3xl"
          />
        </div>

        <div className="max-w-5xl mx-auto text-center relative">
          {/* Badge */}
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5 }}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-full glass-card border border-violet-500/30 text-sm text-violet-300 mb-8"
          >
            <Sparkles size={14} className="text-violet-400" />
            <span>AI-Powered Resume Analysis</span>
            <span className="w-1.5 h-1.5 rounded-full bg-green-400 animate-pulse" />
          </motion.div>

          {/* Headline */}
          <motion.h1
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.1 }}
            className="text-5xl sm:text-6xl lg:text-7xl font-extrabold text-white leading-tight mb-6"
          >
            Transform Your{' '}
            <span className="gradient-text">Resume</span>
            {' '}with AI
          </motion.h1>

          {/* Subheadline */}
          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            className="text-xl text-slate-400 max-w-2xl mx-auto mb-10 leading-relaxed"
          >
            Optimize your CV for ATS systems, get AI-powered improvements, and land{' '}
            <span className="text-white font-medium">3x more interviews</span> with our smart resume analyzer.
          </motion.p>

          {/* CTA Buttons */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.3 }}
            className="flex flex-col sm:flex-row gap-4 justify-center mb-16"
          >
            <Link to="/register" className="btn-primary text-base px-8 py-4 shadow-2xl shadow-violet-500/30">
              Get Started Free
              <ArrowRight size={18} />
            </Link>
            <a href="#how-it-works" className="btn-secondary text-base px-8 py-4">
              See How It Works
            </a>
          </motion.div>

          {/* Social proof */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.5 }}
            className="flex flex-wrap items-center justify-center gap-6 text-sm text-slate-500"
          >
            <div className="flex items-center gap-2">
              <div className="flex -space-x-2">
                {[1, 2, 3, 4].map(i => (
                  <div key={i} className="w-7 h-7 rounded-full bg-violet-600 border-2 border-navy-900" />
                ))}
              </div>
              <span>10,000+ users</span>
            </div>
            <div className="flex items-center gap-1">
              {[1, 2, 3, 4, 5].map(i => (
                <Star key={i} size={14} className="text-amber-400 fill-amber-400" />
              ))}
              <span className="ml-1">4.9/5 rating</span>
            </div>
            <div className="flex items-center gap-2">
              <CheckCircle size={14} className="text-green-400" />
              <span>No credit card required</span>
            </div>
          </motion.div>
        </div>

        {/* Hero visual / mockup */}
        <motion.div
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.4 }}
          className="max-w-4xl mx-auto mt-16 relative"
        >
          <div className="glass-card p-6 border border-white/20 shadow-2xl shadow-black/50">
            {/* Mock analysis result */}
            <div className="flex flex-col md:flex-row gap-6 items-center">
              {/* Score ring mock */}
              <div className="flex-shrink-0 flex flex-col items-center gap-3">
                <div className="relative w-32 h-32">
                  <svg width="128" height="128" viewBox="0 0 128 128" className="-rotate-90">
                    <circle cx="64" cy="64" r="52" fill="none" stroke="rgba(255,255,255,0.08)" strokeWidth="10" />
                    <circle cx="64" cy="64" r="52" fill="none" stroke="#22c55e"
                      strokeWidth="10" strokeLinecap="round" strokeDasharray="326.7"
                      strokeDashoffset="81.7" style={{ filter: 'drop-shadow(0 0 8px #22c55e66)' }} />
                  </svg>
                  <div className="absolute inset-0 flex flex-col items-center justify-center">
                    <span className="text-3xl font-bold text-green-400">75</span>
                    <span className="text-xs text-slate-500">/100</span>
                  </div>
                </div>
                <span className="text-xs font-medium text-green-400 bg-green-500/10 px-3 py-1 rounded-full">Excellent</span>
              </div>

              {/* Analysis details mock */}
              <div className="flex-1 space-y-4">
                <div>
                  <p className="text-xs text-slate-500 mb-2 font-medium uppercase tracking-wide">Technical Skills Found</p>
                  <div className="flex flex-wrap gap-2">
                    {['React', 'Node.js', 'Python', 'AWS', 'Docker', 'SQL'].map(s => (
                      <span key={s} className="px-2.5 py-1 text-xs rounded-full bg-violet-500/20 text-violet-300 border border-violet-500/30 font-medium">{s}</span>
                    ))}
                  </div>
                </div>
                <div>
                  <p className="text-xs text-slate-500 mb-2 font-medium uppercase tracking-wide">Recommendations</p>
                  <div className="space-y-1.5">
                    {['Add quantifiable achievements', 'Include more industry keywords', 'Optimize action verbs'].map((r, i) => (
                      <div key={i} className="flex items-center gap-2 text-sm text-slate-300">
                        <CheckCircle size={13} className="text-violet-400 flex-shrink-0" />
                        {r}
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Floating badges */}
          <motion.div
            animate={{ y: [0, -8, 0] }}
            transition={{ duration: 4, repeat: Infinity, ease: 'easeInOut' }}
            className="absolute -top-4 -right-4 glass-card px-3 py-2 border border-white/20 flex items-center gap-2 text-sm"
          >
            <span className="w-2 h-2 rounded-full bg-green-400 animate-pulse" />
            <span className="text-white font-medium">AI Analysis Complete</span>
          </motion.div>

          <motion.div
            animate={{ y: [0, 8, 0] }}
            transition={{ duration: 5, repeat: Infinity, ease: 'easeInOut', delay: 1 }}
            className="absolute -bottom-4 -left-4 glass-card px-3 py-2 border border-white/20 flex items-center gap-2 text-sm"
          >
            <Shield size={14} className="text-violet-400" />
            <span className="text-white font-medium">ATS Optimized</span>
          </motion.div>
        </motion.div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-24 px-4">
        <div className="max-w-6xl mx-auto">
          <motion.div
            variants={containerVariants}
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            className="text-center mb-16"
          >
            <motion.p variants={itemVariants} className="text-violet-400 font-semibold text-sm uppercase tracking-widest mb-3">
              Features
            </motion.p>
            <motion.h2 variants={itemVariants} className="text-4xl font-bold text-white mb-4">
              Everything you need to land{' '}
              <span className="gradient-text">your dream job</span>
            </motion.h2>
            <motion.p variants={itemVariants} className="text-slate-400 text-lg max-w-2xl mx-auto">
              Our AI-powered platform gives you all the tools to stand out from the competition.
            </motion.p>
          </motion.div>

          <motion.div
            variants={containerVariants}
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            className="grid grid-cols-1 md:grid-cols-2 gap-6"
          >
            {features.map((feature, i) => (
              <motion.div
                key={feature.title}
                variants={itemVariants}
                whileHover={{ scale: 1.02, translateY: -4 }}
                className={`glass-card p-8 border ${feature.border} hover:shadow-xl transition-all duration-300 cursor-default`}
              >
                <div className={`w-12 h-12 ${feature.bg} rounded-xl flex items-center justify-center mb-5`}>
                  <feature.icon size={24} className={feature.color} />
                </div>
                <h3 className="text-xl font-bold text-white mb-3">{feature.title}</h3>
                <p className="text-slate-400 leading-relaxed">{feature.description}</p>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* How It Works */}
      <section id="how-it-works" className="py-24 px-4">
        <div className="max-w-5xl mx-auto">
          <motion.div
            variants={containerVariants}
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            className="text-center mb-16"
          >
            <motion.p variants={itemVariants} className="text-violet-400 font-semibold text-sm uppercase tracking-widest mb-3">
              How It Works
            </motion.p>
            <motion.h2 variants={itemVariants} className="text-4xl font-bold text-white mb-4">
              Get results in <span className="gradient-text">3 simple steps</span>
            </motion.h2>
          </motion.div>

          <motion.div
            variants={containerVariants}
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            className="grid grid-cols-1 md:grid-cols-3 gap-8 relative"
          >
            {/* Connector line */}
            <div className="hidden md:block absolute top-12 left-1/3 right-1/3 h-px bg-gradient-to-r from-violet-600/50 to-violet-600/50" />

            {steps.map((step, i) => (
              <motion.div key={step.step} variants={itemVariants} className="flex flex-col items-center text-center">
                <div className="relative mb-6">
                  <div className="w-24 h-24 rounded-2xl bg-violet-600/20 border border-violet-500/30 flex items-center justify-center">
                    <step.icon size={36} className="text-violet-400" />
                  </div>
                  <div className="absolute -top-3 -right-3 w-8 h-8 rounded-full bg-violet-600 flex items-center justify-center text-white text-xs font-bold">
                    {i + 1}
                  </div>
                </div>
                <h3 className="text-xl font-bold text-white mb-3">{step.title}</h3>
                <p className="text-slate-400 leading-relaxed">{step.description}</p>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-24 px-4">
        <div className="max-w-3xl mx-auto text-center">
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            whileInView={{ opacity: 1, scale: 1 }}
            viewport={{ once: true }}
            className="glass-card p-12 border border-violet-500/20 relative overflow-hidden"
          >
            <div className="absolute inset-0 bg-gradient-to-br from-violet-600/10 to-purple-600/5 pointer-events-none" />
            <div className="relative">
              <div className="w-16 h-16 rounded-2xl bg-violet-600 flex items-center justify-center mx-auto mb-6">
                <Sparkles size={28} className="text-white" />
              </div>
              <h2 className="text-4xl font-bold text-white mb-4">
                Ready to get hired faster?
              </h2>
              <p className="text-slate-400 text-lg mb-8">
                Join thousands of job seekers who've transformed their resumes and landed their dream jobs.
              </p>
              <div className="flex flex-col sm:flex-row gap-4 justify-center">
                <Link to="/register" className="btn-primary text-base px-8 py-4">
                  Start for Free <ArrowRight size={18} />
                </Link>
                <Link to="/login" className="btn-secondary text-base px-8 py-4">
                  Sign In
                </Link>
              </div>
            </div>
          </motion.div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-white/10 py-8 px-4">
        <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 rounded-md bg-violet-600 flex items-center justify-center">
              <FileText size={12} className="text-white" />
            </div>
            <span className="font-bold text-white text-sm">AI CV Analyzer</span>
          </div>
          <p className="text-slate-600 text-sm">© 2025 AI CV Analyzer. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
};

export default LandingPage;
