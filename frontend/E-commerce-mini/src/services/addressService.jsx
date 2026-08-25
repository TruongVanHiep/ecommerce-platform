import axiosClient from "../axiosClient";

export const getMyAddresses = async () => {
  return axiosClient.get("/addresses");
};

export const createAddress = async (data) => {
  return axiosClient.post("/addresses", data);
};

export const updateAddress = async (id, data) => {
  return axiosClient.put(`/addresses/${id}`, data);
};

export const deleteAddress = async (id) => {
  return axiosClient.delete(`/addresses/${id}`);
};
