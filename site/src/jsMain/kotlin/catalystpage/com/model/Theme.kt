package catalystpage.com.model

import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.css.rgba

enum class Theme(
    val hex: String,
    val rgb: CSSColorValue
) {
    MagicMint(hex = "#ADF8DD", rgb = rgb(r = 173, g = 248,b = 221)),
    PolishedPine(hex = "#5D9C8A", rgb = rgb(r = 93, g = 156,b = 138)),
    Bubbles(hex = "#EBFDF97", rgb = rgb(r = 235, g = 253,b = 249)),
    BrightGray(hex = "#E8F3F1", rgb = rgb(r = 232, g = 243,b = 241)),
    PalePink(hex = "F2D4C5", rgb = rgb(r = 242, g = 212, b = 197 ))


}

enum class ColorTheme(
    val hex: String,
    val rgba: CSSColorValue
) {
    Secondary(hex = "2B506F", rgba = rgba(r = 43, g = 80, b = 111, a = 0.1))
}