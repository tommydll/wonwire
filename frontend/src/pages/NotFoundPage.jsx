import {useNavigate} from 'react-router-dom'
import {useAuth} from '../context/useAuth'
import {ROUTES} from '../routes.js'

function NotFoundPage() {
    const navigate = useNavigate()
    const {user} = useAuth()

    return (
        <div style={styles.container}>
            <div style={styles.content}>
                <p style={styles.code}>404</p>
                <h1 style={styles.title}>Page not found</h1>
                <p style={styles.subtitle}>
                    The page you're looking for doesn't exist or has been moved.
                </p>
                <button
                    style={styles.button}
                    onClick={() => navigate(user ? ROUTES.DASHBOARD : ROUTES.LOGIN)}
                >
                    {user ? 'Back to Dashboard' : 'Back to Login'}
                </button>
            </div>
        </div>
    )
}

const styles = {
    container: {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        backgroundColor: '#f5f5f5',
    },
    content: {
        textAlign: 'center',
        padding: '40px',
    },
    code: {
        fontSize: '96px',
        fontWeight: 'bold',
        color: '#1a1a2e',
        lineHeight: 1,
        marginBottom: '16px',
        letterSpacing: '-4px',
    },
    title: {
        fontSize: '28px',
        fontWeight: 'bold',
        color: '#1a1a2e',
        marginBottom: '12px',
    },
    subtitle: {
        fontSize: '15px',
        color: '#666',
        marginBottom: '32px',
    },
    button: {
        padding: '12px 28px',
        backgroundColor: '#1a1a2e',
        color: 'white',
        border: 'none',
        borderRadius: '8px',
        fontSize: '15px',
        fontWeight: '600',
        cursor: 'pointer',
    },
}

export default NotFoundPage