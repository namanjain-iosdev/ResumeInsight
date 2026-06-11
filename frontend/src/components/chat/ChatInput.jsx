import { useRef, useState } from 'react';
import { Send, Paperclip } from 'lucide-react';
import { motion } from 'framer-motion';

const ChatInput = ({ onSend, disabled = false, placeholder = 'Type your message...' }) => {
  const [value, setValue] = useState('');
  const textareaRef = useRef(null);

  const handleSend = () => {
    const trimmed = value.trim();
    if (!trimmed || disabled) return;
    onSend(trimmed);
    setValue('');
    // Reset textarea height
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
    }
  };

  const handleKeyDown = (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault();
      handleSend();
    }
  };

  const handleInput = (e) => {
    setValue(e.target.value);
    // Auto-resize textarea
    const el = textareaRef.current;
    if (el) {
      el.style.height = 'auto';
      el.style.height = Math.min(el.scrollHeight, 160) + 'px';
    }
  };

  const canSend = value.trim().length > 0 && !disabled;

  return (
    <div className="glass-card border border-white/10 p-3 rounded-2xl">
      <textarea
        ref={textareaRef}
        value={value}
        onChange={handleInput}
        onKeyDown={handleKeyDown}
        disabled={disabled}
        placeholder={placeholder}
        rows={1}
        className="w-full bg-transparent text-white placeholder-slate-500 text-sm resize-none
                   focus:outline-none leading-relaxed min-h-[40px] max-h-40"
      />
      <div className="flex items-center justify-between mt-2 pt-2 border-t border-white/10">
        <span className="text-xs text-slate-600">
          <kbd className="px-1.5 py-0.5 rounded bg-white/10 text-slate-500 font-mono text-xs">Ctrl</kbd>
          {' + '}
          <kbd className="px-1.5 py-0.5 rounded bg-white/10 text-slate-500 font-mono text-xs">Enter</kbd>
          {' to send'}
        </span>

        <motion.button
          whileTap={canSend ? { scale: 0.9 } : undefined}
          onClick={handleSend}
          disabled={!canSend}
          className={`flex items-center justify-center gap-2 px-4 py-2 rounded-xl text-sm font-medium
                      transition-all duration-200
                      ${canSend
                        ? 'bg-violet-600 text-white hover:bg-violet-500 shadow-lg shadow-violet-500/25'
                        : 'bg-white/5 text-slate-600 cursor-not-allowed'
                      }`}
        >
          <Send size={14} />
          Send
        </motion.button>
      </div>
    </div>
  );
};

export default ChatInput;
