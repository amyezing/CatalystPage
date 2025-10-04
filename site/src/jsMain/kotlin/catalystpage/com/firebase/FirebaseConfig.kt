package catalystpage.com.firebase

import catalystpage.com.database.Constants
import kotlinx.browser.window
import kotlinx.coroutines.await

object FirebaseConfig {
    var firebaseApp: dynamic = null
        private set

    // initializeApp is provided by firebase JS; rely on global here
    fun initializeWithConfig(config: dynamic) {
        // initializeApp must exist in the global scope (firebase/app is bundled)
        firebaseApp = initializeApp(config)
    }
}
suspend fun initFirebase(): Unit {
    try {
        // hit your backend route that returns the JSON config
        val resp = window.fetch("https://${Constants.HOST}/api/frontend-config").await()
        val json = resp.json().await().asDynamic()

        val configJs = js("({})")
        configJs.apiKey = json.apiKey
        configJs.authDomain = json.authDomain
        configJs.projectId = json.projectId
        configJs.storageBucket = json.storageBucket
        configJs.messagingSenderId = json.messagingSenderId
        configJs.appId = json.appId

        FirebaseConfig.initializeWithConfig(configJs)
        console.log("✅ Firebase initialized successfully")
    } catch (e: dynamic) {
        console.error("❌ Failed to fetch firebase config", e)
        throw e
    }
}

suspend fun getAuthInstance(): dynamic {
    if (FirebaseConfig.firebaseApp == null) {
        initFirebase()
    }
    return getAuth(FirebaseConfig.firebaseApp)
}