import { useState, useCallback } from 'react'
import api from '../api/axiosConfig'

export function useWalletBalance() {
    const [currentBalance, setCurrentBalance] = useState(null)
    const [balanceLoading, setBalanceLoading] = useState(false)

    const refetchBalance = useCallback(() => {
        setBalanceLoading(true)
        api.get('/api/wallet/balance')
            .then(res => setCurrentBalance(res.data.balance))
            .catch(err => console.error('Failed to fetch balance', err))
            .finally(() => setBalanceLoading(false))
    }, [])

    return { currentBalance, balanceLoading, refetchBalance }
}