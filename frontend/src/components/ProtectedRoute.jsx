import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import {ROUTES} from "../routes.js";

function ProtectedRoute({ children }) {
    const { user } = useAuth()

    if (!user) {
        return <Navigate to={ROUTES.LOGIN} replace />
    }

    return children
}

export default ProtectedRoute