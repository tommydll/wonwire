import axios from 'axios'

const api = axios.create({
    baseURL: 'http://localhost:8080',
})

// Request interceptor — automatically adds JWT token to every request
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

// Response interceptor — redirects to login if token is expired or invalid
api.interceptors.response.use(
    (response) => response,
    (error) => {
        const isAuthEndpoint = error.config.url.includes('/api/auth/')
        if (error.response?.status === 401 && !isAuthEndpoint) {
            localStorage.removeItem('token')
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

export default api