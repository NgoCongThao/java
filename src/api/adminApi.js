import axiosClient from './axiosClient';

export const adminLogin = (data) => {
  return axiosClient.post('/api/admin/login', data);
};

export const getAdminProfile = () => {
  return axiosClient.get('/api/admin/profile');
};