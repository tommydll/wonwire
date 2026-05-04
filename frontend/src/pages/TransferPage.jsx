import { useState } from 'react'
import { v4 as uuidv4 } from 'uuid'
import { Send } from 'lucide-react'
import api from '../api/axiosConfig'

function TransferPage() {
    const [toEmail, setToEmail] = useState('')
    const [amount, setAmount] = useState('')
    const [description, setDescription] = useState('')
    const [error, setError] = useState(null)
    const [success, setSuccess] = useState(null)
    const [loading, setLoading] = useState(false)
    const [idempotencyKey] = useState(() => uuidv4())

    const formatAmount = (value) => {
        const numbers = value.replace(/[^0-9]/g, '')
        return numbers ? new Intl.NumberFormat('ko-KR').format(parseInt(numbers)) : ''
    }

    const handleAmountChange = (e) => {
        const raw = e.target.value.replace(/[^0-9]/g, '')
        setAmount(raw)
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setLoading(true)
        setError(null)
        setSuccess(null)

        try {
            await api.post('/api/transfers', {
                toEmail,
                amount: parseInt(amount),
                description,
                idempotencyKey
            })
            setSuccess('Transfer completed successfully!')
            setToEmail('')
            setAmount('')
            setDescription('')
        } catch (err) {
            setError(err.response?.data?.message || 'An error occurred')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div style={styles.container}>
            <h1 style={styles.title}>Send Money</h1>
            <p style={styles.subtitle}>Transfer funds to another WonWire user</p>

            <div style={styles.card}>
                {error && <div style={styles.error}>{error}</div>}
                {success && <div style={styles.success}>{success}</div>}

                <form onSubmit={handleSubmit} style={styles.form}>
                    <div style={styles.inputGroup}>
                        <label style={styles.label} htmlFor="toEmail">Recipient Email</label>
                        <input
                            id="toEmail"
                            type="text"
                            value={toEmail}
                            onChange={(e) => setToEmail(e.target.value)}
                            style={styles.input}
                            placeholder="recipient@email.com"
                            autoComplete="off"
                            required
                        />
                    </div>

                    <div style={styles.inputGroup}>
                        <label style={styles.label} htmlFor="amount">Amount (₩)</label>
                        <div style={styles.amountWrapper}>
                            <span style={styles.currency}>₩</span>
                            <input
                                id="amount"
                                type="text"
                                value={formatAmount(amount)}
                                onChange={handleAmountChange}
                                style={styles.amountInput}
                                placeholder="0"
                                required
                            />
                        </div>
                    </div>

                    <div style={styles.inputGroup}>
                        <label style={styles.label} htmlFor="description">
                            Description <span style={styles.optional}>(optional)</span>
                        </label>
                        <input
                            id="description"
                            type="text"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            style={styles.input}
                            placeholder="Lunch, rent, etc..."
                            autoComplete="off"
                        />
                    </div>

                    <button
                        type="submit"
                        style={styles.button}
                        disabled={loading}
                    >
                        <Send size={18} />
                        {loading ? 'Sending...' : 'Send Money'}
                    </button>
                </form>
            </div>
        </div>
    )
}

const styles = {
    container: {
        maxWidth: '600px',
    },
    title: {
        fontSize: '28px',
        fontWeight: 'bold',
        color: '#1a1a2e',
        marginBottom: '4px',
    },
    subtitle: {
        color: '#666',
        fontSize: '15px',
        marginBottom: '32px',
    },
    card: {
        backgroundColor: 'white',
        borderRadius: '16px',
        padding: '32px',
        boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
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
        gap: '20px',
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
    optional: {
        color: '#999',
        fontWeight: '400',
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
    amountWrapper: {
        display: 'flex',
        alignItems: 'center',
        border: '1px solid #ddd',
        borderRadius: '8px',
        overflow: 'hidden',
    },
    currency: {
        padding: '12px 16px',
        backgroundColor: '#f5f5f5',
        color: '#666',
        fontSize: '16px',
        fontWeight: '600',
        borderRight: '1px solid #ddd',
    },
    amountInput: {
        padding: '12px',
        border: 'none',
        fontSize: '16px',
        outline: 'none',
        flex: 1,
        width: '100%',
    },
    button: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '8px',
        padding: '14px',
        backgroundColor: '#1a1a2e',
        color: 'white',
        border: 'none',
        borderRadius: '8px',
        fontSize: '16px',
        fontWeight: '600',
        cursor: 'pointer',
        marginTop: '8px',
        opacity: 1,
    },
}

export default TransferPage