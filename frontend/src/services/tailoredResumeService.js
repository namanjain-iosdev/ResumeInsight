import api from './api';

export const tailoredResumeService = {
  generate: (resumeId, jobDescription) =>
    api.post('/tailored-resumes/generate', { resumeId, jobDescription }),
  getAll: (page = 0, size = 10) => api.get(`/tailored-resumes?page=${page}&size=${size}`),
  getById: (id) => api.get(`/tailored-resumes/${id}`),
  getByResume: (resumeId) => api.get(`/tailored-resumes/by-resume/${resumeId}`),
  getComparison: (id) => api.get(`/tailored-resumes/${id}/comparison`),
  downloadPdf: (id) => api.get(`/tailored-resumes/${id}/download-pdf`, { responseType: 'blob' }),
};
