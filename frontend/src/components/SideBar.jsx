import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { LayoutDashboard, ArrowRightLeft, History, Landmark, LogOut } from 'lucide-react'
import { useState } from 'react'

function Sidebar() {
    const { user, logout } = useAuth()
    const navigate = useNavigate()
    const [profileHovered, setProfileHovered] = useState(false)
    const [logoutHovered, setLogoutHovered] = useState(false)

    const handleLogout = () => {
        logout()
        navigate('/login')
    }

    const initials = (user?.firstName && user?.lastName)
        ? (user.firstName[0] + user.lastName[0]).toUpperCase()
        : '?'

    return (
        <div style={styles.sidebar}>
            <div style={styles.logo}>
                <h2 style={styles.logoText}>WonWire</h2>
            </div>

            <nav style={styles.nav}>
                <NavLink to="/dashboard" style={({ isActive }) => ({
                    ...styles.navItem,
                    ...(isActive ? styles.navItemActive : {})
                })}>
                    <LayoutDashboard size={20} />
                    <span>Dashboard</span>
                </NavLink>

                <NavLink to="/transfer" style={({ isActive }) => ({
                    ...styles.navItem,
                    ...(isActive ? styles.navItemActive : {})
                })}>
                    <ArrowRightLeft size={20} />
                    <span>Transfer</span>
                </NavLink>

                <NavLink to="/deposit" style={({ isActive }) => ({
                    ...styles.navItem,
                    ...(isActive ? styles.navItemActive : {})
                })}>
                    <Landmark size={20} />
                    <span>Deposit</span>
                </NavLink>

                <NavLink to="/history" style={({ isActive }) => ({
                    ...styles.navItem,
                    ...(isActive ? styles.navItemActive : {})
                })}>
                    <History size={20} />
                    <span>History</span>
                </NavLink>
            </nav>

            <div style={styles.userSection}>
                <div
                    style={{
                        ...styles.userProfile,
                        backgroundColor: profileHovered ? 'rgba(255,255,255,0.08)' : 'transparent',
                    }}
                    onClick={() => navigate('/profile')}
                    onMouseEnter={() => setProfileHovered(true)}
                    onMouseLeave={() => setProfileHovered(false)}
                >
                    <div style={styles.avatar}>
                        <span style={styles.initials}>{initials}</span>
                    </div>
                    <div style={styles.userInfo}>
                        <p style={styles.userName}>{user?.firstName} {user?.lastName}</p>
                        <p style={styles.userEmail}>{user?.email}</p>
                    </div>
                </div>
                <button
                    onClick={handleLogout}
                    style={{
                        ...styles.logoutButton,
                        color: logoutHovered ? '#ef4444' : '#8888aa',
                    }}
                    onMouseEnter={() => setLogoutHovered(true)}
                    onMouseLeave={() => setLogoutHovered(false)}
                >
                    <LogOut size={18} />
                </button>
            </div>
        </div>
    )
}

const styles = {
    sidebar: {
        width: '260px',
        minHeight: '100vh',
        backgroundColor: '#1a1a2e',
        display: 'flex',
        flexDirection: 'column',
        padding: '24px 16px',
        position: 'fixed',
        left: 0,
        top: 0,
        bottom: 0,
    },
    logo: {
        padding: '0 12px',
        marginBottom: '40px',
    },
    logoText: {
        color: 'white',
        fontSize: '22px',
        fontWeight: 'bold',
    },
    nav: {
        display: 'flex',
        flexDirection: 'column',
        gap: '4px',
        flex: 1,
    },
    navItem: {
        display: 'flex',
        alignItems: 'center',
        gap: '12px',
        padding: '12px',
        borderRadius: '8px',
        color: '#8888aa',
        textDecoration: 'none',
        fontSize: '15px',
        fontWeight: '500',
        transition: 'all 0.2s',
    },
    navItemActive: {
        backgroundColor: 'rgba(255,255,255,0.1)',
        color: 'white',
    },
    userSection: {
        display: 'flex',
        alignItems: 'center',
        gap: '4px',
        borderTop: '1px solid rgba(255,255,255,0.1)',
        marginTop: 'auto',
        paddingTop: '8px',
    },
    avatar: {
        width: '36px',
        height: '36px',
        borderRadius: '50%',
        backgroundColor: '#4f46e5',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        flexShrink: 0,
    },
    initials: {
        color: 'white',
        fontSize: '13px',
        fontWeight: 'bold',
    },
    userInfo: {
        flex: 1,
        overflow: 'hidden',
    },
    userName: {
        color: 'white',
        fontSize: '13px',
        fontWeight: '600',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
    },
    userEmail: {
        color: '#8888aa',
        fontSize: '11px',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
    },
    logoutButton: {
        background: 'none',
        border: 'none',
        color: '#8888aa',
        cursor: 'pointer',
        padding: '4px',
        display: 'flex',
        alignItems: 'center',
        flexShrink: 0,
    },
    userProfile: {
        display: 'flex',
        alignItems: 'center',
        gap: '10px',
        padding: '8px',
        borderRadius: '8px',
        cursor: 'pointer',
        transition: 'background-color 0.2s',
        flex: 1,
        overflow: 'hidden',
    },
}

export default Sidebar