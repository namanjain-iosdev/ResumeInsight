import api from './api';

export const improvedResumeService = {
  improve: (analysisId) => api.post(`/improved-resumes/improve/${analysisId}`),
  getById: (id) => api.get(`/improved-resumes/${id}`),
  getByAnalysis: (analysisId) => api.get(`/improved-resumes/by-analysis/${analysisId}`),
  update: (id, content) => api.put(`/improved-resumes/${id}`, { content }),
  downloadPdf: (id) => api.get(`/improved-resumes/${id}/pdf`, { responseType: 'blob' }),
};
