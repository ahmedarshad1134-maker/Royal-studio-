package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Portfolio : Screen("portfolio", "Portfolio")
    object Services : Screen("services", "Services")
    object ServiceDetail : Screen("service_detail/{serviceId}", "Service Detail") {
        fun createRoute(serviceId: String) = "service_detail/$serviceId"
    }
    object Packages : Screen("packages", "Packages")
    object PackageDetail : Screen("package_detail/{packageId}", "Package Detail") {
        fun createRoute(packageId: String) = "package_detail/$packageId"
    }
    object Booking : Screen("booking", "Booking")
    object CustomerArea : Screen("customer_area", "Customer Area")
    object CustomerPrivateGallery : Screen("customer_private_gallery", "Customer Private Gallery")
    object PrivateGallery : Screen("private_gallery/{bookingId}", "Private Gallery") {
        fun createRoute(bookingId: String) = "private_gallery/$bookingId"
    }
    object Reviews : Screen("reviews", "Reviews")
    object About : Screen("about", "About")
    object Contact : Screen("contact", "Contact")
    object Admin : Screen("admin", "Admin")
    object AdminPanel : Screen("admin_panel", "Admin Panel")
}

val drawerScreens = listOf(
    Screen.Home,
    Screen.Portfolio,
    Screen.Services,
    Screen.Packages,
    Screen.Booking,
    Screen.Reviews,
    Screen.About,
    Screen.Contact,
    Screen.CustomerArea,
    Screen.Admin
)
