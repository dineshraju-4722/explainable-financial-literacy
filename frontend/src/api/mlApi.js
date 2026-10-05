import axiosClient from './axiosClient';

export const mlApi = {
  getRequiredFeatures: async () => {
    const res = await axiosClient.get('/ml/features');
    return res.data.data;
  },

  predictArchetype: async (features) => {
    const res = await axiosClient.post('/ml/predict', { features });
    return res.data.data;
  },

  explainArchetype: async (features, targetClass, topK = 5) => {
    const res = await axiosClient.post('/ml/explain', {
      features,
      targetClass,
      topK,
    });
    return res.data.data;
  },
};

export default mlApi;
