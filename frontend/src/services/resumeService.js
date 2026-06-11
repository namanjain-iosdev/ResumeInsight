import api from './api';

export const resumeService = {
  upload: (file, onUploadProgress) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/resumes/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress,
    });
  },
  getAll: (page = 0, size = 10) => api.get(`/resumes?page=${page}&size=${size}`),
  getById: (id) => api.get(`/resumes/${id}`),
  download: (id) => api.get(`/resumes/${id}/download`, { responseType: 'blob' }),
  delete: (id) => api.delete(`/resumes/${id}`),
};
