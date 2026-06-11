import api from './api';

export const adminService = {
  getStats: () => api.get('/admin/stats'),
  getUsers: (page = 0, size = 20) => api.get(`/admin/users?page=${page}&size=${size}`),
  searchUsers: (query, page = 0, size = 20) => api.get(`/admin/users?search=${query}&page=${page}&size=${size}`),
  getAIStatus: () => api.get('/ai/status'),
  toggleUserRole: (userId, role) => api.put(`/admin/users/${userId}/roles`, { role }),
};
