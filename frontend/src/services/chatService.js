import api from './api';

export const chatService = {
  sendMessage: (message, chatType = 'GENERAL', resumeId = null) =>
    api.post('/chat/message', { message, chatType, resumeId }),
  getHistory: (page = 0, size = 50) => api.get(`/chat/history?page=${page}&size=${size}`),
  clearHistory: () => api.delete('/chat/history'),
};
