import { useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { FileText, BarChart2, Loader2, Sparkles } from 'lucide-react';
import toast from 'react-hot-toast';
import AppLayout from '../components/layout/AppLayout';
import AnalysisCard from '../components/analysis/AnalysisCard';
import { analysisService } from '../services/analysisService';
import { resumeService } from '../services/resumeService';

const unwrap = (r) => r?.data ?? r;
const fmt = (d) => (d ? new Date(d).toLocaleString() : '—');

const AnalysesPage = () => {
  const [searchParams] = useSearchParams();
  const initialResumeId = searchParams.get('resumeId');

  const [analyses, setAnalyses] = useState([]);
  const [resumes, setResumes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [jobDescription, setJobDescription] = useState('');
  const [analyzingId, setAnalyzingId] = useState(null);
  const autoRan = useRef(false);

  const loadAll = () =>
    Promise.all([analysisService.getHistory(0, 50), resumeService.getAll(0, 50)]).then(([a, r]) => {
      setAnalyses(unwrap(a)?.content ?? []);
      setResumes(unwrap(r)?.content ?? []);
    });

  const runAnalysis = async (resumeId) => {
    setAnalyzingId(resumeId);
    try {
      await analysisService.analyze(Number(resumeId), jobDescription || null);
      toast.success('Analysis complete');
      await loadAll();
    } catch (err) {
      toast.error(err?.message || 'Analysis failed');
    } finally {
      setAnalyzingId(null);
    }
  };

  useEffect(() => {
    loadAll()
      .catch(() => toast.error('Could not load analyses'))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // When arriving straight from an upload (?resumeId=...), analyze it once.
  useEffect(() => {
    if (!loading && initialResumeId && !autoRan.current) {
      autoRan.current = true;
      const already = analyses.some((a) => String(a.resumeId) === String(initialResumeId));
      if (!already) runAnalysis(initialResumeId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [loading, initialResumeId]);

  return (
    <AppLayout>
      <div className="max-w-5xl mx-auto p-6 space-y-8">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-2">
            <BarChart2 className="text-violet-400" size={22} /> Analysis History
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Analyze your uploaded resumes and review past AI evaluations.
          </p>
        </div>

        {/* Optional job description used for the next analysis */}
        <div className="glass-card p-4 border border-white/10">
          <label className="label">Job description (optional — improves keyword matching)</label>
          <textarea
            value={jobDescription}
            onChange={(e) => setJobDescription(e.target.value)}
            rows={3}
            placeholder="Paste a job description to tailor the analysis…"
            className="input-field resize-y"
          />
        </div>

        {/* Resumes with analyze action */}
        <section className="space-y-3">
          <h2 className="text-lg font-semibold text-white/80">Your Resumes</h2>
          {loading && <p className="text-slate-500 text-sm">Loading…</p>}
          {!loading && resumes.length === 0 && (
            <p className="text-slate-500 text-sm">No resumes uploaded yet.</p>
          )}
          <div className="grid gap-3">
            {resumes.map((r) => (
              <div
                key={r.id}
                className="glass-card p-4 border border-white/10 flex items-center justify-between gap-3"
              >
                <div className="flex items-center gap-3 min-w-0">
                  <FileText size={18} className="text-violet-400 flex-shrink-0" />
                  <div className="min-w-0">
                    <p className="text-white text-sm truncate">
                      {r.originalFileName || `Resume #${r.id}`}
                      {r.versionNumber != null && (
                        <span className="ml-2 text-xs px-2 py-0.5 rounded-full bg-violet-600/20 text-violet-300">
                          v{r.versionNumber}
                        </span>
                      )}
                    </p>
                    <p className="text-slate-500 text-xs">Uploaded {fmt(r.uploadedAt)}</p>
                  </div>
                </div>
                <button
                  onClick={() => runAnalysis(r.id)}
                  disabled={analyzingId === r.id}
                  className="btn-primary text-sm whitespace-nowrap disabled:opacity-60"
                >
                  {analyzingId === r.id ? (
                    <><Loader2 size={15} className="animate-spin" /> Analyzing…</>
                  ) : (
                    <><Sparkles size={15} /> {r.hasAnalysis ? 'Re-analyze' : 'Analyze'}</>
                  )}
                </button>
              </div>
            ))}
          </div>
        </section>

        {/* Past analyses */}
        <section className="space-y-3">
          <h2 className="text-lg font-semibold text-white/80">Analyses</h2>
          {!loading && analyses.length === 0 && (
            <p className="text-slate-500 text-sm">No analyses yet. Click “Analyze” on a resume above.</p>
          )}
          <div className="grid gap-3">
            {analyses.map((a) => (
              <AnalysisCard
                key={a.id}
                title={a.resumeFileName || `Analysis #${a.id}`}
                icon={BarChart2}
              >
                <div className="space-y-2 text-sm">
                  <div className="flex flex-wrap items-center gap-3">
                    <span className="px-2.5 py-1 rounded-lg bg-violet-600/20 text-violet-300 font-medium">
                      ATS Score: {a.atsScore ?? '—'}
                    </span>
                    {a.aiProvider && (
                      <span className="text-slate-500 text-xs">via {a.aiProvider}</span>
                    )}
                    <span className="text-slate-500 text-xs ml-auto">{fmt(a.createdAt)}</span>
                  </div>
                  {a.technicalSkills && (
                    <p className="text-slate-300">
                      <span className="text-slate-500">Skills: </span>
                      {a.technicalSkills}
                    </p>
                  )}
                  {a.recommendations && (
                    <p className="text-slate-400">
                      <span className="text-slate-500">Recommendations: </span>
                      {a.recommendations}
                    </p>
                  )}
                  {a.overallFeedback && (
                    <p className="text-slate-400 whitespace-pre-wrap">{a.overallFeedback}</p>
                  )}
                </div>
              </AnalysisCard>
            ))}
          </div>
        </section>
      </div>
    </AppLayout>
  );
};

export default AnalysesPage;
