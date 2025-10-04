package catalystpage.com.model

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.padding
import kotlinx.serialization.Serializable
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Text

@Serializable
data class Cart(
    val id: String,
    var pack: Int,
    val subscription: Boolean,
    val price: Int,
    var status: String = "in_cart",
    var quantity: Int = 1

)


@Composable
fun CartItem(cart: Cart) {
    Column(modifier = Modifier.padding(10.px)) {
        Text("Pack: ${cart.pack}")
        Text("Subscription: ${if (cart.subscription) "Yes" else "No"}")
        Text("Price: ₱${cart.price}")
        Text("Status: ${cart.status}")
    }
}