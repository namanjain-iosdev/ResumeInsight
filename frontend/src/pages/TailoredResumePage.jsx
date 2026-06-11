import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { Sparkles, Upload, Download, FileText, ShieldCheck, Loader2 } from 'lucide-react';
import toast from 'react-hot-toast';
import AppLayout from '../components/layout/AppLayout';
import ResumeComparison from '../components/resume/ResumeComparison';
import { resumeService } from '../services/resumeService';
import { tailoredResumeService } from '../services/tailoredResumeService';

const unwrap = (r) => r?.data ?? r;

const TailoredResumePage = () => {
  const [resumes, setResumes] = useState([]);
  const [selectedResumeId, setSelectedResumeId] = useState('');
  const [jobDescription, setJobDescription] = useState('');
  const [generating, setGenerating] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState(null);
  const [history, setHistory] = useState([]);

  const loadResumes = () =>
    resumeService.getAll(0, 50).then((r) => {
      const list = unwrap(r)?.content ?? [];
      setResumes(list);
      if (list.length && !selectedResumeId) setSelectedResumeId(String(list[0].id));
    });

  const loadHistory = () =>
    tailoredResumeService.getAll(0, 50).then((r) => setHistory(unwrap(r)?.content ?? []));

  useEffect(() => {
    loadResumes();
    loadHistory();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      const r = await resumeService.upload(file);
      const uploaded = unwrap(r);
      toast.success('Resume uploaded');
      await loadResumes();
      if (uploaded?.id) setSelectedResumeId(String(uploaded.id));
    } catch (err) {
      toast.error(err?.message || 'Upload failed');
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  };

  const handleGenerate = async () => {
    if (!selectedResumeId) return toast.error('Select or upload a resume first');
    if (!jobDescription.trim()) return toast.error('Paste a job description');
    setGenerating(true);
    setResult(null);
    try {
      const r = await tailoredResumeService.generate(Number(selectedResumeId), jobDescription);
      const generated = unwrap(r);
      setResult(generated);
      toast.success('Tailored resume generated');
      loadHistory();
    } catch (err) {
      toast.error(err?.message || 'Generation rejected — could not produce a grounded resume');
    } finally {
      setGenerating(false);
    }
  };

  const download = async (id) => {
    try {
      const blob = await tailoredResumeService.downloadPdf(id);
      const url = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
      const a = document.createElement('a');
      a.href = url;
      a.download = `tailored-resume-${id}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      toast.error('Download failed');
    }
  };

  return (
    <AppLayout>
      <div className="max-w-5xl mx-auto p-6 space-y-6">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-2">
            <Sparkles className="text-violet-400" size={22} /> Generate Tailored Resume
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Optimize an existing resume for a specific job — using only information already in your resume.
            Nothing is invented.
          </p>
        </div>

        {/* Form */}
        <div className="glass-card p-6 border border-white/10 space-y-5">
          <div>
            <label className="label">Resume</label>
            <div className="flex flex-col sm:flex-row gap-3">
              <select
                value={selectedResumeId}
                onChange={(e) => setSelectedResumeId(e.target.value)}
                className="input-field flex-1"
              >
                <option value="">Select a resume…</option>
                {resumes.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.originalFileName} {r.versionNumber ? `(v${r.versionNumber})` : ''}
                  </option>
                ))}
              </select>
              <label className="btn-secondary cursor-pointer whitespace-nowrap">
                {uploading ? <Loader2 size={16} className="animate-spin" /> : <Upload size={16} />}
                Upload new
                <input type="file" accept=".pdf,.docx,.doc,.txt" className="hidden" onChange={handleUpload} />
              </label>
            </div>
          </div>

          <div>
            <label className="label">Job Description</label>
            <textarea
              value={jobDescription}
              onChange={(e) => setJobDescription(e.target.value)}
              rows={8}
              placeholder="Paste the full job description here…"
              className="input-field resize-y"
            />
          </div>

          <motion.button
            whileTap={{ scale: 0.98 }}
            onClick={handleGenerate}
            disabled={generating}
            className="btn-primary w-full py-3"
          >
            {generating ? (
              <><Loader2 size={18} className="animate-spin" /> Generating &amp; validating…</>
            ) : (
              <><Sparkles size={18} /> Generate Tailored Resume</>
            )}
          </motion.button>
        </div>

        {/* Result */}
        {result && (
          <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} className="space-y-4">
            <div className="flex items-center justify-between flex-wrap gap-3">
              <div className="flex items-center gap-2 text-emerald-400 text-sm font-medium">
                <ShieldCheck size={16} />
                Grounding validated — no invented content (v{result.versionNumber}, {result.aiProvider})
              </div>
              <button onClick={() => download(result.id)} className="btn-secondary">
                <Download size={16} /> Download PDF
              </button>
            </div>
            <ResumeComparison
              original={result.originalContent}
              optimized={result.optimizedContent}
              changeSummary={result.changeSummary}
            />
          </motion.div>
        )}

        {/* History */}
        <section className="space-y-3">
          <h2 className="text-lg font-semibold text-white/80">Previously generated</h2>
          {history.length === 0 && <p className="text-slate-500 text-sm">No tailored resumes yet.</p>}
          <div className="grid gap-3">
            {history.map((g) => (
              <div key={g.id} className="glass-card p-4 border border-white/10 flex items-center justify-between gap-3">
                <div className="flex items-center gap-3 min-w-0">
                  <FileText size={18} className="text-violet-400 flex-shrink-0" />
                  <div className="min-w-0">
                    <p className="text-white text-sm truncate">
                      {g.originalFileName} — v{g.versionNumber}
                    </p>
                    <p className="text-slate-500 text-xs">
                      {g.aiProvider} · {g.createdAt ? new Date(g.createdAt).toLocaleString() : ''}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-2 flex-shrink-0">
                  <button
                    onClick={() => tailoredResumeService.getComparison(g.id).then((r) => setResult(unwrap(r)))}
                    className="text-xs text-violet-400 hover:text-violet-300"
                  >
                    Compare
                  </button>
                  <button onClick={() => download(g.id)} className="text-slate-400 hover:text-white">
                    <Download size={16} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </section>
      </div>
    </AppLayout>
  );
};

export default TailoredResumePage;
