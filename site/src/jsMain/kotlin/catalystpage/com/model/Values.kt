package catalystpage.com.model

import org.jetbrains.compose.web.css.CSSSizeValue
import org.jetbrains.compose.web.css.CSSUnit
import org.jetbrains.compose.web.css.percent

enum class Values(
    val title: String,
    val percentage: CSSSizeValue<CSSUnit.percent>
) {
    Quality(
        title = "Quality Craftsmanship",
        percentage = 100.percent
    ),
    Health(
        title = "Health & Wellness Focus",
        percentage = 98.percent
    ),
    Sustainable(
        title = "Sustainable Practices",
        percentage = 95.percent
    ),
    Customer(
        title = "Customer Satisfaction",
        percentage = 98.percent
    ),
    Innovation(
        title = "Innovation & Adaptability",
        percentage = 95.percent
    )

}