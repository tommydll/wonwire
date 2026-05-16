import {useState} from 'react'
import {Link, useNavigate, useSearchParams} from 'react-router-dom'
import {Eye, EyeOff, Loader2} from 'lucide-react'
import api from '../api/axiosConfig'
import Footer from '../components/Footer'
import {ROUTES} from '../routes.js'

function ResetPasswordPage() {
    const [searchParams] = useSearchParams()
    const token = searchParams.get('token')
    const navigate = useNavigate()

    const [newPassword, setNewPassword] = useState('')
    const [showPassword, setShowPassword] = useState(false)
    const [loading, setLoading] = useState(false)
    const [success, setSuccess] = useState(null)
    const [error, setError] = useState(null)

    if (!token) {
        return (
            <>
                <div style={styles.container}>
                    <div style={styles.card}>
                        <h1 style={styles.title}>WonWire</h1>
                        <div style={styles.error}>
                            Invalid or missing reset token. Please request a new password reset.
                        </div>
                        <p style={styles.link}>
                            <Link to={ROUTES.FORGOT_PASSWORD}>Request a new link</Link>
                        </p>
                    </div>
                </div>
                <Footer/>
            </>
        )
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setLoading(true)
        setError(null)

        try {
            const response = await api.post('/api/auth/reset-password', {
                token,
                newPassword,
            })
            setSuccess(response.data.message)
            setTimeout(() => navigate(ROUTES.LOGIN), 6000)
        } catch (err) {
            setError(err.response?.data?.message || 'An error occurred')
        } finally {
            setLoading(false)
        }
    }

    return (
        <>
            <div style={styles.container}>
                <div style={styles.card}>
                    <h1 style={styles.title}>WonWire</h1>
                    <p style={styles.subtitle}>Choose a new password</p>

                    {error && <div style={styles.error}>{error}</div>}

                    {success ? (
                        <div style={styles.success}>
                            <p style={styles.successTitle}>✅ Password reset successfully!</p>
                            <p style={styles.successText}>{success}</p>
                            <p style={styles.successRedirect}>Redirecting to sign in...</p>
                        </div>
                    ) : (
                        <form onSubmit={handleSubmit} style={styles.form}>
                            <div style={styles.inputGroup}>
                                <label style={styles.label} htmlFor="newPassword">New Password</label>
                                <div style={styles.passwordWrapper}>
                                    <input
                                        id="newPassword"
                                        type={showPassword ? 'text' : 'password'}
                                        value={newPassword}
                                        onChange={(e) => setNewPassword(e.target.value)}
                                        style={styles.passwordInput}
                                        placeholder="••••••••"
                                        autoComplete="new-password"
                                        required
                                        disabled={loading}
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowPassword(!showPassword)}
                                        style={styles.eyeButton}
                                    >
                                        {showPassword
                                            ? <Eye size={18} color="#999"/>
                                            : <EyeOff size={18} color="#999"/>
                                        }
                                    </button>
                                </div>
                                <p style={styles.hint}>Minimum 8 characters</p>
                            </div>

                            <button
                                type="submit"
                                style={{
                                    ...styles.button,
                                    opacity: loading ? 0.7 : 1,
                                    cursor: loading ? 'not-allowed' : 'pointer',
                                }}
                                disabled={loading}
                            >
                                {loading
                                    ? <><Loader2 size={18} style={styles.spinIcon}/> Resetting...</>
                                    : 'Reset Password'
                                }
                            </button>
                        </form>
                    )}

                    <p style={styles.link}>
                        <Link to={ROUTES.LOGIN}>← Back to sign in</Link>
                    </p>
                </div>
            </div>
            <Footer/>
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
        marginBottom: '24px',
    },
    error: {
        backgroundColor: '#fee2e2',
        color: '#dc2626',
        padding: '12px',
        borderRadius: '8px',
        marginBottom: '16px',
        fontSize: '14px',
    },
    success: {
        backgroundColor: '#dcfce7',
        color: '#16a34a',
        padding: '16px',
        borderRadius: '8px',
        marginBottom: '16px',
    },
    successTitle: {
        fontWeight: '700',
        fontSize: '15px',
        marginBottom: '4px',
    },
    successText: {
        fontSize: '14px',
        marginBottom: '4px',
    },
    successRedirect: {
        fontSize: '13px',
        color: '#16a34a',
        opacity: 0.8,
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
        padding: 0,
        display: 'flex',
        alignItems: 'center',
    },
    hint: {
        fontSize: '12px',
        color: '#999',
    },
    button: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '8px',
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
    spinIcon: {
        animation: 'spin 0.7s linear infinite',
    },
    link: {
        textAlign: 'center',
        marginTop: '24px',
        fontSize: '14px',
        color: '#666',
    },
}

export default ResetPasswordPage