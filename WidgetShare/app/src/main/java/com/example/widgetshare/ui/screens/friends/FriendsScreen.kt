package com.example.widgetshare.ui.screens.friends

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.widgetshare.data.models.User
import com.example.widgetshare.data.repository.UserRepository
import kotlinx.coroutines.launch

@Composable
fun FriendsScreen(navController: NavController) {
    val userRepository = remember { UserRepository() }
    val scope = rememberCoroutineScope()
    var friends by remember { mutableStateOf<List<User>>(emptyList()) }
    var friendRequests by remember { mutableStateOf<List<User>>(emptyList()) }
    var searchNickname by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isRequestLoading by remember { mutableStateOf(false) }

    fun refresh() {
        scope.launch {
            friends = userRepository.getFriends()
            friendRequests = userRepository.getFriendRequests()
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Friends", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = {
                scope.launch {
                    userRepository.logout()
                    navController.navigate("auth") {
                        popUpTo(0)
                    }
                }
            }) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Add friend by nickname", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchNickname,
                        onValueChange = { searchNickname = it },
                        label = { Text("Nickname") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            isRequestLoading = true
                            errorMessage = null
                            scope.launch {
                                val result = userRepository.sendFriendRequest(searchNickname)
                                if (result.isSuccess) {
                                    errorMessage = "Request sent!"
                                    searchNickname = ""
                                    refresh()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.localizedMessage
                                }
                                isRequestLoading = false
                            }
                        },
                        enabled = !isRequestLoading && searchNickname.isNotBlank()
                    ) {
                        if (isRequestLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        else Icon(Icons.Default.PersonAdd, contentDescription = null)
                    }
                }
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (friendRequests.isNotEmpty()) {
            Text("Friend requests", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.heightIn(max = 180.dp)
            ) {
                items(friendRequests) { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(user.nickname, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.weight(1f))
                            Button(onClick = {
                                isLoading = true
                                scope.launch {
                                    userRepository.acceptFriendRequest(user.id)
                                    refresh()
                                    isLoading = false
                                }
                            }, enabled = !isLoading) {
                                Text("Accept")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedButton(onClick = {
                                isLoading = true
                                scope.launch {
                                    userRepository.rejectFriendRequest(user.id)
                                    refresh()
                                    isLoading = false
                                }
                            }, enabled = !isLoading) {
                                Text("Decline")
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text("Your friends", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(friends) { friend ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(friend.nickname, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("camera") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send photo")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { navController.navigate("history") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text("Photo history")
        }
    }
} 