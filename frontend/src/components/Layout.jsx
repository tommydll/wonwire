import Sidebar from './Sidebar'
import Footer from './Footer'

function Layout({ children }) {
    return (
        <div style={styles.container}>
            <Sidebar />
            <main style={styles.main}>
                {children}
                <Footer />
            </main>
        </div>
    )
}

const styles = {
    container: {
        display: 'flex',
        minHeight: '100vh',
    },
    main: {
        marginLeft: '260px',
        flex: 1,
        padding: '40px',
        backgroundColor: '#f5f5f5',
        minHeight: '100vh',
        position: 'relative',
        paddingBottom: '60px',
    },
}

export default Layout