const skillColors = [
  'bg-violet-500/20 text-violet-300 border-violet-500/30',
  'bg-blue-500/20 text-blue-300 border-blue-500/30',
  'bg-cyan-500/20 text-cyan-300 border-cyan-500/30',
  'bg-emerald-500/20 text-emerald-300 border-emerald-500/30',
  'bg-amber-500/20 text-amber-300 border-amber-500/30',
  'bg-pink-500/20 text-pink-300 border-pink-500/30',
  'bg-indigo-500/20 text-indigo-300 border-indigo-500/30',
  'bg-teal-500/20 text-teal-300 border-teal-500/30',
];

const getColorForSkill = (skill) => {
  const idx = skill.charCodeAt(0) % skillColors.length;
  return skillColors[idx];
};

const SkillBadge = ({ skill, variant = 'auto', size = 'md' }) => {
  const colorClass = variant === 'auto' ? getColorForSkill(skill) : {
    violet: 'bg-violet-500/20 text-violet-300 border-violet-500/30',
    blue: 'bg-blue-500/20 text-blue-300 border-blue-500/30',
    green: 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30',
    red: 'bg-red-500/20 text-red-300 border-red-500/30',
    amber: 'bg-amber-500/20 text-amber-300 border-amber-500/30',
    slate: 'bg-white/10 text-slate-300 border-white/20',
  }[variant] || skillColors[0];

  const sizeClass = {
    sm: 'px-2 py-0.5 text-xs',
    md: 'px-3 py-1 text-sm',
    lg: 'px-4 py-1.5 text-base',
  }[size];

  return (
    <span className={`inline-flex items-center rounded-full border font-medium ${colorClass} ${sizeClass}`}>
      {skill}
    </span>
  );
};

export default SkillBadge;
