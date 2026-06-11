import { useEffect, useRef, useState } from 'react';
import { motion } from 'framer-motion';

const getScoreColor = (score) => {
  if (score >= 70) return { stroke: '#22c55e', text: 'text-green-400', bg: 'bg-green-500/10', label: 'Excellent' };
  if (score >= 40) return { stroke: '#f59e0b', text: 'text-amber-400', bg: 'bg-amber-500/10', label: 'Fair' };
  return { stroke: '#ef4444', text: 'text-red-400', bg: 'bg-red-500/10', label: 'Needs Work' };
};

const ATSScoreRing = ({ score = 0, size = 'lg', showLabel = true, animate = true }) => {
  const [displayScore, setDisplayScore] = useState(animate ? 0 : score);
  const [strokeOffset, setStrokeOffset] = useState(0);
  
  const sizes = {
    sm: { svgSize: 80, strokeWidth: 6, radius: 32, fontSize: 'text-xl', labelSize: 'text-xs' },
    md: { svgSize: 120, strokeWidth: 8, radius: 48, fontSize: 'text-2xl', labelSize: 'text-xs' },
    lg: { svgSize: 160, strokeWidth: 10, radius: 64, fontSize: 'text-4xl', labelSize: 'text-sm' },
    xl: { svgSize: 200, strokeWidth: 12, radius: 84, fontSize: 'text-5xl', labelSize: 'text-base' },
  };

  const cfg = sizes[size];
  const circumference = 2 * Math.PI * cfg.radius;
  const colors = getScoreColor(score);

  useEffect(() => {
    if (!animate) {
      setDisplayScore(score);
      setStrokeOffset(circumference - (score / 100) * circumference);
      return;
    }

    // Animate the number count up
    const duration = 1500;
    const startTime = Date.now();
    const timer = setInterval(() => {
      const elapsed = Date.now() - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3); // ease-out cubic
      setDisplayScore(Math.round(eased * score));
      setStrokeOffset(circumference - (eased * score / 100) * circumference);
      if (progress >= 1) clearInterval(timer);
    }, 16);

    return () => clearInterval(timer);
  }, [score, animate, circumference]);

  return (
    <div className="flex flex-col items-center gap-3">
      <div className="relative" style={{ width: cfg.svgSize, height: cfg.svgSize }}>
        <svg
          width={cfg.svgSize}
          height={cfg.svgSize}
          viewBox={`0 0 ${cfg.svgSize} ${cfg.svgSize}`}
          className="-rotate-90"
        >
          {/* Background circle */}
          <circle
            cx={cfg.svgSize / 2}
            cy={cfg.svgSize / 2}
            r={cfg.radius}
            fill="none"
            stroke="rgba(255,255,255,0.08)"
            strokeWidth={cfg.strokeWidth}
          />
          {/* Score arc */}
          <circle
            cx={cfg.svgSize / 2}
            cy={cfg.svgSize / 2}
            r={cfg.radius}
            fill="none"
            stroke={colors.stroke}
            strokeWidth={cfg.strokeWidth}
            strokeLinecap="round"
            strokeDasharray={circumference}
            strokeDashoffset={strokeOffset}
            className="score-ring"
            style={{
              filter: `drop-shadow(0 0 8px ${colors.stroke}66)`,
              transition: animate ? 'stroke-dashoffset 1.5s cubic-bezier(0.34, 1.56, 0.64, 1)' : 'none',
            }}
          />
        </svg>

        {/* Score text overlay */}
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className={`font-bold ${cfg.fontSize} ${colors.text}`}>
            {displayScore}
          </span>
          <span className="text-slate-500 text-xs font-medium">/100</span>
        </div>
      </div>

      {showLabel && (
        <div className={`px-3 py-1 rounded-full ${colors.bg} border border-current/20`}>
          <span className={`text-sm font-semibold ${colors.text}`}>
            {colors.label} ATS Score
          </span>
        </div>
      )}
    </div>
  );
};

export default ATSScoreRing;
