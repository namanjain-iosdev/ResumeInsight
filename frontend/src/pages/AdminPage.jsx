import { useEffect, useState } from 'react';
import AppLayout from '../components/layout/AppLayout';
import { adminService } from '../services/adminService';

const AdminPage = () => {
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([adminService.getStats(), adminService.getUsers()])
      .then(([s, u]) => {
        setStats(s?.data ?? s);
        setUsers(u?.data?.content ?? u?.content ?? []);
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <AppLayout>
    <div className="max-w-5xl mx-auto p-6 space-y-6">
      <h1 className="text-2xl font-semibold text-white">Admin Panel</h1>
      {loading ? (
        <p className="text-white/60 text-sm">Loading…</p>
      ) : (
        <>
          <section className="glass-card p-6">
            <h2 className="text-lg font-medium text-white mb-3">Stats</h2>
            <pre className="text-white/70 text-xs whitespace-pre-wrap">{JSON.stringify(stats, null, 2)}</pre>
          </section>
          <section className="glass-card p-6">
            <h2 className="text-lg font-medium text-white mb-3">Users ({users.length})</h2>
            <ul className="divide-y divide-white/10">
              {users.map((u) => (
                <li key={u.id} className="py-2 text-white/80 text-sm flex justify-between">
                  <span>{u.email}</span>
                  <span className="text-white/50">{(u.roles || []).join(', ')}</span>
                </li>
              ))}
            </ul>
          </section>
        </>
      )}
    </div>
    </AppLayout>
  );
};

export default AdminPage;
