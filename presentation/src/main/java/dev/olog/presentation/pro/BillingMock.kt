package dev.olog.presentation.pro

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class BillingMock : IBilling {

    companion object {
        private val STATE = BillingState(isTrial = false, isBought = true)
    }

    override fun observeBillingsState(): Flow<BillingState> {
        return flowOf(STATE)
    }

    override fun getBillingsState(): BillingState {
        return STATE
    }

    override fun purchasePremium() {

    }
}