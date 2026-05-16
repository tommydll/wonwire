function PageLoader({ message = 'Loading...' }) {
    return (
        <div style={styles.container}>
            <div style={styles.spinner} />
            <p style={styles.text}>{message}</p>
        </div>
    )
}

const styles = {
    container: {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        height: '60vh',
        gap: '16px',
    },
    spinner: {
        width: '36px',
        height: '36px',
        border: '3px solid #e5e5e5',
        borderTop: '3px solid #1a1a2e',
        borderRadius: '50%',
        animation: 'spin 0.7s linear infinite',
    },
    text: {
        color: '#888',
        fontSize: '14px',
    },
}

export default PageLoader