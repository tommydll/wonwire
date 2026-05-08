import {useEffect, useState} from 'react'
import { useWalletBalance } from '../hooks/useWalletBalance'
import { v4 as uuidv4 } from 'uuid'
import { Send, Loader2 } from 'lucide-react'
import api from '../api/axiosConfig'

function TransferPage() {
    const [toEmail, setToEmail] = useState('')
    const [amount, setAmount] = useState('')
    const [description, setDescription] = useState('')
    const [error, setError] = useState(null)
    const [success, setSuccess] = useState(null)
    const [loading, setLoading] = useState(false)
    const [idempotencyKey, setIdempotencyKey] = useState(() => uuidv4())
    const [contacts, setContacts] = useState([])

    const { currentBalance, balanceLoading, refetchBalance } = useWalletBalance()

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
            setSuccess(`₩${new Intl.NumberFormat('ko-KR').format(amount)} successfully sent to ${toEmail}`)
            refetchBalance()
            api.get('/api/contacts')
                .then(res => setContacts(res.data))
                .catch(err => console.error('Failed to fetch contacts', err))
            setAmount('')
            setDescription('')
            setIdempotencyKey(uuidv4())
        } catch (err) {
            setError(err.response?.data?.message || 'An error occurred')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        refetchBalance()
        api.get('/api/contacts')
            .then(res => setContacts(res.data))
            .catch(err => console.error('Failed to fetch contacts', err))
    }, [refetchBalance])

    return (
        <div style={styles.wrapper}>
            <div style={styles.left}>
                <h1 style={styles.title}>Send Money</h1>
                <p style={styles.subtitle}>Transfer funds to another WonWire user</p>

                <div style={styles.balanceInfo}>
                    <p style={styles.balanceLabel}>Available Balance</p>
                    <p style={styles.balanceAmount}>
                        {balanceLoading
                            ? '...'
                            : `₩${new Intl.NumberFormat('ko-KR').format(currentBalance)}`
                        }
                    </p>
                </div>

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
                                disabled={loading}
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
                                    disabled={loading}
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
                                : <><Send size={18} /> Send Money</>
                            }
                        </button>
                    </form>
                </div>
            </div>

            {contacts.length > 0 && (
                <div style={styles.right}>
                    <p style={styles.contactsLabel}>Recent contacts</p>
                    <div style={styles.contactsList}>
                        {contacts.map((contact) => (
                            <button
                                key={contact.email}
                                type="button"
                                onClick={() => setToEmail(contact.email)}
                                style={{
                                    ...styles.contactChip,
                                    ...(toEmail === contact.email ? styles.contactChipActive : {}),
                                    backgroundColor: toEmail === contact.email ? 'white' : 'white',
                                }}
                            >
                                <div style={{
                                    ...styles.contactAvatar,
                                    ...(toEmail === contact.email ? styles.contactAvatarActive : {})
                                }}>
                                    {contact.fullName.charAt(0).toUpperCase()}
                                </div>
                                <div style={styles.contactInfo}>
                                    <span style={styles.contactName}>{contact.fullName}</span>
                                    <span style={styles.contactEmail}>{contact.email}</span>
                                </div>
                            </button>
                        ))}
                    </div>
                </div>
            )}
        </div>
    )
}

const styles = {
    wrapper: {
        display: 'flex',
        gap: '32px',
        alignItems: 'flex-start',
        maxWidth: '1000px',
    },
    left: {
        flex: 1,
        minWidth: 0,
    },
    right: {
        width: '280px',
        flexShrink: 0,
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
        backgroundColor: 'white',
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
        backgroundColor: 'white',
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
    contactsLabel: {
        fontSize: '13px',
        fontWeight: '500',
        color: '#999',
        marginBottom: '12px',
        textTransform: 'uppercase',
        letterSpacing: '0.5px',
    },
    contactsList: {
        display: 'flex',
        flexDirection: 'column',
        gap: '8px',
    },
    contactChip: {
        display: 'flex',
        alignItems: 'center',
        gap: '12px',
        padding: '12px 14px',
        backgroundColor: 'white',
        border: '2px solid transparent',
        borderRadius: '10px',
        cursor: 'pointer',
        textAlign: 'left',
        outline: 'none',
        boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
        width: '100%',
        transition: 'border-color 0.15s',
    },
    contactChipActive: {
        border: '2px solid #1a1a2e',
    },
    contactAvatar: {
        width: '36px',
        height: '36px',
        borderRadius: '50%',
        backgroundColor: '#e8e8f0',
        color: '#1a1a2e',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontWeight: '700',
        fontSize: '14px',
        flexShrink: 0,
    },
    contactAvatarActive: {
        backgroundColor: '#1a1a2e',
        color: 'white',
    },
    contactInfo: {
        display: 'flex',
        flexDirection: 'column',
        gap: '2px',
    },
    contactName: {
        fontSize: '14px',
        fontWeight: '600',
        color: '#1a1a2e',
    },
    contactEmail: {
        fontSize: '12px',
        color: '#999',
    },
}

export default TransferPage