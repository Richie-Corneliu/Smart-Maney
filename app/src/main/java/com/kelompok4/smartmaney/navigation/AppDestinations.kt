package com.kelompok4.smartmaney.navigation

object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val DASHBOARD_ROUTE = "dashboard"
    const val WALLET_ROUTE = "wallet_route"
    const val SCAN_RECEIPT_ROUTE = "scan_receipt_route"
    const val BUDGET_PLANNING_ROUTE = "budget_planning_route"
    const val EXPENSE_HISTORY_ROUTE = "expense_history_route"
    const val EXPENSE_HISTORY_MODE_ARG = "mode"
    const val EXPENSE_HISTORY_MODE_DEFAULT = "history"
    const val EXPENSE_HISTORY_MODE_MONTHLY_RECAP = "monthly_recap"
    const val EXPENSE_HISTORY_ROUTE_PATTERN =
        "$EXPENSE_HISTORY_ROUTE?$EXPENSE_HISTORY_MODE_ARG={$EXPENSE_HISTORY_MODE_ARG}"
    const val PROFILE_ROUTE = "profile_route"
    const val TRANSACTION_ID_ARG = "transactionId"
    const val TRANSACTION_DETAIL_ROUTE = "transaction_detail_route"
    const val TRANSACTION_EDIT_ROUTE = "edit_transaction_route"
    const val TRANSACTION_DETAIL_ROUTE_PATTERN =
        "$TRANSACTION_DETAIL_ROUTE/{$TRANSACTION_ID_ARG}"
    const val TRANSACTION_EDIT_ROUTE_PATTERN =
        "$TRANSACTION_EDIT_ROUTE/{$TRANSACTION_ID_ARG}"

    fun expenseHistoryRoute(mode: String = EXPENSE_HISTORY_MODE_DEFAULT): String {
        return "$EXPENSE_HISTORY_ROUTE?$EXPENSE_HISTORY_MODE_ARG=$mode"
    }

    fun transactionDetailRoute(transactionId: Long): String {
        return "$TRANSACTION_DETAIL_ROUTE/$transactionId"
    }

    fun transactionEditRoute(transactionId: Long): String {
        return "$TRANSACTION_EDIT_ROUTE/$transactionId"
    }
}

