package me.oscarsanchez.cacaonet

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object OperatorDashboard : Screen("operator_dashboard")
    object ProducerDashboard : Screen("producer_dashboard")
    object BuyerDashboard : Screen("buyer_dashboard")

    object RegisterDelivery : Screen("register_delivery")
    object RegisterQuality : Screen("register_quality")
    object Reports : Screen("reports")
    object Traceability : Screen("traceability")
    object Offline : Screen("offline")

    object Inventory : Screen("inventory")
    object Payments : Screen("payments")

    // 👇 NUEVA RUTA
    object Producers : Screen("producers")
}