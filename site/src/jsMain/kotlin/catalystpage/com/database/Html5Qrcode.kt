@file:JsModule("html5-qrcode")
@file:JsNonModule

package catalystpage.com.database

import kotlin.js.Promise


external class Html5Qrcode(elementId: String) {
    fun start(
        cameraIdOrConfig: dynamic,
        config: dynamic,
        qrCodeSuccessCallback: (decodedText: String) -> Unit,
        qrCodeErrorCallback: (errorMessage: String) -> Unit = definedExternally
    ): Promise<Unit>

    fun stop(): Promise<Unit>
}



