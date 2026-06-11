import { useEffect, useState } from 'react';
import { Download, History, FileText, Sparkles, ShieldCheck } from 'lucide-react';
import toast from 'react-hot-toast';
import AppLayout from '../components/layout/AppLayout';
import { auditService } from '../services/auditService';
import { resumeService } from '../services/resumeService';
import { tailoredResumeService } from '../services/tailoredResumeService';

const unwrap = (r) => r?.data ?? r;
const fmt = (d) => (d ? new Date(d).toLocaleString() : '—');

const AuditHistoryPage = () => {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    auditService
      .getHistory(0, 50)
      .then((r) => setRows(unwrap(r)?.content ?? []))
      .catch(() => toast.error('Could not load audit history'))
      .finally(() => setLoading(false));
  }, []);

  const downloadOriginal = async (resumeId, name) => {
    try {
      const blob = await resumeService.download(resumeId);
      const url = window.URL.createObjectURL(new Blob([blob]));
      const a = document.createElement('a');
      a.href = url;
      a.download = name || `resume-${resumeId}`;
      a.click();
      window.URL.revokeObjectURL(url);
    } catch {
      toast.error('Download failed');
    }
  };

  const downloadGenerated = async (id) => {
    try {
      const blob = await tailoredResumeService.downloadPdf(id);
      const url = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
      const a = document.createElement('a');
      a.href = url;
      a.download = `generated-resume-${id}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    } catch {
      toast.error('Download failed');
    }
  };

  return (
    <AppLayout>
      <div className="max-w-6xl mx-auto p-6 space-y-6">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-2">
            <History className="text-violet-400" size={22} /> Audit History
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Full trail of every uploaded resume, its analyses, and the tailored resumes generated from it.
          </p>
        </div>

        {loading && <p className="text-slate-500 text-sm">Loading…</p>}
        {!loading && rows.length === 0 && (
          <p className="text-slate-500 text-sm">No activity yet. Upload a resume to get started.</p>
        )}

        <div className="space-y-4">
          {rows.map((row) => (
            <div key={row.resumeId} className="glass-card p-5 border border-white/10 space-y-4">
              {/* Resume header */}
              <div className="flex items-start justify-between gap-3 flex-wrap">
                <div className="flex items-start gap-3 min-w-0">
                  <FileText size={20} className="text-violet-400 flex-shrink-0 mt-0.5" />
                  <div className="min-w-0">
                    <p className="text-white font-medium truncate">
                      {row.originalFileName}
                      {row.versionNumber != null && (
                        <span className="ml-2 text-xs px-2 py-0.5 rounded-full bg-violet-600/20 text-violet-300">
                          Resume v{row.versionNumber}
                        </span>
                      )}
                    </p>
                    <p className="text-slate-500 text-xs mt-0.5">
                      Uploaded {fmt(row.uploadedAt)}
                      {row.fileSize ? ` · ${(row.fileSize / 1024).toFixed(0)} KB` : ''}
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => downloadOriginal(row.resumeId, row.originalFileName)}
                  className="btn-secondary text-sm"
                >
                  <Download size={15} /> Original
                </button>
              </div>

              {/* Analysis info */}
              <div className="grid sm:grid-cols-2 gap-3 text-sm">
                <div className="bg-white/5 rounded-lg px-3 py-2">
                  <p className="text-slate-500 text-xs">Analysis</p>
                  <p className="text-slate-300">
                    {row.analysisCount > 0
                      ? `${row.analysisCount} analysis · last ${fmt(row.lastAnalysisAt)}`
                      : 'Not analyzed yet'}
                  </p>
                </div>
                <div className="bg-white/5 rounded-lg px-3 py-2">
                  <p className="text-slate-500 text-xs">AI Provider</p>
                  <p className="text-slate-300">{row.analysisProvider || '—'}</p>
                </div>
              </div>

              {/* Generated versions */}
              {row.generatedVersions?.length > 0 && (
                <div className="space-y-2">
                  <p className="text-xs uppercase tracking-wider text-slate-500">Generated resumes</p>
                  {row.generatedVersions.map((g) => (
                    <div
                      key={g.id}
                      className="flex items-center justify-between gap-3 bg-white/5 rounded-lg px-3 py-2"
                    >
                      <div className="flex items-center gap-2 min-w-0">
                        <Sparkles size={14} className="text-emerald-400 flex-shrink-0" />
                        <span className="text-sm text-slate-300">Generated V{g.versionNumber}</span>
                        {g.validated && <ShieldCheck size={13} className="text-emerald-400" />}
                        <span className="text-xs text-slate-500">
                          {g.aiProvider} · {fmt(g.createdAt)}
                        </span>
                      </div>
                      <button
                        onClick={() => downloadGenerated(g.id)}
                        disabled={!g.hasPdf}
                        className="text-slate-400 hover:text-white disabled:opacity-40"
                        title={g.hasPdf ? 'Download generated PDF' : 'No PDF stored'}
                      >
                        <Download size={15} />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </AppLayout>
  );
};

export default AuditHistoryPage;
