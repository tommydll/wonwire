import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { Eye, EyeOff } from 'lucide-react'
import api from '../api/axiosConfig'
import Footer from '../components/Footer'

function RegisterPage() {
    const [fullName, setFullName] = useState('')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState(null)
    const [loading, setLoading] = useState(false)
    const [showPassword, setShowPassword] = useState(false)

    const navigate = useNavigate()

    const handleSubmit = async (e) => {
        e.preventDefault()
        setLoading(true)
        setError(null)

        try {
            await api.post('/api/auth/register', {
                email,
                password,
                fullName
            })
            navigate('/login')
        } catch (err) {
            setError(err.response?.data?.message || 'An error occurred')
            setPassword('')
        } finally {
            setLoading(false)
        }
    }

    return (
        <>
            <div style={styles.container}>
                <div style={styles.card}>
                    <h1 style={styles.title}>WonWire</h1>
                    <p style={styles.subtitle}>Create your account</p>

                    {error && <div style={styles.error}>{error}</div>}

                    <form onSubmit={handleSubmit} style={styles.form}>
                        <div style={styles.inputGroup}>
                            <label style={styles.label} htmlFor="fullName">Full Name</label>
                            <input
                                id="fullName"
                                type="text"
                                value={fullName}
                                onChange={(e) => setFullName(e.target.value)}
                                style={styles.input}
                                placeholder="Lee Minjeong"
                                autoComplete="off"
                                required
                            />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label} htmlFor="email">Email</label>
                            <input
                                id="email"
                                type="text"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                style={styles.input}
                                placeholder="your@email.com"
                                autoComplete="off"
                                required
                            />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label} htmlFor="password">Password</label>
                            <div style={styles.passwordWrapper}>
                                <input
                                    id="password"
                                    type={showPassword ? 'text' : 'password'}
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    style={styles.passwordInput}
                                    placeholder="••••••••"
                                    autoComplete="new-password"
                                    required
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    style={styles.eyeButton}
                                >
                                    {showPassword ? <Eye size={18} color="#999" /> : <EyeOff size={18} color="#999" />}
                                </button>
                            </div>
                        </div>

                        <button
                            type="submit"
                            style={styles.button}
                            disabled={loading}
                        >
                            {loading ? 'Creating account...' : 'Create account'}
                        </button>
                    </form>

                    <p style={styles.link}>
                        Already have an account? <Link to="/login">Sign in</Link>
                    </p>
                </div>
            </div>
            <Footer />
        </>
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
    card: {
        backgroundColor: 'white',
        padding: '40px',
        borderRadius: '12px',
        boxShadow: '0 2px 20px rgba(0,0,0,0.1)',
        width: '100%',
        maxWidth: '400px',
    },
    title: {
        fontSize: '28px',
        fontWeight: 'bold',
        color: '#1a1a2e',
        textAlign: 'center',
        marginBottom: '8px',
    },
    subtitle: {
        color: '#666',
        textAlign: 'center',
        marginBottom: '32px',
    },
    error: {
        backgroundColor: '#fee2e2',
        color: '#dc2626',
        padding: '12px',
        borderRadius: '8px',
        marginBottom: '16px',
        fontSize: '14px',
    },
    form: {
        display: 'flex',
        flexDirection: 'column',
        gap: '16px',
    },
    inputGroup: {
        display: 'flex',
        flexDirection: 'column',
        gap: '6px',
    },
    label: {
        fontSize: '14px',
        fontWeight: '500',
        color: '#333',
    },
    input: {
        padding: '12px',
        borderRadius: '8px',
        border: '1px solid #ddd',
        fontSize: '16px',
        outline: 'none',
        width: '100%',
        boxSizing: 'border-box',
    },
    passwordWrapper: {
        position: 'relative',
        width: '100%',
    },
    passwordInput: {
        width: '100%',
        padding: '12px',
        paddingRight: '40px',
        borderRadius: '8px',
        border: '1px solid #ddd',
        fontSize: '16px',
        outline: 'none',
        boxSizing: 'border-box',
    },
    eyeButton: {
        position: 'absolute',
        right: '12px',
        top: '50%',
        transform: 'translateY(-50%)',
        border: 'none',
        backgroundColor: 'transparent',
        cursor: 'pointer',
        color: '#999',
        padding: 0,
        display: 'flex',
        alignItems: 'center',
    },
    button: {
        padding: '12px',
        backgroundColor: '#1a1a2e',
        color: 'white',
        border: 'none',
        borderRadius: '8px',
        fontSize: '16px',
        fontWeight: '600',
        cursor: 'pointer',
        marginTop: '8px',
    },
    link: {
        textAlign: 'center',
        marginTop: '24px',
        fontSize: '14px',
        color: '#666',
    },
}

export default RegisterPage