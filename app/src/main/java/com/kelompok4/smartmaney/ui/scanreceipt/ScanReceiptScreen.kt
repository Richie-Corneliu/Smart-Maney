package com.kelompok4.smartmaney.ui.scanreceipt

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.kelompok4.smartmaney.ui.theme.SmartManeyTheme
import com.kelompok4.smartmaney.viewmodel.ScanReceiptUiState
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanReceiptScreen(
    onBackClick: () -> Unit,
    uiState: ScanReceiptUiState,
    onImageCaptured: (ByteArray) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigationConsumed: () -> Unit,
    onDismissError: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCameraPermission = isGranted }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(uiState.isProcessing) {
        if (uiState.isProcessing) {
            cameraProvider?.unbindAll()
            cameraControl = null
            imageCaptureUseCase = null
            isTorchOn = false
        } else {
            val provider = cameraProvider ?: return@LaunchedEffect
            val pv = previewViewRef ?: return@LaunchedEffect

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = pv.surfaceProvider
            }
            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                provider.unbindAll()
                val camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
                cameraControl = camera.cameraControl
                imageCaptureUseCase = imageCapture
            } catch (_: Exception) {}
        }
    }

    // Launcher untuk memanggil galeri bawaan sistem Android
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    val bytes = readBytesFromUri(context, uri)
                    if (bytes == null) {
                        Toast.makeText(context, "Gagal membaca foto", Toast.LENGTH_SHORT).show()
                    } else {
                        onImageCaptured(bytes)
                    }
                }
            } else {
                Toast.makeText(context, "Batal memilih foto", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(uiState.navigateToTransactionId) {
        val transactionId = uiState.navigateToTransactionId
        if (transactionId != null) {
            onNavigateToDetail(transactionId)
            onNavigationConsumed()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Scan Receipt", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    // 3. LOGIKA TOMBOL BACK DIHUBUNGKAN KE PARAMETER
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        cameraControl?.let { control ->
                            isTorchOn = !isTorchOn
                            control.enableTorch(isTorchOn)
                        }
                    }) {
                        Icon(
                            if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (isTorchOn) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                        previewViewRef = previewView

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val provider = cameraProviderFuture.get()
                            cameraProvider = provider

                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val imageCapture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCaptureUseCase = imageCapture

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                provider.unbindAll()
                                val camera = provider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                                cameraControl = camera.cameraControl
                            } catch (_: Exception) {
                                // ignore bind errors
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    }
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 140.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = "Position the receipt inside the frame", color = Color.White, fontSize = 12.sp)
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding() // INI KUNCINYA: Mendorong isi ke atas menghindari tombol sistem
                    .padding(vertical = 24.dp, horizontal = 32.dp), // Padding internal tombol
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("IMG", fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("GALLERY", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // 5. TOMBOL SHUTTER UTAMA DIHUBUNGKAN KE FUNGSI JEPRET
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable {
                            takePhoto(context, imageCaptureUseCase) { photoFile ->
                                scope.launch {
                                    val bytes = readBytesFromFile(photoFile)
                                    if (bytes == null) {
                                        Toast.makeText(context, "Gagal membaca foto", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onImageCaptured(bytes)
                                    }
                                }
                            }
                        }   ,
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Capture", tint = Color.White, modifier = Modifier.size(32.dp))
                }

                // Tombol Auto
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("A", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("AUTO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (uiState.isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "Analyzing receipt...",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            uiState.errorMessage?.let { message ->
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = onDismissError,
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = onDismissError) {
                            Text("Dismiss")
                        }
                    },
                    title = { Text("Error") },
                    text = { Text(message) }
                )
            }
        }
    }
}

// 6. FUNGSI EKSEKUSI JEPRET FOTO
// Tambahkan parameter onPhotoSaved di fungsinya
private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    onPhotoSaved: (File) -> Unit
) {
    val capture = imageCapture ?: return

    val photoFile = File(context.cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    capture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Toast.makeText(context, "Gagal mengambil foto: ${exc.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Toast.makeText(context, "Foto berhasil disimpan!", Toast.LENGTH_SHORT).show()
                onPhotoSaved(photoFile)
            }
        }
    )
}

private fun readBytesFromUri(context: Context, uri: android.net.Uri): ByteArray? {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val original = input.readBytes()
            compressImageBytes(original)
        }
    }.getOrNull()
}

private fun readBytesFromFile(file: File): ByteArray? {
    return runCatching { compressImageBytes(file.readBytes()) }.getOrNull()
}

private fun compressImageBytes(source: ByteArray): ByteArray? {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(source, 0, source.size, options)
    if (options.outWidth <= 0 || options.outHeight <= 0) return null

    val maxDimension = 1280
    val sampleSize = calculateInSampleSize(options.outWidth, options.outHeight, maxDimension)
    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val bitmap = BitmapFactory.decodeByteArray(source, 0, source.size, decodeOptions) ?: return null

    val output = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
    bitmap.recycle()
    return output.toByteArray()
}

private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
    var inSampleSize = 1
    while (width / (inSampleSize * 2) >= maxDimension && height / (inSampleSize * 2) >= maxDimension) {
        inSampleSize *= 2
    }
    return inSampleSize
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun ScanReceiptScreenPreview() {
    SmartManeyTheme {
        ScanReceiptScreen(
            onBackClick = {},
            uiState = ScanReceiptUiState(isProcessing = true),
            onImageCaptured = {},
            onNavigateToDetail = {},
            onNavigationConsumed = {},
            onDismissError = {}
        )
    }
}