package catalystpage.com.model

enum class DashboardItem (
    val id : String,
    val title: String,
    val desc: String,
    val path: String
) {
    Profile(
        id = "profile",
        title = "Profile",
        desc = "Hello!",
        path = ""
    ),
    Shop(
        id = "shop",
        title = "Shop",
        desc = "",
        path = ""
    ),
    Cart(
        id = "cart",
        title = "Cart",
        desc = "",
        path = ""
    ),
    Orders(
        id = "my_orders",
        title = "My Orders",
        desc = "",
        path = ""
    ),
    Settings(
        id = "settings",
        title = "Settings",
        desc = "",
        path = ""
    )
}