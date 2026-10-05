import axiosClient from './axiosClient';

export const educationApi = {
  getEducationRecommendations: async (assessmentId) => {
    const res = await axiosClient.get(`/assessments/${assessmentId}/education`);
    return res.data;
  },
};

export default educationApi;
