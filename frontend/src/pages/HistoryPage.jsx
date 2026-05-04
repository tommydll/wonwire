import { useState, useEffect } from 'react'
import { useAuth } from '../context/useAuth'
import { ArrowUpRight, ArrowDownLeft } from 'lucide-react'
import api from '../api/axiosConfig'

function HistoryPage() {
    const { user } = useAuth()
    const [transactions, setTransactions] = useState([])
    const [loading, setLoading] = useState(true)
    const [currentPage, setCurrentPage] = useState(0)
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)

    const formatAmount = (amount) => {
        return new Intl.NumberFormat('ko-KR').format(amount)
    }

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        })
    }

    useEffect(() => {
        const fetchHistory = async () => {
            setLoading(true)
            try {
                const response = await api.get(`/api/transfers?page=${currentPage}&size=5`)
                setTransactions(response.data.content)
                setTotalPages(response.data.totalPages)
                setTotalElements(response.data.totalElements)
            } catch (err) {
                console.error('Failed to fetch history', err)
            } finally {
                setLoading(false)
            }
        }

        fetchHistory()
    }, [currentPage])

    if (loading) {
        return (
            <div style={styles.loadingContainer}>
                <p style={styles.loadingText}>Loading...</p>
            </div>
        )
    }

    return (
        <div style={styles.container}>
            <h1 style={styles.title}>Transaction History</h1>
            <p style={styles.subtitle}>
                {totalElements} transaction{totalElements !== 1 ? 's' : ''} total
            </p>

            {transactions.length === 0 ? (
                <div style={styles.emptyState}>
                    <p style={styles.emptyText}>No transactions yet</p>
                    <p style={styles.emptySubtext}>Your transaction history will appear here</p>
                </div>
            ) : (
                <>
                    <div style={styles.list}>
                        {transactions.map((tx) => {
                            const isReceived = tx.toEmail === user?.email
                            return (
                                <div key={tx.transactionId} style={styles.transactionCard}>
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
                                            {isReceived ? `From ${tx.fromEmail}` : `To ${tx.toEmail}`}
                                        </p>
                                        {tx.description && (
                                            <p style={styles.transactionDescription}>{tx.description}</p>
                                        )}
                                        <p style={styles.transactionDate}>{formatDate(tx.createdAt)}</p>
                                    </div>
                                    <div style={styles.transactionRight}>
                                        <p style={{
                                            ...styles.transactionAmount,
                                            color: isReceived ? '#16a34a' : '#dc2626'
                                        }}>
                                            {isReceived ? '+' : '-'}₩{formatAmount(tx.amount)}
                                        </p>
                                        <p style={styles.transactionStatus}>{tx.status}</p>
                                    </div>
                                </div>
                            )
                        })}
                    </div>

                    {totalPages > 1 && (
                        <div style={styles.pagination}>
                            <button
                                onClick={() => setCurrentPage(p => p - 1)}
                                disabled={currentPage === 0}
                                style={{
                                    ...styles.pageButton,
                                    opacity: currentPage === 0 ? 0.4 : 1
                                }}
                            >
                                ← Previous
                            </button>
                            <span style={styles.pageInfo}>
                                Page {currentPage + 1} of {totalPages}
                            </span>
                            <button
                                onClick={() => setCurrentPage(p => p + 1)}
                                disabled={currentPage === totalPages - 1}
                                style={{
                                    ...styles.pageButton,
                                    opacity: currentPage === totalPages - 1 ? 0.4 : 1
                                }}
                            >
                                Next →
                            </button>
                        </div>
                    )}
                </>
            )}
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
        height: '50vh',
    },
    loadingText: {
        color: '#666',
        fontSize: '16px',
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
    emptyState: {
        backgroundColor: 'white',
        borderRadius: '12px',
        padding: '48px',
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
    list: {
        display: 'flex',
        flexDirection: 'column',
        gap: '8px',
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
    transactionDescription: {
        fontSize: '13px',
        color: '#666',
        marginBottom: '2px',
    },
    transactionDate: {
        fontSize: '13px',
        color: '#999',
    },
    transactionRight: {
        textAlign: 'right',
        flexShrink: 0,
    },
    transactionAmount: {
        fontSize: '16px',
        fontWeight: '700',
        marginBottom: '4px',
    },
    transactionStatus: {
        fontSize: '12px',
        color: '#999',
        textTransform: 'uppercase',
        letterSpacing: '0.5px',
    },
    pagination: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '16px',
        marginTop: '24px',
    },
    pageButton: {
        padding: '8px 16px',
        backgroundColor: 'white',
        border: '1px solid #ddd',
        borderRadius: '8px',
        cursor: 'pointer',
        fontSize: '14px',
        color: '#1a1a2e',
        fontWeight: '500',
    },
    pageInfo: {
        fontSize: '14px',
        color: '#666',
    },
}

export default HistoryPage