import {useEffect, useState} from 'react'
import {useNavigate} from 'react-router-dom'
import {useAuth} from '../context/useAuth'
import {ArrowDownLeft, ArrowRightLeft, ArrowUpRight, Landmark} from 'lucide-react'
import api from '../api/axiosConfig'
import PageLoader from "../components/PageLoader.jsx";

function DashboardPage() {
    const {user} = useAuth()
    const navigate = useNavigate()
    const [wallet, setWallet] = useState(null)
    const [lastTransaction, setLastTransaction] = useState(null)
    const [loading, setLoading] = useState(true)
    const [stats, setStats] = useState(null)
    const [hoveredBar, setHoveredBar] = useState(null)

    const greeting = () => {
        const hour = new Date().getHours()
        if (hour < 12) return 'Good morning'
        if (hour < 18) return 'Good afternoon'
        return 'Good evening'
    }

    const formatBalance = (amount) => {
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
        const fetchData = async () => {
            try {
                const [walletRes, historyRes, statsRes] = await Promise.all([
                    api.get('/api/wallet/balance'),
                    api.get('/api/transfers?page=0&size=1'),
                    api.get('/api/stats')
                ])
                setWallet(walletRes.data)
                const transactions = historyRes.data.content
                setLastTransaction(transactions.length > 0 ? transactions[0] : null)
                setStats(statsRes.data)
            } catch (err) {
                console.error('Failed to fetch dashboard data', err)
            } finally {
                setLoading(false)
            }
        }

        fetchData()
    }, [])

    if (loading) return <PageLoader message="Loading your dashboard..."/>

    const isDeposit = lastTransaction?.type === 'DEPOSIT'
    const isReceived = !isDeposit && lastTransaction?.toEmail === user?.email

    return (
        <div style={styles.container}>
            <h1 style={styles.greeting}>
                {greeting()}, {user?.firstName} 👋
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
                    <ArrowRightLeft size={18}/>
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
                            backgroundColor: isDeposit ? '#dbeafe' : isReceived ? '#dcfce7' : '#fee2e2'
                        }}>
                            {isDeposit
                                ? <Landmark size={20} color="#2563eb"/>
                                : isReceived
                                    ? <ArrowDownLeft size={20} color="#16a34a"/>
                                    : <ArrowUpRight size={20} color="#dc2626"/>
                            }
                        </div>
                        <div style={styles.transactionInfo}>
                            <p style={styles.transactionName}>
                                {isDeposit
                                    ? `Deposit via ${lastTransaction.description?.replace('Deposit via ', '')}`
                                    : isReceived
                                        ? `From ${lastTransaction.fromEmail}`
                                        : `To ${lastTransaction.toEmail}`
                                }
                            </p>
                            <p style={styles.transactionDate}>
                                {formatDate(lastTransaction.createdAt)}
                            </p>
                            {!isDeposit && lastTransaction.description && (
                                <p style={styles.transactionDescription}>
                                    {lastTransaction.description}
                                </p>
                            )}
                        </div>
                        <p style={{
                            ...styles.transactionAmount,
                            color: isDeposit ? '#2563eb' : isReceived ? '#16a34a' : '#dc2626'
                        }}>
                            {isDeposit ? '+' : isReceived ? '+' : '-'}₩{formatBalance(lastTransaction.amount)}
                        </p>
                    </div>
                ) : (
                    <div style={styles.emptyState}>
                        <p style={styles.emptyText}>No transactions yet</p>
                        <p style={styles.emptySubtext}>Send your first transfer to get started</p>
                    </div>
                )}
            </div>

            {/* Spending Statistics */}
            {stats && (
                <div style={styles.section}>
                    <h3 style={styles.sectionTitle}>Spending Statistics</h3>
                    <div style={styles.statsCard}>
                        <div style={styles.statsSummary}>
                            <div style={styles.statItem}>
                                <span style={styles.statLabel}>Total Sent</span>
                                <span style={{...styles.statValue, color: '#dc2626'}}>
                        -₩{formatBalance(stats.totalSent)}
                    </span>
                            </div>
                            <div style={styles.statDivider}/>
                            <div style={styles.statItem}>
                                <span style={styles.statLabel}>Total Received</span>
                                <span style={{...styles.statValue, color: '#16a34a'}}>
                        +₩{formatBalance(stats.totalReceived)}
                    </span>
                            </div>
                        </div>

                        <div style={styles.chartContainer}>
                            <div style={styles.chartHeader}>
                                <span style={styles.chartTitle}>📊 Last 6 months</span>
                            </div>
                            <svg width="100%" height="160" viewBox="0 0 600 160">
                                {(() => {
                                    const maxVal = Math.max(
                                        ...stats.monthly.map(x => Math.max(Number(x.sent), Number(x.received))),
                                        1
                                    )
                                    return stats.monthly.map((m, i) => {
                                        const barWidth = 40
                                        const gap = 60
                                        const x = i * gap + 20
                                        const maxHeight = 120

                                        const sentHeight = (Number(m.sent) / maxVal) * maxHeight
                                        const receivedHeight = (Number(m.received) / maxVal) * maxHeight

                                        const monthLabel = new Date(m.year, m.month - 1)
                                            .toLocaleDateString('en-US', {month: 'short'})

                                        return (
                                            <g key={`${m.year}-${m.month}`}>
                                                {/* Sent bar */}
                                                <rect
                                                    x={x}
                                                    y={maxHeight - sentHeight + 10}
                                                    width={barWidth / 2 - 2}
                                                    height={sentHeight || 2}
                                                    fill="#dc2626"
                                                    opacity="0.8"
                                                    rx="3"
                                                    style={{cursor: 'pointer'}}
                                                    onMouseEnter={() => setHoveredBar({
                                                        x: x + barWidth / 2,
                                                        y: maxHeight - sentHeight + 10,
                                                        label: monthLabel,
                                                        amount: Number(m.sent),
                                                        type: 'sent'
                                                    })}
                                                    onMouseLeave={() => setHoveredBar(null)}
                                                />
                                                {/* Received bar */}
                                                <rect
                                                    x={x + barWidth / 2}
                                                    y={maxHeight - receivedHeight + 10}
                                                    width={barWidth / 2 - 2}
                                                    height={receivedHeight || 2}
                                                    fill="#16a34a"
                                                    opacity="0.8"
                                                    rx="3"
                                                    style={{cursor: 'pointer'}}
                                                    onMouseEnter={() => setHoveredBar({
                                                        x: x + barWidth / 2 + barWidth / 4,
                                                        y: maxHeight - receivedHeight + 10,
                                                        label: monthLabel,
                                                        amount: Number(m.received),
                                                        type: 'received'
                                                    })}
                                                    onMouseLeave={() => setHoveredBar(null)}
                                                />
                                                {/* Month label */}
                                                <text
                                                    x={x + barWidth / 2}
                                                    y={150}
                                                    textAnchor="middle"
                                                    fontSize="11"
                                                    fill="#999"
                                                >
                                                    {monthLabel}
                                                </text>
                                            </g>
                                        )
                                    })
                                })()}
                                {hoveredBar && (() => {
                                    const tooltipY = hoveredBar.y < 40 ? hoveredBar.y + 36 : hoveredBar.y - 36
                                    const textY = hoveredBar.y < 40 ? hoveredBar.y + 55 : hoveredBar.y - 17
                                    return (
                                        <g pointerEvents="none">
                                            <rect
                                                x={hoveredBar.x - 50}
                                                y={tooltipY}
                                                width={100}
                                                height={28}
                                                fill="#1a1a2e"
                                                rx="6"
                                            />
                                            <text
                                                x={hoveredBar.x}
                                                y={textY}
                                                textAnchor="middle"
                                                fontSize="11"
                                                fill="white"
                                                fontWeight="600"
                                            >
                                                {hoveredBar.type === 'sent' ? '-' : '+'}₩{new Intl.NumberFormat('ko-KR').format(hoveredBar.amount)}
                                            </text>
                                        </g>
                                    )
                                })()}
                            </svg>

                            <div style={styles.chartLegend}>
                                <div style={styles.legendItem}>
                                    <div style={{...styles.legendDot, backgroundColor: '#dc2626'}}/>
                                    <span style={styles.legendLabel}>Sent</span>
                                </div>
                                <div style={styles.legendItem}>
                                    <div style={{...styles.legendDot, backgroundColor: '#16a34a'}}/>
                                    <span style={styles.legendLabel}>Received</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
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
    statsCard: {
        backgroundColor: 'white',
        borderRadius: '12px',
        padding: '24px',
        boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
    },
    statsSummary: {
        display: 'flex',
        alignItems: 'center',
        gap: '24px',
        marginBottom: '24px',
        paddingBottom: '20px',
        borderBottom: '1px solid #f0f0f0',
    },
    statItem: {
        display: 'flex',
        flexDirection: 'column',
        gap: '4px',
    },
    statLabel: {
        fontSize: '13px',
        color: '#999',
    },
    statValue: {
        fontSize: '20px',
        fontWeight: '700',
    },
    statDivider: {
        width: '1px',
        height: '36px',
        backgroundColor: '#f0f0f0',
    },
    chartContainer: {
        width: '100%',
    },
    chartLegend: {
        display: 'flex',
        gap: '16px',
        marginTop: '8px',
    },
    legendItem: {
        display: 'flex',
        alignItems: 'center',
        gap: '6px',
    },
    legendDot: {
        width: '10px',
        height: '10px',
        borderRadius: '50%',
    },
    legendLabel: {
        fontSize: '12px',
        color: '#666',
    },
    chartHeader: {
        display: 'flex',
        alignItems: 'center',
        marginBottom: '12px',
    },
    chartTitle: {
        fontSize: '14px',
        fontWeight: '600',
        color: '#444',
        textTransform: 'uppercase',
        letterSpacing: '0.5px',
    },
}

export default DashboardPage