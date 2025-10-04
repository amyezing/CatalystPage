package catalystpage.com.util

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.style.KobwebComposeStyleSheet.style
import dto.BadgeDTO
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import model.OrderStatus
import org.jetbrains.compose.web.css.media
import org.w3c.dom.css.StyleSheet
import org.w3c.dom.events.EventListener

@Composable
fun ObserveViewportEntered(
    sectionId: String,
    distanceFromTop: Double,
    onViewportEntered: () -> Unit
) {
    var viewportEntered by remember { mutableStateOf(false) }
    val listener = remember {
        EventListener {
            val top = document.getElementById(sectionId)?.getBoundingClientRect()?.top
            if (top != null && top < distanceFromTop) {
                viewportEntered = true
            }
        }
    }

    LaunchedEffect(viewportEntered) {
        if (viewportEntered) {
            onViewportEntered()
            window.removeEventListener(type = "scroll", callback = listener)
        } else {
            window.addEventListener(type = "scroll", callback = listener)
        }
    }
}

suspend fun animateNumbers(
    number: Int,
    delay: Long = 10L,
    onUpdate: (Int) -> Unit
) {
    (0..number).forEach {
        delay(delay)
        onUpdate(it)
    }
}

fun formatCurrency(value: Double): String {
    return value
        .asDynamic()
        .toLocaleString("en-PH", js("{ style: 'currency', currency: 'PHP' }"))
        .unsafeCast<String>()
}


fun formatReadableDate(dateString: String): String {
    return try {
        val date = js("new Date(dateString)")
        date.toLocaleString("en-US", js("{ dateStyle: 'long', timeStyle: 'short' }")) as String
    } catch (e: dynamic) {
        dateString
    }
}

fun mapPaymentStatusToOrderStatus(paymentStatus: String): OrderStatus {
    return when (paymentStatus.uppercase()) {
        "APPROVED" -> OrderStatus.Paid
        "REJECTED" -> OrderStatus.Cancelled
        else -> OrderStatus.Pending
    }
}


fun formatLabel(raw: String): String {
    return raw.split("_")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
}

fun getNextBadgeThreshold(userPoints: Int, badges: List<BadgeDTO>): Int? {
    // Sort badges by eco_points ascending
    val sorted = badges.sortedBy { it.ecoPoints }
    return sorted.firstOrNull { it.ecoPoints > userPoints }?.ecoPoints
}

fun generateQRCodeDataUrl(data: String, onGenerated: (String) -> Unit) {
    QRCode.toDataURL(data) { err, url ->
        if (err != null) {
            console.error("QR code generation error:", err)
            return@toDataURL
        }
        onGenerated(url)
    }
}

