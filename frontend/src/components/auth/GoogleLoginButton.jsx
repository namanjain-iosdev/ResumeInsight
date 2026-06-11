import { useEffect, useRef, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import toast from 'react-hot-toast';
import { authService } from '../../services/authService';
import useAuthStore from '../../store/authStore';

const GSI_SRC = 'https://accounts.google.com/gsi/client';
const CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID;

/** Loads the Google Identity Services script once. */
const loadGsiScript = () =>
  new Promise((resolve, reject) => {
    if (window.google?.accounts?.id) return resolve();
    const existing = document.querySelector(`script[src="${GSI_SRC}"]`);
    if (existing) {
      existing.addEventListener('load', () => resolve());
      existing.addEventListener('error', reject);
      return;
    }
    const script = document.createElement('script');
    script.src = GSI_SRC;
    script.async = true;
    script.defer = true;
    script.onload = () => resolve();
    script.onerror = reject;
    document.head.appendChild(script);
  });

const GoogleLoginButton = () => {
  const buttonRef = useRef(null);
  const [ready, setReady] = useState(false);
  const { login } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/dashboard';

  useEffect(() => {
    if (!CLIENT_ID) return; // Not configured — render fallback notice instead.

    let cancelled = false;

    const handleCredential = async (response) => {
      try {
        const res = await authService.google(response.credential);
        const payload = res?.data ?? res;
        login(payload, payload?.token);
        toast.success('Signed in with Google 🎉');
        navigate(from, { replace: true });
      } catch (err) {
        toast.error(err?.message || err?.error || 'Google sign-in failed');
      }
    };

    loadGsiScript()
      .then(() => {
        if (cancelled || !window.google?.accounts?.id) return;
        window.google.accounts.id.initialize({
          client_id: CLIENT_ID,
          callback: handleCredential,
        });
        if (buttonRef.current) {
          window.google.accounts.id.renderButton(buttonRef.current, {
            theme: 'filled_black',
            size: 'large',
            shape: 'pill',
            text: 'continue_with',
            width: 320,
          });
        }
        setReady(true);
      })
      .catch(() => toast.error('Could not load Google sign-in'));

    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!CLIENT_ID) {
    return (
      <p className="text-center text-xs text-slate-500">
        Google sign-in is not configured. Set <code>VITE_GOOGLE_CLIENT_ID</code> to enable it.
      </p>
    );
  }

  return (
    <div className="flex justify-center">
      <div ref={buttonRef} />
      {!ready && <span className="text-xs text-slate-500">Loading Google…</span>}
    </div>
  );
};

export default GoogleLoginButton;
