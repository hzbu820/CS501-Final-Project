package com.cs501.pantrypal.screen.profilePage.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.cs501.pantrypal.viewmodel.UserIngredientsViewModel
import com.cs501.pantrypal.viewmodel.UserViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun photoPicker(
    filetype: String,
    fileName: String,
    userViewModel: UserViewModel,
    snackbarHostState: SnackbarHostState
): ManagedActivityResultLauncher<String, Uri?> {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    return (rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                val combinedFileName =
                    "${filetype}_${fileName}_${userViewModel.currentUser.value?.id}.jpg"
                val fileDir = context.filesDir
                val file = File(fileDir, combinedFileName)

                try {
                    context.contentResolver.openInputStream(it)?.use { inputStream ->
                        file.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    val currentUser = userViewModel.currentUser.value
                    if (currentUser != null) {
                        val updatedUser = currentUser.copy(
                            profileImageUrl = file.absolutePath
                        )
                        userViewModel.updateUserProfile(updatedUser)
                        snackbarHostState.showSnackbar("Profile image updated and saved")
                    } else {
                        snackbarHostState.showSnackbar("Failed to update profile: User not logged in")
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Failed to save image: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    })
}

@Composable
fun cameraPicker(
    snackbarHostState: SnackbarHostState, onImageCaptured: (Uri) -> Unit
): ManagedActivityResultLauncher<Uri, Boolean> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            try {
                val imageUri = createTempImageUri(context)
                imageUri?.let { uri ->
                    onImageCaptured(uri)
                }
            } catch (e: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Failed to process image: ${e.message}")
                }
            }
        }
    }
}

fun createTempImageUri(context: Context): Uri? {
    val tempFile = File.createTempFile(
        "barcode_", ".jpg", context.cacheDir
    ).apply {
        deleteOnExit()
    }
    return FileProvider.getUriForFile(
        context, "${context.packageName}.fileprovider", tempFile
    )
}

fun scanBarcode(
    image: InputImage,
    userIngredientsViewModel: UserIngredientsViewModel,
    onNameChange: (String) -> Unit,
    onImageChange: (String) -> Unit,
    onError: (String) -> Unit
) {
    val options = BarcodeScannerOptions.Builder().setBarcodeFormats(
        Barcode.FORMAT_EAN_13,
        Barcode.FORMAT_EAN_8,
        Barcode.FORMAT_UPC_A,
        Barcode.FORMAT_UPC_E,
        Barcode.FORMAT_CODE_128
    ).build()

    val scanner = BarcodeScanning.getClient(options)

    scanner.process(image).addOnSuccessListener { barcodes ->
        if (barcodes.isNotEmpty()) {
            val barcode = barcodes[0]
            barcode.rawValue?.let { value ->
                onNameChange("Loading product info...")

                MainScope().launch {
                    try {
                        userIngredientsViewModel.searchIngredientsByApi(value)
                        delay(500)
                        val result = userIngredientsViewModel.ingredientsByBarcode.value
                        if (result.name.isNotEmpty()) {
                            onNameChange(result.name)
                            onImageChange(result.image)
                        } else {
                            onNameChange("Product: $value")
                            onError("Product found but no details available")
                        }
                    } catch (e: Exception) {
                        onNameChange("Scanned: $value")
                        onError("Error loading product: ${e.message}")
                    }
                }
            }
        } else {
            onError("No barcode found")
        }
    }.addOnFailureListener { e ->
        onError("Failed to scan barcode: ${e.message}")
    }
}

@Composable
fun checkCameraPermission(
    snackbarHostState: SnackbarHostState
): Pair<Boolean, ManagedActivityResultLauncher<String, Boolean>> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val hasCameraPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Camera permission is required to scan barcodes")
            }
        }
    }

    return Pair(hasCameraPermission, cameraPermissionLauncher)
}