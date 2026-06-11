import { NavLink, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  LayoutDashboard, Upload, FileText, BarChart2, Sparkles,
  MessageSquare, Shield, LogOut, FileText as Logo, ChevronRight, History
} from 'lucide-react';
import useAuthStore from '../../store/authStore';
import { authService } from '../../services/authService';
import toast from 'react-hot-toast';

const navItems = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/upload', icon: Upload, label: 'Upload Resume' },
  { to: '/tailored', icon: Sparkles, label: 'Tailored Resume' },
  { to: '/analyses', icon: BarChart2, label: 'Analysis History' },
  { to: '/audit', icon: History, label: 'Audit History' },
  { to: '/chat', icon: MessageSquare, label: 'Chat Assistant' },
];

const NavItem = ({ to, icon: Icon, label, onClick }) => (
  <NavLink
    to={to}
    onClick={onClick}
    className={({ isActive }) =>
      isActive
        ? 'nav-link-active relative'
        : 'nav-link group'
    }
  >
    {({ isActive }) => (
      <>
        {isActive && (
          <motion.div
            layoutId="activeNav"
            className="absolute inset-0 bg-violet-600/20 border border-violet-500/30 rounded-xl -z-10"
            transition={{ type: 'spring', damping: 30, stiffness: 400 }}
          />
        )}
        <Icon size={18} className={isActive ? 'text-violet-400' : 'text-slate-400 group-hover:text-white transition-colors'} />
        <span>{label}</span>
        {isActive && <ChevronRight size={14} className="ml-auto text-violet-400" />}
      </>
    )}
  </NavLink>
);

const Sidebar = ({ isMobileOpen, onMobileClose }) => {
  const { logout, isAdmin } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await authService.logout();
    } catch (e) {
      // Stateless logout — ignore backend errors and clear locally anyway.
    }
    logout();
    navigate('/login');
    toast.success('Signed out successfully');
  };

  const sidebarContent = (
    <div className="flex flex-col h-full">
      {/* Logo */}
      <div className="flex items-center gap-3 px-4 py-6 border-b border-white/10">
        <div className="w-9 h-9 rounded-xl bg-violet-600 flex items-center justify-center flex-shrink-0">
          <Logo size={18} className="text-white" />
        </div>
        <div>
          <p className="font-bold text-white text-sm">AI CV Analyzer</p>
          <p className="text-xs text-slate-500">Smart Resume Analysis</p>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
        <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 mb-3">
          Main Menu
        </p>
        {navItems.map((item) => (
          <NavItem key={item.to} {...item} onClick={onMobileClose} />
        ))}

        {isAdmin() && (
          <>
            <div className="my-4 border-t border-white/10" />
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 mb-3">
              Administration
            </p>
            <NavItem to="/admin" icon={Shield} label="Admin Panel" onClick={onMobileClose} />
          </>
        )}
      </nav>

      {/* Logout */}
      <div className="p-4 border-t border-white/10">
        <button
          onClick={handleLogout}
          className="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-slate-400 
                     hover:text-red-400 hover:bg-red-500/10 transition-all duration-200 font-medium"
        >
          <LogOut size={18} />
          <span>Sign Out</span>
        </button>
      </div>
    </div>
  );

  return (
    <>
      {/* Desktop Sidebar */}
      <aside className="hidden lg:flex flex-col w-64 fixed left-0 top-16 bottom-0 
                        glass-card rounded-none border-r border-white/10 border-l-0 border-b-0 border-t-0 z-30">
        {sidebarContent}
      </aside>

      {/* Mobile Sidebar Overlay */}
      <AnimatePresence>
        {isMobileOpen && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/60 z-40 lg:hidden"
              onClick={onMobileClose}
            />
            <motion.aside
              initial={{ x: -280 }}
              animate={{ x: 0 }}
              exit={{ x: -280 }}
              transition={{ type: 'spring', damping: 30, stiffness: 300 }}
              className="fixed left-0 top-0 bottom-0 w-72 glass-card border-r border-white/10 z-50 lg:hidden"
            >
              {sidebarContent}
            </motion.aside>
          </>
        )}
      </AnimatePresence>
    </>
  );
};

export default Sidebar;
