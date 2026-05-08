import { useState, useEffect } from 'react'
import { useAuth } from '../context/useAuth'
import { Loader2, Eye, EyeOff } from 'lucide-react'
import api from '../api/axiosConfig'
import PageLoader from '../components/PageLoader'

function ProfilePage() {
    const { user, login } = useAuth()

    // Profile form
    const [firstName, setFirstName] = useState('')
    const [lastName, setLastName] = useState('')
    const [email, setEmail] = useState('')
    const [profileLoading, setProfileLoading] = useState(true)
    const [profileSubmitting, setProfileSubmitting] = useState(false)
    const [profileSuccess, setProfileSuccess] = useState(null)
    const [profileError, setProfileError] = useState(null)

    // Password form
    const [currentPassword, setCurrentPassword] = useState('')
    const [newPassword, setNewPassword] = useState('')
    const [passwordSubmitting, setPasswordSubmitting] = useState(false)
    const [passwordSuccess, setPasswordSuccess] = useState(null)
    const [passwordError, setPasswordError] = useState(null)
    const [showCurrentPassword, setShowCurrentPassword] = useState(false)
    const [showNewPassword, setShowNewPassword] = useState(false)

    useEffect(() => {
        api.get('/api/user/profile')
            .then(res => {
                setFirstName(res.data.firstName)
                setLastName(res.data.lastName)
                setEmail(res.data.email)
            })
            .catch(err => console.error('Failed to fetch profile', err))
            .finally(() => setProfileLoading(false))
    }, [])

    const handleProfileSubmit = async (e) => {
        e.preventDefault()
        setProfileSubmitting(true)
        setProfileError(null)
        setProfileSuccess(null)

        try {
            const response = await api.put('/api/user/profile', {
                firstName,
                lastName,
                email,
            })
            setProfileSuccess('Profile updated successfully')
            // Update AuthContext so sidebar reflects new name/email
            login({
                ...user,
                firstName: response.data.firstName,
                lastName: response.data.lastName,
                email: response.data.email,
            })
        } catch (err) {
            setProfileError(err.response?.data?.message || 'An error occurred')
        } finally {
            setProfileSubmitting(false)
        }
    }

    const handlePasswordSubmit = async (e) => {
        e.preventDefault()
        setPasswordSubmitting(true)
        setPasswordError(null)
        setPasswordSuccess(null)

        try {
            const response = await api.post('/api/user/change-password', {
                currentPassword,
                newPassword,
            })
            setPasswordSuccess(response.data.message)
            setCurrentPassword('')
            setNewPassword('')
        } catch (err) {
            setPasswordError(err.response?.data?.message || 'An error occurred')
        } finally {
            setPasswordSubmitting(false)
        }
    }

    if (profileLoading) return <PageLoader message="Loading your profile..." />

    return (
        <div style={styles.container}>
            <h1 style={styles.title}>Profile</h1>
            <p style={styles.subtitle}>Manage your personal information</p>

            {/* Profile Info */}
            <div style={styles.section}>
                <h3 style={styles.sectionTitle}>Personal Information</h3>
                <div style={styles.card}>
                    {profileError && <div style={styles.error}>{profileError}</div>}
                    {profileSuccess && <div style={styles.success}>{profileSuccess}</div>}

                    <form onSubmit={handleProfileSubmit} style={styles.form}>
                        <div style={styles.row}>
                            <div style={styles.inputGroup}>
                                <label style={styles.label} htmlFor="firstName">First Name</label>
                                <input
                                    id="firstName"
                                    type="text"
                                    value={firstName}
                                    onChange={(e) => setFirstName(e.target.value)}
                                    style={styles.input}
                                    required
                                    disabled={profileSubmitting}
                                />
                            </div>
                            <div style={styles.inputGroup}>
                                <label style={styles.label} htmlFor="lastName">Last Name</label>
                                <input
                                    id="lastName"
                                    type="text"
                                    value={lastName}
                                    onChange={(e) => setLastName(e.target.value)}
                                    style={styles.input}
                                    required
                                    disabled={profileSubmitting}
                                />
                            </div>
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label} htmlFor="email">Email</label>
                            <input
                                id="email"
                                type="text"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                style={styles.input}
                                required
                                disabled={profileSubmitting}
                            />
                        </div>

                        <button
                            type="submit"
                            style={{
                                ...styles.button,
                                opacity: profileSubmitting ? 0.7 : 1,
                                cursor: profileSubmitting ? 'not-allowed' : 'pointer',
                            }}
                            disabled={profileSubmitting}
                        >
                            {profileSubmitting
                                ? <><Loader2 size={18} style={styles.spinIcon} /> Saving...</>
                                : 'Save Changes'
                            }
                        </button>
                    </form>
                </div>
            </div>

            {/* Change Password */}
            <div style={styles.section}>
                <h3 style={styles.sectionTitle}>Change Password</h3>
                <div style={styles.card}>
                    {passwordError && <div style={styles.error}>{passwordError}</div>}
                    {passwordSuccess && <div style={styles.success}>{passwordSuccess}</div>}

                    <form onSubmit={handlePasswordSubmit} style={styles.form}>
                        <div style={styles.inputGroup}>
                            <label style={styles.label} htmlFor="currentPassword">Current Password</label>
                            <div style={styles.passwordWrapper}>
                                <input
                                    id="currentPassword"
                                    type={showCurrentPassword ? 'text' : 'password'}
                                    value={currentPassword}
                                    onChange={(e) => setCurrentPassword(e.target.value)}
                                    style={styles.passwordInput}
                                    required
                                    disabled={passwordSubmitting}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                                    style={styles.eyeButton}
                                >
                                    {showCurrentPassword ? <Eye size={18} color="#999" /> : <EyeOff size={18} color="#999" />}
                                </button>
                            </div>
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label} htmlFor="newPassword">New Password</label>
                            <div style={styles.passwordWrapper}>
                                <input
                                    id="newPassword"
                                    type={showNewPassword ? 'text' : 'password'}
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    style={styles.passwordInput}
                                    placeholder="Minimum 8 characters"
                                    required
                                    disabled={passwordSubmitting}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowNewPassword(!showNewPassword)}
                                    style={styles.eyeButton}
                                >
                                    {showNewPassword ? <Eye size={18} color="#999" /> : <EyeOff size={18} color="#999" />}
                                </button>
                            </div>
                        </div>

                        <button
                            type="submit"
                            style={{
                                ...styles.button,
                                opacity: passwordSubmitting ? 0.7 : 1,
                                cursor: passwordSubmitting ? 'not-allowed' : 'pointer',
                            }}
                            disabled={passwordSubmitting}
                        >
                            {passwordSubmitting
                                ? <><Loader2 size={18} style={styles.spinIcon} /> Updating...</>
                                : 'Update Password'
                            }
                        </button>
                    </form>
                </div>
            </div>
        </div>
    )
}

const styles = {
    container: {
        maxWidth: '700px',
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
    section: {
        marginBottom: '32px',
    },
    sectionTitle: {
        fontSize: '18px',
        fontWeight: '600',
        color: '#1a1a2e',
        marginBottom: '16px',
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
    row: {
        display: 'flex',
        gap: '16px',
    },
    inputGroup: {
        display: 'flex',
        flexDirection: 'column',
        gap: '6px',
        flex: 1,
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
        backgroundColor: 'white',
    },
    button: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '8px',
        padding: '12px 24px',
        backgroundColor: '#1a1a2e',
        color: 'white',
        border: 'none',
        borderRadius: '8px',
        fontSize: '15px',
        fontWeight: '600',
        alignSelf: 'flex-start',
    },
    spinIcon: {
        animation: 'spin 0.7s linear infinite',
    },
    passwordWrapper: {
        position: 'relative',
        width: '100%',
    },
    passwordInput: {
        padding: '12px',
        paddingRight: '40px',
        borderRadius: '8px',
        border: '1px solid #ddd',
        fontSize: '16px',
        outline: 'none',
        width: '100%',
        boxSizing: 'border-box',
        backgroundColor: 'white',
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
}

export default ProfilePage