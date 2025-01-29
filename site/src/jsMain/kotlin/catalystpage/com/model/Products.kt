package catalystpage.com.model

import catalystpage.com.util.Res.Image.product1
import catalystpage.com.util.Res.Image.product2
import catalystpage.com.util.Res.Image.product3
import catalystpage.com.util.Res.Image.product4

enum class Products(
    val image: String,
    val title: String,
    val description: String,
    val path: String
) {
    One(
        image = product1,
        title = "Cherry Spice",
        description = "",
        path = "Cherry Spice"
    ),
    Two(
        image = product2,
        title = "Lemon Ginger",
        description = "",
        path = "Cherry Spice"
    ),
    Three(
        image = product3,
        title = "Chai Spice",
        description = "",
        path = "Cherry Spice"
    ),
    Four(
        image = product4,
        title = "Squash Spice",
        description = "",
        path = "Cherry Spice"
    )

}