import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { ArrowUpRight, ArrowDownLeft, ArrowRightLeft } from 'lucide-react'
import api from '../api/axiosConfig'
import PageLoader from "../components/PageLoader.jsx";

function DashboardPage() {
    const { user } = useAuth()
    const navigate = useNavigate()
    const [wallet, setWallet] = useState(null)
    const [lastTransaction, setLastTransaction] = useState(null)
    const [loading, setLoading] = useState(true)

    throw new Error('Test error boundary')

    const greeting = () => {
        const hour = new Date().getHours()
        if (hour < 12) return 'Good morning'
        if (hour < 18) return 'Good afternoon'
        return 'Good evening'
    }

    const formatBalance = (amount) => {
        return new Intl.NumberFormat('ko-KR').format(amount)
    }

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [walletRes, historyRes] = await Promise.all([
                    api.get('/api/wallet/balance'),
                    api.get('/api/transfers?page=0&size=1')
                ])
                setWallet(walletRes.data)
                const transactions = historyRes.data.content
                setLastTransaction(transactions.length > 0 ? transactions[0] : null)
            } catch (err) {
                console.error('Failed to fetch dashboard data', err)
            } finally {
                setLoading(false)
            }
        }

        fetchData()
    }, [])

    if (loading) return <PageLoader message="Loading your dashboard..." />

    const isReceived = lastTransaction?.toEmail === user?.email

    return (
        <div style={styles.container}>
            <h1 style={styles.greeting}>
                {greeting()}, {user?.fullName?.split(' ')[0]} 👋
            </h1>
            <p style={styles.subtitle}>Here's your financial overview</p>

            {/* Balance Card */}
            <div style={styles.balanceCard}>
                <p style={styles.balanceLabel}>Available Balance</p>
                <h2 style={styles.balanceAmount}>
                    ₩ {formatBalance(wallet?.balance || 0)}
                </h2>
                <p style={styles.balanceCurrency}>{wallet?.currency}</p>
                <button
                    style={styles.sendButton}
                    onClick={() => navigate('/transfer')}
                >
                    <ArrowRightLeft size={18} />
                    Send Money
                </button>
            </div>

            {/* Last Transaction */}
            <div style={styles.section}>
                <h3 style={styles.sectionTitle}>Last Transaction</h3>
                {lastTransaction ? (
                    <div style={styles.transactionCard}>
                        <div style={{
                            ...styles.transactionIcon,
                            backgroundColor: isReceived ? '#dcfce7' : '#fee2e2'
                        }}>
                            {isReceived
                                ? <ArrowDownLeft size={20} color="#16a34a" />
                                : <ArrowUpRight size={20} color="#dc2626" />
                            }
                        </div>
                        <div style={styles.transactionInfo}>
                            <p style={styles.transactionName}>
                                {isReceived ? `From ${lastTransaction.fromEmail}` : `To ${lastTransaction.toEmail}`}
                            </p>
                            <p style={styles.transactionDate}>
                                {new Date(lastTransaction.createdAt).toLocaleDateString('ko-KR')}
                            </p>
                            {lastTransaction.description && (
                                <p style={styles.transactionDescription}>
                                    {lastTransaction.description}
                                </p>
                            )}
                        </div>
                        <p style={{
                            ...styles.transactionAmount,
                            color: isReceived ? '#16a34a' : '#dc2626'
                        }}>
                            {isReceived ? '+' : '-'}₩{formatBalance(lastTransaction.amount)}
                        </p>
                    </div>
                ) : (
                    <div style={styles.emptyState}>
                        <p style={styles.emptyText}>No transactions yet</p>
                        <p style={styles.emptySubtext}>Send your first transfer to get started</p>
                    </div>
                )}
            </div>
        </div>
    )
}

const styles = {
    container: {
        maxWidth: '800px',
    },
    loadingContainer: {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
    },
    loadingText: {
        color: '#666',
        fontSize: '16px',
    },
    greeting: {
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
    balanceCard: {
        backgroundColor: '#1a1a2e',
        borderRadius: '16px',
        padding: '32px',
        marginBottom: '32px',
        color: 'white',
    },
    balanceLabel: {
        fontSize: '14px',
        color: 'rgba(255,255,255,0.6)',
        marginBottom: '8px',
    },
    balanceAmount: {
        fontSize: '42px',
        fontWeight: 'bold',
        marginBottom: '4px',
    },
    balanceCurrency: {
        fontSize: '14px',
        color: 'rgba(255,255,255,0.6)',
        marginBottom: '24px',
    },
    sendButton: {
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        backgroundColor: 'white',
        color: '#1a1a2e',
        border: 'none',
        borderRadius: '8px',
        padding: '12px 20px',
        fontSize: '15px',
        fontWeight: '600',
        cursor: 'pointer',
    },
    section: {
        marginBottom: '32px',
    },
    sectionTitle: {
        fontSize: '18px',
        fontWeight: '600',
        color: '#1a1a2e',
        marginBottom: '16px',
    },
    transactionCard: {
        backgroundColor: 'white',
        borderRadius: '12px',
        padding: '16px',
        display: 'flex',
        alignItems: 'center',
        gap: '16px',
        boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
    },
    transactionIcon: {
        width: '44px',
        height: '44px',
        borderRadius: '50%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        flexShrink: 0,
    },
    transactionInfo: {
        flex: 1,
    },
    transactionName: {
        fontSize: '15px',
        fontWeight: '600',
        color: '#1a1a2e',
        marginBottom: '2px',
    },
    transactionDate: {
        fontSize: '13px',
        color: '#999',
    },
    transactionDescription: {
        fontSize: '13px',
        color: '#666',
        marginTop: '2px',
    },
    transactionAmount: {
        fontSize: '16px',
        fontWeight: '700',
        flexShrink: 0,
    },
    emptyState: {
        backgroundColor: 'white',
        borderRadius: '12px',
        padding: '32px',
        textAlign: 'center',
        boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
    },
    emptyText: {
        fontSize: '16px',
        fontWeight: '600',
        color: '#1a1a2e',
        marginBottom: '4px',
    },
    emptySubtext: {
        fontSize: '14px',
        color: '#999',
    },
}

export default DashboardPage