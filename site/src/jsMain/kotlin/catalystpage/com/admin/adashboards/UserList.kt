package catalystpage.com.admin.adashboards

import androidx.compose.runtime.*
import catalystpage.com.admin.fetcher.UserFetcher
import com.varabyte.kobweb.compose.foundation.layout.Column
import dto.UserDTO
import org.jetbrains.compose.web.dom.*

@Composable
fun UserList() {
    var users by remember { mutableStateOf<List<UserDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fetch users from backend using fetcher
    LaunchedEffect(Unit) {
        try {
            users = UserFetcher.getAllUsers()
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    Column {
        if (isLoading) {
            P { Text("Loading users...") }
        } else if (error != null) {
            P { Text("Error: $error") }
        } else {
            Table {
                Thead {
                    Tr {
                        Th { Text("ID") }
                        Th { Text("Firebase UID") }
                        Th { Text("Email") }
                        Th { Text("Name") }
                        Th { Text("Phone") }
                        Th { Text("Role") }
                        Th { Text("Created At") }
                    }
                }
                Tbody {
                    users.forEach { user ->
                        Tr {
                            Td { Text(user.id?.toString() ?: "") }
                            Td { Text(user.firebaseUid) }
                            Td { Text(user.email ?: "") }
                            Td { Text(user.name ?: "") }
                            Td { Text(user.phone ?: "") }
                            Td { Text(user.roleRaw ?: "") }
                            Td { Text(user.createdAt ?: "") }
                        }
                    }
                }
            }
        }
    }
}