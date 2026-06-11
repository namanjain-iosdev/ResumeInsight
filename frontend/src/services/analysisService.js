import api from './api';

export const analysisService = {
  analyze: (resumeId, jobDescription) => api.post('/analyses/analyze', { resumeId, jobDescription }),
  getHistory: (page = 0, size = 10) => api.get(`/analyses?page=${page}&size=${size}`),
  getById: (id) => api.get(`/analyses/${id}`),
  delete: (id) => api.delete(`/analyses/${id}`),
};
