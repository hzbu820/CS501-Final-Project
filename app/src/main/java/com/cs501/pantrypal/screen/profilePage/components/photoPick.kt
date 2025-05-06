package com.cs501.pantrypal.screen.profilePage.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.cs501.pantrypal.viewmodel.UserIngredientsViewModel
import com.cs501.pantrypal.viewmodel.UserViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
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

fun createTempImageUri(context: Context): Uri {
    val tempFile = File(context.cacheDir, "barcode_temp.jpg")
    if (tempFile.exists()) {
        tempFile.delete()
    }
    tempFile.createNewFile()

    return FileProvider.getUriForFile(
        context, "${context.packageName}.fileprovider", tempFile
    )
}

fun scanBarcode(
    image: InputImage,
    userIngredientsViewModel: UserIngredientsViewModel,
    onNameChange: (String) -> Unit,
    onBarcodeChange: (String, String) -> Unit,
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

    onNameChange("Handling barcode scanning...")

    scanner.process(image).addOnSuccessListener { barcodes ->
        if (barcodes.isNotEmpty()) {
            val barcode = barcodes[0]
            barcode.rawValue?.let { value ->
                onNameChange("Loading Item's name: $value")

                MainScope().launch {
                    try {
                        userIngredientsViewModel.setEmptyBarcodeIngredient()

                        userIngredientsViewModel.searchIngredientsByApi(value)

                        try {
                            withTimeout(6000L) {
                                val result =
                                    userIngredientsViewModel.ingredientsByBarcode.filter { it.name.isNotEmpty() && it.userId.isNotEmpty() }
                                        .first()

                                onBarcodeChange(result.name, result.image)
                            }
                        } catch (e: TimeoutCancellationException) {
                            onError("Product lookup timed out. Try again.")
                        }
                    } catch (e: Exception) {
                        onError("Error loading product: ${e.message}")
                    }
                }
            }
        } else {
            onNameChange("")
            onError("Cannot find barcode")
        }
    }.addOnFailureListener { e ->
        onNameChange("")
        onError("Barcode scan error: ${e.message}")
    }
}

