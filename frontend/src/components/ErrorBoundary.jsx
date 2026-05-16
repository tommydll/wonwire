import { Component } from 'react'
import { ROUTES } from '../routes.js'

class ErrorBoundary extends Component {
    constructor(props) {
        super(props)
        this.state = { hasError: false, error: null }
    }

    static getDerivedStateFromError(error) {
        return { hasError: true, error }
    }

    componentDidCatch(error, info) {
        console.error('ErrorBoundary caught:', error, info)
    }

    handleReset = () => {
        this.setState({ hasError: false, error: null })
        window.location.href = ROUTES.DASHBOARD
    }

    render() {
        if (this.state.hasError) {
            return (
                <div style={styles.container}>
                    <div style={styles.content}>
                        <p style={styles.icon}>⚠️</p>
                        <h1 style={styles.title}>Something went wrong</h1>
                        <p style={styles.subtitle}>
                            An unexpected error occurred. Please try again.
                        </p>
                        {this.state.error?.message && (
                            <p style={styles.errorDetail}>
                                {this.state.error.message}
                            </p>
                        )}
                        <button style={styles.button} onClick={this.handleReset}>
                            Go to Dashboard
                        </button>
                    </div>
                </div>
            )
        }

        return this.props.children
    }
}

const styles = {
    container: {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        backgroundColor: '#f5f5f5',
    },
    content: {
        textAlign: 'center',
        padding: '40px',
        maxWidth: '480px',
    },
    icon: {
        fontSize: '56px',
        marginBottom: '16px',
    },
    title: {
        fontSize: '26px',
        fontWeight: 'bold',
        color: '#1a1a2e',
        marginBottom: '12px',
    },
    subtitle: {
        fontSize: '15px',
        color: '#666',
        marginBottom: '16px',
    },
    errorDetail: {
        fontSize: '13px',
        color: '#999',
        backgroundColor: '#f0f0f0',
        padding: '10px 16px',
        borderRadius: '8px',
        marginBottom: '24px',
        fontFamily: 'monospace',
        wordBreak: 'break-word',
    },
    button: {
        padding: '12px 28px',
        backgroundColor: '#1a1a2e',
        color: 'white',
        border: 'none',
        borderRadius: '8px',
        fontSize: '15px',
        fontWeight: '600',
        cursor: 'pointer',
        marginTop: '8px',
    },
}

export default ErrorBoundary