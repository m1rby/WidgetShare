package com.example.widgetshare.ui.screens.camera

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.widgetshare.data.remote.UserDto
import com.example.widgetshare.data.repository.PhotoRepository
import com.example.widgetshare.data.repository.UserRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository(context) }
    val photoRepository = remember { PhotoRepository(context) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var uploadUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var friends by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var selectedFriends by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var isSending by remember { mutableStateOf(false) }
    var sendSuccess by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
        uploadUrl = null
        errorMessage = null
        sendSuccess = false
        selectedFriends = emptySet()
    }

    LaunchedEffect(Unit) {
        scope.launch { friends = userRepository.getFriends() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Send a photo", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { pickImageLauncher.launch("image/*") }) {
            Text("Pick from gallery")
        }
        photoUri?.let { uri ->
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (uploadUrl == null) {
                Button(
                    onClick = {
                        scope.launch {
                            isUploading = true
                            errorMessage = null
                            try {
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val fileBytes = inputStream?.readBytes() ?: throw Exception("Failed to read file")
                                val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), fileBytes)
                                val body = MultipartBody.Part.createFormData("file", "photo.jpg", requestFile)
                                uploadUrl = photoRepository.uploadPhoto(body)
                            } catch (e: Exception) {
                                errorMessage = e.localizedMessage
                            } finally {
                                isUploading = false
                            }
                        }
                    },
                    enabled = !isUploading
                ) {
                    if (isUploading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Text("Upload to server")
                }
            }
        }
        uploadUrl?.let { url ->
            Spacer(modifier = Modifier.height(16.dp))
            Text("Select friends to send:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                items(friends) { friend ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = selectedFriends.contains(friend.id),
                            onCheckedChange = { checked ->
                                selectedFriends = if (checked) selectedFriends + friend.id else selectedFriends - friend.id
                            }
                        )
                        Text(friend.nickname, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        isSending = true
                        errorMessage = null
                        try {
                            photoRepository.sendPhotoToFriends(url, selectedFriends.toList())
                            sendSuccess = true
                        } catch (e: Exception) {
                            errorMessage = e.localizedMessage
                        } finally {
                            isSending = false
                        }
                    }
                },
                enabled = selectedFriends.isNotEmpty() && !isSending && !sendSuccess
            ) {
                if (isSending) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Send photo")
            }
            if (sendSuccess) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Photo sent!", color = MaterialTheme.colorScheme.primary)
            }
        }
        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
} 