@file:JsModule("firebase/auth")
@file:JsNonModule


package catalystpage.com.firebase

import kotlin.js.Promise

external interface UserCredential {
    val user: dynamic
}

external class GoogleAuthProvider

@JsName("getAuth")
external fun getAuth(app: dynamic = definedExternally): dynamic

@JsName("signInWithPopup")
external fun signInWithPopup(auth: dynamic, provider: GoogleAuthProvider): Promise<UserCredential>

@JsName("signOut")
external fun signOut(auth: dynamic): Promise<Unit>

