import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { CheckCircle, XCircle, Mail } from 'lucide-react';
import { authService } from '../../services/authService';
import LoadingSpinner from '../../components/ui/LoadingSpinner';

const VerifyEmailPage = () => {
  const [status, setStatus] = useState('loading'); // loading | success | error
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  useEffect(() => {
    if (!token) {
      setStatus('error');
      return;
    }

    authService.verifyEmail(token)
      .then(() => setStatus('success'))
      .catch(() => setStatus('error'));
  }, [token]);

  return (
    <div className="min-h-screen flex items-center justify-center px-4 pt-20">
      <div className="fixed inset-0 pointer-events-none overflow-hidden">
        <div className="absolute -top-40 -left-40 w-96 h-96 bg-violet-600/10 rounded-full blur-3xl" />
      </div>

      <motion.div
        initial={{ opacity: 0, y: 24 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-md"
      >
        <div className="glass-card p-10 border border-white/20 shadow-2xl shadow-black/50 text-center">
          {status === 'loading' && (
            <div className="flex flex-col items-center gap-4">
              <LoadingSpinner size="xl" />
              <div>
                <h2 className="text-xl font-bold text-white">Verifying your email</h2>
                <p className="text-slate-400 mt-1 text-sm">Please wait a moment...</p>
              </div>
            </div>
          )}

          {status === 'success' && (
            <motion.div initial={{ opacity: 0, scale: 0.9 }} animate={{ opacity: 1, scale: 1 }}>
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-green-500/20 border border-green-500/30 mb-6">
                <CheckCircle size={30} className="text-green-400" />
              </div>
              <h2 className="text-2xl font-bold text-white mb-3">Email Verified!</h2>
              <p className="text-slate-400 mb-8">
                Your email has been verified. You can now sign in to your account.
              </p>
              <Link to="/login" className="btn-primary px-8">
                <Mail size={18} />
                Sign In
              </Link>
            </motion.div>
          )}

          {status === 'error' && (
            <motion.div initial={{ opacity: 0, scale: 0.9 }} animate={{ opacity: 1, scale: 1 }}>
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-500/20 border border-red-500/30 mb-6">
                <XCircle size={30} className="text-red-400" />
              </div>
              <h2 className="text-2xl font-bold text-white mb-3">Verification Failed</h2>
              <p className="text-slate-400 mb-8">
                The verification link is invalid or has expired. Please request a new verification email.
              </p>
              <div className="flex gap-3 justify-center">
                <Link to="/register" className="btn-secondary px-6">Register Again</Link>
                <Link to="/login" className="btn-primary px-6">Sign In</Link>
              </div>
            </motion.div>
          )}
        </div>
      </motion.div>
    </div>
  );
};

export default VerifyEmailPage;
