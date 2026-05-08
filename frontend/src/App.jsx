import { Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ResetPasswordPage from "./pages/ResetPasswordPage.jsx";
import ForgotPasswordPage from "./pages/ForgotPasswordPage.jsx";
import DashboardPage from './pages/DashboardPage'
import TransferPage from './pages/TransferPage'
import HistoryPage from './pages/HistoryPage'
import NotFoundPage from "./pages/NotFoundPage.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import {useAuth} from "./context/useAuth.js";
import Layout from "./components/Layout.jsx";
import DepositPage from "./pages/DepositPage.jsx";

function App() {
    const { user } = useAuth()

    return (
        <Routes>
            <Route path="/" element={<Navigate to={user ? '/dashboard' : '/login'} />} />
            <Route path="/login" element={user ? <Navigate to="/dashboard" replace /> : <LoginPage />} />
            <Route path="/register" element={user ? <Navigate to="/dashboard" replace /> : <RegisterPage />} />
            <Route path="/forgot-password" element={user ? <Navigate to="/dashboard" replace /> : <ForgotPasswordPage />} />
            <Route path="/reset-password" element={user ? <Navigate to="/dashboard" replace /> : <ResetPasswordPage />} />
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
            <Route path="/deposit" element={
                <ProtectedRoute>
                    <Layout><DepositPage /></Layout>
                </ProtectedRoute>
            } />
            <Route path="/history" element={
                <ProtectedRoute>
                    <Layout><HistoryPage /></Layout>
                </ProtectedRoute>
            } />
            <Route path="*" element={<NotFoundPage />} />
        </Routes>
    )
}

export default App