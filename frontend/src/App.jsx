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
import ProfilePage from "./pages/ProfilePage.jsx";
import { ROUTES } from './routes.js'

function App() {
    const { user } = useAuth()

    return (
        <Routes>
            <Route path={ROUTES.HOME} element={<Navigate to={user ? ROUTES.DASHBOARD : ROUTES.LOGIN} />} />
            <Route path={ROUTES.LOGIN} element={user ? <Navigate to={ROUTES.DASHBOARD} replace /> : <LoginPage />} />
            <Route path={ROUTES.REGISTER} element={user ? <Navigate to={ROUTES.DASHBOARD} replace /> : <RegisterPage />} />
            <Route path={ROUTES.FORGOT_PASSWORD} element={user ? <Navigate to={ROUTES.DASHBOARD} replace /> : <ForgotPasswordPage />} />
            <Route path={ROUTES.RESET_PASSWORD} element={user ? <Navigate to={ROUTES.DASHBOARD} replace /> : <ResetPasswordPage />} />
            <Route path={ROUTES.DASHBOARD} element={
                <ProtectedRoute>
                    <Layout><DashboardPage /></Layout>
                </ProtectedRoute>
            } />
            <Route path={ROUTES.TRANSFER} element={
                <ProtectedRoute>
                    <Layout><TransferPage /></Layout>
                </ProtectedRoute>
            } />
            <Route path={ROUTES.DEPOSIT} element={
                <ProtectedRoute>
                    <Layout><DepositPage /></Layout>
                </ProtectedRoute>
            } />
            <Route path={ROUTES.HISTORY} element={
                <ProtectedRoute>
                    <Layout><HistoryPage /></Layout>
                </ProtectedRoute>
            } />
            <Route path={ROUTES.PROFILE} element={
                <ProtectedRoute>
                    <Layout><ProfilePage /></Layout>
                </ProtectedRoute>
            } />
            <Route path="*" element={<NotFoundPage />} />
        </Routes>
    )
}

export default App