import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
    const [user, setUser] = useState(() => {
        const token = localStorage.getItem('token')
        const email = localStorage.getItem('email')
        const fullName = localStorage.getItem('fullName')
        return token ? { token, email, fullName } : null
    })

    const login = (userData) => {
        localStorage.setItem('token', userData.token)
        localStorage.setItem('email', userData.email)
        localStorage.setItem('fullName', userData.fullName)
        setUser(userData)
    }

    const logout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('email')
        localStorage.removeItem('fullName')
        setUser(null)
    }

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
            {children}
        </AuthContext.Provider>
    )
}

export function useAuth() {
    return useContext(AuthContext)
}