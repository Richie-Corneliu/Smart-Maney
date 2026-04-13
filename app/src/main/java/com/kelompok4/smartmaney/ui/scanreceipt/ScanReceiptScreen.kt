package com.kelompok4.smartmaney.ui.scanreceipt

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import com.kelompok4.smartmaney.ui.theme.SmPrimary
import com.kelompok4.smartmaney.ui.theme.SmSurfaceMuted
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanReceiptScreen(
    onBackClick: () -> Unit,
    onPhotoSaved: () -> Unit// 1. PARAMETER BARU UNTUK TOMBOL BACK
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

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

    // 2. STATE BARU UNTUK MESIN PENJEPRET FOTO
    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }

    // State untuk menyimpan alamat foto yang dipilih dari galeri
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher untuk memanggil galeri bawaan sistem Android
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
                // Karena lu menunda UI untuk menampilkan foto, kita pakai Toast dulu sebagai bukti datanya masuk
                Toast.makeText(context, "Foto dipilih: $uri", Toast.LENGTH_SHORT).show()
                onPhotoSaved()
            } else {
                Toast.makeText(context, "Batal memilih foto", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Scan Receipt", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                ),
                navigationIcon = {
                    // 3. LOGIKA TOMBOL BACK DIHUBUNGKAN KE PARAMETER
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
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
                            Icons.Default.FlashOn,
                            contentDescription = "Flash",
                            tint = if (isTorchOn) SmPrimary else Color.White
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
                .background(Color.Black)
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

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            // 4. KONFIGURASI IMAGE CAPTURE
                            val imageCapture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCaptureUseCase = imageCapture // Simpan ke state agar bisa diakses tombol

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture // Wajib dimasukkan ke dalam binding
                                )
                                cameraControl = camera.cameraControl
                            } catch (exc: Exception) {
                                // Tangani error
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
                    .background(Color.White) // Background putih tetap sampai bawah layar
                    .navigationBarsPadding() // INI KUNCINYA: Mendorong isi ke atas menghindari tombol sistem
                    .padding(vertical = 24.dp, horizontal = 32.dp), // Padding internal tombol
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
// Tombol Gallery
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SmSurfaceMuted)
                            // INI KUNCINYA: Memanggil Photo Picker saat ditekan
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
                        .background(SmPrimary)
                        .clickable {
                            takePhoto(context, imageCaptureUseCase) {
                                onPhotoSaved() // Teruskan jembatannya ke fungsi
                            }
                        }   ,
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Capture", tint = Color.White, modifier = Modifier.size(32.dp))
                }

                // Tombol Auto
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(SmSurfaceMuted),
                        contentAlignment = Alignment.Center
                    ) { Text("A", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("AUTO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 6. FUNGSI EKSEKUSI JEPRET FOTO
// Tambahkan parameter onPhotoSaved di fungsinya
private fun takePhoto(context: Context, imageCapture: ImageCapture?, onPhotoSaved: () -> Unit) {
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

                // EKSEKUSI JEMBATAN: Pindah halaman setelah kamera sukses menjepret
                onPhotoSaved()
            }
        }
    )
}