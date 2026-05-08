import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import api from '../api/axiosConfig'
import Footer from '../components/Footer'

function ForgotPasswordPage() {
    const [email, setEmail] = useState('')
    const [loading, setLoading] = useState(false)
    const [success, setSuccess] = useState(null)
    const [error, setError] = useState(null)

    const handleSubmit = async (e) => {
        e.preventDefault()
        setLoading(true)
        setError(null)
        setSuccess(null)

        try {
            const response = await api.post('/api/auth/forgot-password', { email })
            setSuccess(response.data.message)
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
                    <p style={styles.subtitle}>Reset your password</p>
                    <p style={styles.description}>
                        {success
                            ? 'Check your inbox and follow the instructions in the email.'
                            : 'Enter your email address and we\'ll send you a link to reset your password.'
                        }
                    </p>

                    {error && <div style={styles.error}>{error}</div>}
                    {success && <div style={styles.success}>{success}</div>}

                    {!success && (
                        <form onSubmit={handleSubmit} style={styles.form}>
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
                                    disabled={loading}
                                />
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
                                    ? <><Loader2 size={18} style={styles.spinIcon} /> Sending...</>
                                    : 'Send reset link'
                                }
                            </button>
                        </form>
                    )}

                    <p style={styles.link}>
                        <Link to="/login">← Back to sign in</Link>
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
        color: '#1a1a2e',
        textAlign: 'center',
        fontWeight: '600',
        fontSize: '18px',
        marginBottom: '8px',
    },
    description: {
        color: '#666',
        textAlign: 'center',
        fontSize: '14px',
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

export default ForgotPasswordPage