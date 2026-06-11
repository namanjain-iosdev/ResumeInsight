import api from './api';

export const auditService = {
  getHistory: (page = 0, size = 10) => api.get(`/audit/history?page=${page}&size=${size}`),
  getLogs: (page = 0, size = 20) => api.get(`/audit/logs?page=${page}&size=${size}`),
  getResumeVersions: () => api.get('/audit/resume-versions'),
};
