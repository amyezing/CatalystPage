package catalystpage.com.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.CSSLengthValue
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLInputElement

@Composable
fun ToggleSwitch(
    id: String,
    checked: Boolean,
    label: String,
    onChange: (Boolean) -> Unit,
    labelWidth: CSSLengthValue = 120.px // fixed width for labels
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.px)
    ) {
        // Label text with fixed width
        Span(
            attrs = Modifier
                .width(labelWidth)        // set fixed width
                .textAlign(TextAlign.Left) // align text to the right
                .fontSize(14.px)
                .lineHeight(30.px)
                .toAttrs()
        ) {
            Text(label)
        }

        // Switch
        Label(
            attrs = Modifier.classNames("switch").toAttrs()
        ) {
            Input(
                type = InputType.Checkbox,
                attrs = {
                    id(id)
                    if (checked) attr("checked", "checked")
                    onInput { event ->
                        val input = event.target as HTMLInputElement
                        onChange(input.checked)
                    }
                    style { cursor("pointer") }
                }
            )

            Span(attrs = Modifier.classNames("slider").toAttrs())
        }
    }
}
