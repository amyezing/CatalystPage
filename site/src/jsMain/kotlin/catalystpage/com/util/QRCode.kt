package catalystpage.com.util

@JsModule("qrcode")
@JsNonModule
external object QRCode {
    fun toDataURL(data: String, callback: (err: dynamic, url: String) -> Unit)
}