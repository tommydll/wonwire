import { useState } from 'react'
import { AuthContext } from './AuthContext'

export function AuthProvider({ children }) {
    const [user, setUser] = useState(() => {
        const token = localStorage.getItem('token')
        const email = localStorage.getItem('email')
        const firstName = localStorage.getItem('firstName')
        const lastName = localStorage.getItem('lastName')
        return token ? { token, email, firstName, lastName } : null
    })

    const login = (userData) => {
        localStorage.setItem('token', userData.token)
        localStorage.setItem('email', userData.email)
        localStorage.setItem('firstName', userData.firstName)
        localStorage.setItem('lastName', userData.lastName)
        setUser(userData)
    }

    const logout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('email')
        localStorage.removeItem('firstName')
        localStorage.removeItem('lastName')
        setUser(null)
    }

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
            {children}
        </AuthContext.Provider>
    )
}