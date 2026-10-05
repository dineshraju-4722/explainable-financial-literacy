import axiosClient from './axiosClient';

export const assessmentApi = {
  createAssessment: async (userId, assessmentType = 'INITIAL') => {
    const res = await axiosClient.post('/assessments', { userId, assessmentType });
    return res.data.data;
  },

  getAssessment: async (id) => {
    const res = await axiosClient.get(`/assessments/${id}`);
    return res.data.data;
  },

  getAssessmentQuestions: async (id) => {
    const res = await axiosClient.get(`/assessments/${id}/questions`);
    return res.data.data;
  },

  submitAnswer: async (id, questionId, selectedOptionId) => {
    const res = await axiosClient.post(`/assessments/${id}/responses`, {
      questionId,
      selectedOptionId,
    });
    return res.data.data;
  },

  completeAssessment: async (id) => {
    const res = await axiosClient.post(`/assessments/${id}/complete`);
    return res.data.data;
  },

  getAssessmentResult: async (id) => {
    const res = await axiosClient.get(`/assessments/${id}/result`);
    return res.data.data;
  },

  getAssessmentAnalytics: async (id) => {
    const res = await axiosClient.get(`/assessments/${id}/analytics`);
    return res.data.data;
  },
};

export default assessmentApi;
