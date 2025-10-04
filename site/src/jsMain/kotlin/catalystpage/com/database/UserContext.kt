package catalystpage.com.database

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import dto.UserDTO

val LocalUserState = compositionLocalOf { mutableStateOf<UserDTO?>(null) }