import { Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import DashboardPage from './pages/DashboardPage'
import TransferPage from './pages/TransferPage'
import HistoryPage from './pages/HistoryPage'
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import {useAuth} from "./context/useAuth.js";
import Layout from "./components/Layout.jsx";

function App() {
    const { user } = useAuth()

    return (
        <Routes>
            <Route path="/" element={<Navigate to={user ? '/dashboard' : '/login'} />} />
            <Route path="/login" element={user ? <Navigate to="/dashboard" replace /> : <LoginPage />} />
            <Route path="/register" element={user ? <Navigate to="/dashboard" replace /> : <RegisterPage />} />
            <Route path="/dashboard" element={
                <ProtectedRoute>
                    <Layout><DashboardPage /></Layout>
                </ProtectedRoute>
            } />
            <Route path="/transfer" element={
                <ProtectedRoute>
                    <Layout><TransferPage /></Layout>
                </ProtectedRoute>
            } />
            <Route path="/history" element={
                <ProtectedRoute>
                    <Layout><HistoryPage /></Layout>
                </ProtectedRoute>
            } />
        </Routes>
    )
}

export default App