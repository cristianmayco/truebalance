import axios from 'axios'
import { API_CONFIG } from '@/config/api'

const apiClient = axios.create({
  baseURL: API_CONFIG.baseURL,
  timeout: API_CONFIG.timeout,
  headers: API_CONFIG.headers,
})

// Request Interceptor
apiClient.interceptors.request.use(
  (config) => {
    // Adicionar token se necessário (futuro)
    // const token = localStorage.getItem('token')
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`
    // }
    return config
  },
  (error) => Promise.reject(error)
)

// Response Interceptor
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Tratamento global de erros
    if (error.response?.status === 401) {
      // Redirect para login (futuro)
    }
    return Promise.reject(error)
  }
)

export default apiClient;
export { apiClient as axiosInstance };
