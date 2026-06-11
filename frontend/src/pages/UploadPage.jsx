import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import AppLayout from '../components/layout/AppLayout';
import FileUploadZone from '../components/resume/FileUploadZone';
import { resumeService } from '../services/resumeService';

const UploadPage = () => {
  const navigate = useNavigate();
  const [isUploading, setIsUploading] = useState(false);
  const [progress, setProgress] = useState(0);

  const handleFile = async (file) => {
    if (!file) return;
    setIsUploading(true);
    setProgress(0);
    try {
      const res = await resumeService.upload(file, (e) => {
        if (e.total) setProgress(Math.round((e.loaded * 100) / e.total));
      });
      toast.success('Resume uploaded');
      const id = res?.data?.id ?? res?.id;
      if (id) navigate(`/analyses?resumeId=${id}`);
      else navigate('/dashboard');
    } catch (err) {
      toast.error(err?.message || 'Upload failed');
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <AppLayout>
      <div className="max-w-3xl mx-auto p-6">
        <h1 className="text-2xl font-semibold text-white mb-6">Upload Resume</h1>
        <FileUploadZone onFileSelect={handleFile} isUploading={isUploading} uploadProgress={progress} />
      </div>
    </AppLayout>
  );
};

export default UploadPage;
