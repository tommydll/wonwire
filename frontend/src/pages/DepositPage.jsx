import {useEffect, useState} from 'react'
import { useWalletBalance } from '../hooks/useWalletBalance'
import { Landmark, Loader2 } from 'lucide-react'
import api from '../api/axiosConfig'

const PAYMENT_METHODS = [
    { id: 'kakao', label: 'Kakao Pay', emoji: '💛' },
    { id: 'toss', label: 'Toss', emoji: '🔵' },
    { id: 'shinhan', label: 'Shinhan Bank', emoji: '🏦' },
]

function DepositPage() {
    const [amount, setAmount] = useState('')
    const [selectedMethod, setSelectedMethod] = useState(null)
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState(null)
    const [success, setSuccess] = useState(null)

    const { currentBalance, balanceLoading, refetchBalance } = useWalletBalance()

    useEffect(() => {
        refetchBalance()
    }, [refetchBalance])

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
        if (!selectedMethod) {
            setError('Please select a payment method')
            return
        }

        setLoading(true)
        setError(null)
        setSuccess(null)

        try {
            const response = await api.post('/api/wallet/deposit', {
                amount: parseInt(amount),
                paymentMethod: selectedMethod.label,
            })
            setSuccess({
                amount: response.data.amount,
                newBalance: response.data.newBalance,
                paymentMethod: selectedMethod.label,
            })
            refetchBalance()
            setAmount('')
            setSelectedMethod(null)
        } catch (err) {
            setError(err.response?.data?.message || 'An error occurred')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div style={styles.container}>
            <h1 style={styles.title}>Deposit Funds</h1>
            <p style={styles.subtitle}>Add money to your WonWire wallet</p>

            <div style={styles.balanceInfo}>
                <p style={styles.balanceLabel}>Current Balance</p>
                <p style={styles.balanceAmount}>
                    {balanceLoading
                        ? '...'
                        : `₩${new Intl.NumberFormat('ko-KR').format(currentBalance)}`
                    }
                </p>
            </div>

            <div style={styles.card}>
                {error && <div style={styles.error}>{error}</div>}
                {success && (
                    <div style={styles.success}>
                        <p style={styles.successTitle}>Deposit successful! 🎉</p>
                        <p style={styles.successDetail}>
                            ₩{new Intl.NumberFormat('ko-KR').format(success.amount)} added via {success.paymentMethod}
                        </p>
                    </div>
                )}

                <form onSubmit={handleSubmit} style={styles.form}>
                    {/* Amount */}
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
                                disabled={loading}
                            />
                        </div>
                        <p style={styles.hint}>Minimum deposit: ₩1,000</p>
                    </div>

                    {/* Payment method */}
                    <div style={styles.inputGroup}>
                        <label style={styles.label}>Payment Method</label>
                        <div style={styles.methodGrid}>
                            {PAYMENT_METHODS.map((method) => (
                                <button
                                    key={method.id}
                                    type="button"
                                    onClick={() => setSelectedMethod(method)}
                                    disabled={loading}
                                    style={{
                                        ...styles.methodCard,
                                        ...(selectedMethod?.id === method.id ? styles.methodCardActive : {}),
                                        backgroundColor: selectedMethod?.id === method.id ? '#f0f0ff' : 'white',
                                    }}
                                >
                                    <span style={styles.methodEmoji}>{method.emoji}</span>
                                    <span style={styles.methodLabel}>{method.label}</span>
                                </button>
                            ))}
                        </div>
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
                            ? <><Loader2 size={18} style={styles.spinIcon} /> Processing...</>
                            : <><Landmark size={18} /> Deposit</>
                        }
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
    balanceInfo: {
        backgroundColor: '#e8e8f0',
        borderRadius: '10px',
        padding: '16px',
        marginBottom: '24px',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    balanceLabel: {
        fontSize: '14px',
        color: '#666',
        fontWeight: '500',
    },
    balanceAmount: {
        fontSize: '20px',
        fontWeight: '700',
        color: '#1a1a2e',
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
    successDetail: {
        fontSize: '14px',
        marginBottom: '2px',
    },
    successBalance: {
        fontSize: '14px',
        fontWeight: '600',
    },
    form: {
        display: 'flex',
        flexDirection: 'column',
        gap: '24px',
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
        backgroundColor: 'white',
    },
    hint: {
        fontSize: '12px',
        color: '#999',
    },
    methodGrid: {
        display: 'grid',
        gridTemplateColumns: 'repeat(3, 1fr)',
        gap: '12px',
    },
    methodCard: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '8px',
        padding: '16px 8px',
        backgroundColor: '#f9f9f9',
        border: '2px solid #eee',
        borderRadius: '12px',
        outline: 'none',
        cursor: 'pointer',
        transition: 'all 0.2s',
    },
    methodCardActive: {
        border: '2px solid #1a1a2e',
    },
    methodEmoji: {
        fontSize: '24px',
    },
    methodLabel: {
        fontSize: '13px',
        fontWeight: '600',
        color: '#1a1a2e',
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
        marginTop: '8px',
        transition: 'opacity 0.2s',
    },
    spinIcon: {
        animation: 'spin 0.7s linear infinite',
    },
}

export default DepositPage