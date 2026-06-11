import { ArrowRight, Sparkles, Shuffle, PenLine, Search } from 'lucide-react';

const ChangeGroup = ({ icon: Icon, title, items, color }) => {
  if (!items || items.length === 0) return null;
  return (
    <div className="space-y-2">
      <div className={`flex items-center gap-2 text-sm font-semibold ${color}`}>
        <Icon size={15} />
        <span>{title}</span>
      </div>
      <div className="flex flex-wrap gap-2">
        {items.map((it, i) => (
          <span
            key={i}
            className="px-2.5 py-1 rounded-lg bg-white/5 border border-white/10 text-xs text-slate-300"
          >
            {it}
          </span>
        ))}
      </div>
    </div>
  );
};

/**
 * Side-by-side Original vs Generated resume with a structured summary of what
 * changed (reordered sections, rewritten content, surfaced keywords, ATS wins).
 */
const ResumeComparison = ({ original, optimized, changeSummary }) => {
  const cs = changeSummary || {};
  return (
    <div className="space-y-6">
      {/* Change summary */}
      <div className="glass-card p-5 border border-white/10 space-y-4">
        <h3 className="text-white font-semibold flex items-center gap-2">
          <Sparkles size={16} className="text-violet-400" />
          What changed
        </h3>
        <div className="grid sm:grid-cols-2 gap-5">
          <ChangeGroup icon={Shuffle} title="Reordered sections" items={cs.reorderedSections} color="text-sky-400" />
          <ChangeGroup icon={PenLine} title="Rewritten content" items={cs.rewrittenSections} color="text-amber-400" />
          <ChangeGroup icon={Search} title="Keywords emphasized" items={cs.keywordsEmphasized} color="text-emerald-400" />
          <ChangeGroup icon={Sparkles} title="ATS improvements" items={cs.atsImprovements} color="text-violet-400" />
        </div>
        {!cs.reorderedSections?.length && !cs.rewrittenSections?.length &&
          !cs.keywordsEmphasized?.length && !cs.atsImprovements?.length && (
            <p className="text-slate-500 text-sm">No structured change summary was returned.</p>
          )}
      </div>

      {/* Side-by-side text */}
      <div className="grid md:grid-cols-2 gap-4">
        <div className="glass-card p-4 border border-white/10">
          <p className="text-xs uppercase tracking-wider text-slate-500 mb-2">Original Resume</p>
          <pre className="whitespace-pre-wrap text-sm text-slate-300 font-sans leading-relaxed max-h-[28rem] overflow-y-auto">
            {original || '—'}
          </pre>
        </div>
        <div className="glass-card p-4 border border-violet-500/30">
          <p className="text-xs uppercase tracking-wider text-violet-400 mb-2 flex items-center gap-1">
            Generated <ArrowRight size={12} /> Optimized
          </p>
          <pre className="whitespace-pre-wrap text-sm text-white font-sans leading-relaxed max-h-[28rem] overflow-y-auto">
            {optimized || '—'}
          </pre>
        </div>
      </div>
    </div>
  );
};

export default ResumeComparison;
