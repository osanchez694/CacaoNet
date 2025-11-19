package me.oscarsanchez.cacaonet

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object OperatorDashboard : Screen("dashboard_operator")
    object ProducerDashboard : Screen("dashboard_producer")
    object BuyerDashboard : Screen("dashboard_buyer")

    object RegisterDelivery : Screen("register_delivery")
    object RegisterQuality : Screen("register_quality")
    object Reports : Screen("reports")
    object Traceability : Screen("traceability")
    object Offline : Screen("offline")

    // 👇 NUEVAS RUTAS
    object Inventory : Screen("inventory")
    object Payments : Screen("payments")
}
