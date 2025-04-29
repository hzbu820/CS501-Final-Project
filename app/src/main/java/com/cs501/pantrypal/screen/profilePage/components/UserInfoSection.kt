package com.cs501.pantrypal.screen.profilePage.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cs501.pantrypal.R
import com.cs501.pantrypal.data.database.User
import com.cs501.pantrypal.ui.theme.BackgroundLight
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.ui.theme.Typography
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserInfoSection(
    user: User,
    profileImageUri: Uri?,
    onEditClick: () -> Unit,
    onImageClick: () -> Unit,
    onSyncClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(BackgroundLight)
                .clickable(onClick = onImageClick),
            contentAlignment = Alignment.Center
        ) {
            when {
                profileImageUri != null -> {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                user.profileImageUrl.isNotBlank() -> {
                    val imageFile = File(user.profileImageUrl)
                    if (imageFile.exists()) {
                        AsyncImage(
                            model = imageFile,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.default_profile_image),
                            contentDescription = "Default Profile Image",
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
                else -> {
                    Image(
                        painter = painterResource(id = R.drawable.default_profile_image),
                        contentDescription = "Default Profile Image",
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }

        // User Info
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = user.username,
                style = Typography.headlineLarge
            )

            Text(
                text = if (user.email.isNotBlank()) user.email else "No email provided",
                style = Typography.bodyMedium
            )

            Text(
                text = "Join PantryPal Since: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(user.createdAt))}",
                style = Typography.bodySmall,
            )
        }

        // Edit Button
        IconButton(onClick = onEditClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_edit),
                contentDescription = "Edit Profile",
                tint = InfoColor
            )
        }

        // Sync Button
        IconButton(onClick = { onSyncClick() }) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Sync to Cloud",
                tint = InfoColor
            )
        }
    }
}