function Footer() {
    return (
        <footer style={styles.footer}>
            <p style={styles.text}>
                WonWire · Built by <strong>Thomas Daullé</strong>
            </p>
        </footer>
    )
}

const styles = {
    footer: {
        position: 'fixed',
        bottom: 0,
        width: '100%',
        textAlign: 'center',
        padding: '12px',
        backgroundColor: 'white',
        borderTop: '1px solid #eee',
    },
    text: {
        fontSize: '13px',
        color: '#999',
    },
}

export default Footer