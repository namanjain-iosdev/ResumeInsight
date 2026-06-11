import { useCallback, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { motion, AnimatePresence } from 'framer-motion';
import { Upload, FileText, X, CheckCircle, AlertCircle, File } from 'lucide-react';

const MAX_SIZE = 10 * 1024 * 1024; // 10MB
const ACCEPTED_TYPES = {
  'application/pdf': ['.pdf'],
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
  'application/msword': ['.doc'],
};

const formatFileSize = (bytes) => {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
};

const FileUploadZone = ({ onFileSelect, uploadProgress = null, isUploading = false }) => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [error, setError] = useState('');

  const onDrop = useCallback((acceptedFiles, rejectedFiles) => {
    setError('');

    if (rejectedFiles.length > 0) {
      const rejection = rejectedFiles[0];
      if (rejection.errors[0]?.code === 'file-too-large') {
        setError('File is too large. Maximum size is 10MB.');
      } else if (rejection.errors[0]?.code === 'file-invalid-type') {
        setError('Invalid file type. Only PDF and DOCX files are accepted.');
      } else {
        setError('File rejected. Please check the file type and size.');
      }
      return;
    }

    if (acceptedFiles.length > 0) {
      const file = acceptedFiles[0];
      setSelectedFile(file);
      onFileSelect(file);
    }
  }, [onFileSelect]);

  const { getRootProps, getInputProps, isDragActive, isDragReject } = useDropzone({
    onDrop,
    accept: ACCEPTED_TYPES,
    maxSize: MAX_SIZE,
    maxFiles: 1,
    disabled: isUploading,
  });

  const removeFile = (e) => {
    e.stopPropagation();
    setSelectedFile(null);
    setError('');
    onFileSelect(null);
  };

  const getFileIcon = (filename) => {
    if (filename?.endsWith('.pdf')) return '📄';
    return '📝';
  };

  return (
    <div className="space-y-4">
      <div
        {...getRootProps()}
        className={`relative border-2 border-dashed rounded-2xl p-10 cursor-pointer transition-all duration-300
          ${isDragActive && !isDragReject
            ? 'border-violet-500 bg-violet-500/10 shadow-lg shadow-violet-500/20'
            : isDragReject
            ? 'border-red-500 bg-red-500/10'
            : selectedFile
            ? 'border-green-500/50 bg-green-500/5'
            : 'border-white/20 bg-white/5 hover:border-violet-500/50 hover:bg-violet-500/5'
          }
          ${isUploading ? 'opacity-60 cursor-not-allowed' : ''}
        `}
      >
        <input {...getInputProps()} />

        <AnimatePresence mode="wait">
          {selectedFile ? (
            <motion.div
              key="file-selected"
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.9 }}
              className="flex flex-col items-center gap-4"
            >
              <div className="relative">
                <div className="w-16 h-16 rounded-2xl bg-green-500/20 border border-green-500/30 
                                flex items-center justify-center text-3xl">
                  {getFileIcon(selectedFile.name)}
                </div>
                {!isUploading && (
                  <button
                    onClick={removeFile}
                    className="absolute -top-2 -right-2 w-6 h-6 bg-red-500 rounded-full flex items-center 
                               justify-center text-white hover:bg-red-400 transition-colors"
                  >
                    <X size={12} />
                  </button>
                )}
              </div>

              <div className="text-center">
                <p className="font-semibold text-white truncate max-w-xs">{selectedFile.name}</p>
                <p className="text-sm text-slate-400 mt-1">{formatFileSize(selectedFile.size)}</p>
              </div>

              {/* Upload Progress */}
              {isUploading && uploadProgress !== null && (
                <div className="w-full max-w-xs space-y-2">
                  <div className="flex justify-between text-sm text-slate-400">
                    <span>Uploading...</span>
                    <span>{uploadProgress}%</span>
                  </div>
                  <div className="h-2 bg-white/10 rounded-full overflow-hidden">
                    <motion.div
                      className="h-full bg-violet-500 rounded-full"
                      initial={{ width: 0 }}
                      animate={{ width: `${uploadProgress}%` }}
                      transition={{ duration: 0.3 }}
                    />
                  </div>
                </div>
              )}

              {!isUploading && (
                <div className="flex items-center gap-2 text-green-400 text-sm">
                  <CheckCircle size={16} />
                  <span>Ready to upload</span>
                </div>
              )}
            </motion.div>
          ) : (
            <motion.div
              key="drop-zone"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="flex flex-col items-center gap-4"
            >
              <motion.div
                animate={isDragActive ? { scale: 1.1, rotate: 5 } : { scale: 1, rotate: 0 }}
                transition={{ type: 'spring', stiffness: 300 }}
                className={`w-16 h-16 rounded-2xl flex items-center justify-center
                  ${isDragActive ? 'bg-violet-600/30 border border-violet-500' : 'bg-white/10 border border-white/20'}`}
              >
                <Upload size={28} className={isDragActive ? 'text-violet-400' : 'text-slate-400'} />
              </motion.div>

              <div className="text-center">
                {isDragActive ? (
                  <p className="text-lg font-semibold text-violet-400">Drop your resume here!</p>
                ) : (
                  <>
                    <p className="text-lg font-semibold text-white">
                      Drag & drop your resume
                    </p>
                    <p className="text-slate-400 mt-1">
                      or{' '}
                      <span className="text-violet-400 font-medium hover:text-violet-300 transition-colors">
                        browse files
                      </span>
                    </p>
                  </>
                )}
              </div>

              <div className="flex items-center gap-4 text-xs text-slate-500">
                <div className="flex items-center gap-1">
                  <FileText size={12} />
                  <span>PDF, DOC, DOCX</span>
                </div>
                <span>•</span>
                <div className="flex items-center gap-1">
                  <File size={12} />
                  <span>Max 10MB</span>
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* Error message */}
      <AnimatePresence>
        {error && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            className="flex items-center gap-2 text-red-400 text-sm bg-red-500/10 border border-red-500/20 rounded-xl px-4 py-3"
          >
            <AlertCircle size={16} className="flex-shrink-0" />
            <span>{error}</span>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default FileUploadZone;
