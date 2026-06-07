package com.govautofill.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.govautofill.databinding.ActivityMediaVaultBinding
import com.govautofill.utils.AdManager
import com.govautofill.utils.ImageProcessor
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MediaVaultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaVaultBinding

    private var currentMode = ImageProcessor.ImageType.PHOTO
    private var pendingUri: Uri? = null
    private var cameraUri: Uri? = null
    private var currentWhitenLevel = 0

    // ── Permission launcher ──────────────────────────────────────────────────
    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) showPickerDialog()
        else Toast.makeText(this, "Permission দরকার!", Toast.LENGTH_SHORT).show()
    }

    // ── Gallery picker ───────────────────────────────────────────────────────
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { processImage(it) }
    }

    // ── Camera launcher ──────────────────────────────────────────────────────
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraUri?.let { processImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaVaultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
        setupSeekBar()
        loadExistingImages()

        // Banner ad at bottom
        AdManager.loadBanner(this, binding.adContainer)
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener { finish() }

        // Photo section
        binding.btnPickPhoto.setOnClickListener {
            currentMode = ImageProcessor.ImageType.PHOTO
            checkPermissionsAndPick()
        }

        // Signature section
        binding.btnPickSignature.setOnClickListener {
            currentMode = ImageProcessor.ImageType.SIGNATURE
            checkPermissionsAndPick()
        }

        // Re-process with new whiten level
        binding.btnApplyWhiten.setOnClickListener {
            val uri = pendingUri ?: return@setOnClickListener
            processImage(uri)
        }
    }

    private fun setupSeekBar() {
        binding.seekWhiten.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                currentWhitenLevel = progress
                binding.tvWhitenLevel.text = "Background হোয়াইটেনার: $progress%"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {
                // Auto-apply when slider stops
                pendingUri?.let { processImage(it) }
            }
        })
    }

    private fun checkPermissionsAndPick() {
        val perms = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) perms.add(Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) perms.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (perms.isEmpty()) showPickerDialog()
        else permLauncher.launch(perms.toTypedArray())
    }

    private fun showPickerDialog() {
        val label = if (currentMode == ImageProcessor.ImageType.PHOTO) "ছবি" else "সিগনেচার"
        AlertDialog.Builder(this)
            .setTitle("$label কোথা থেকে নেবেন?")
            .setItems(arrayOf("📷 ক্যামেরা", "🖼️ গ্যালারি")) { _, which ->
                if (which == 0) openCamera() else galleryLauncher.launch("image/*")
            }
            .show()
    }

    private fun openCamera() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFile = File(externalCacheDir, "IMG_$timestamp.jpg")
        cameraUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", imageFile)
        cameraLauncher.launch(cameraUri)
    }

    private fun processImage(uri: Uri) {
        pendingUri = uri
        binding.progressProcessing.visibility = View.VISIBLE
        binding.btnApplyWhiten.isEnabled = false

        Thread {
            try {
                val result = ImageProcessor.process(this, uri, currentMode, currentWhitenLevel)
                runOnUiThread {
                    binding.progressProcessing.visibility = View.GONE
                    binding.btnApplyWhiten.isEnabled = true

                    if (currentMode == ImageProcessor.ImageType.PHOTO) {
                        binding.ivPhotoPreview.setImageBitmap(result.bitmap)
                        binding.tvPhotoInfo.text =
                            "✅ ${result.width}×${result.height}px  •  ${result.sizeKb} KB\n${result.file.absolutePath}"
                        binding.cardPhotoPreview.visibility = View.VISIBLE
                    } else {
                        binding.ivSignaturePreview.setImageBitmap(result.bitmap)
                        binding.tvSignatureInfo.text =
                            "✅ ${result.width}×${result.height}px  •  ${result.sizeKb} KB\n${result.file.absolutePath}"
                        binding.cardSignaturePreview.visibility = View.VISIBLE
                    }

                    Toast.makeText(this, "✅ ${result.sizeKb} KB — সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.progressProcessing.visibility = View.GONE
                    binding.btnApplyWhiten.isEnabled = true
                    Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun loadExistingImages() {
        val dir = File(filesDir, "media_vault")

        val photoFile = File(dir, ImageProcessor.ImageType.PHOTO.fileName)
        if (photoFile.exists()) {
            android.graphics.BitmapFactory.decodeFile(photoFile.absolutePath)?.let {
                binding.ivPhotoPreview.setImageBitmap(it)
                val kb = (photoFile.length() / 1024).toInt()
                binding.tvPhotoInfo.text = "✅ 300×300px  •  $kb KB (আগের ছবি)"
                binding.cardPhotoPreview.visibility = View.VISIBLE
            }
        }

        val sigFile = File(dir, ImageProcessor.ImageType.SIGNATURE.fileName)
        if (sigFile.exists()) {
            android.graphics.BitmapFactory.decodeFile(sigFile.absolutePath)?.let {
                binding.ivSignaturePreview.setImageBitmap(it)
                val kb = (sigFile.length() / 1024).toInt()
                binding.tvSignatureInfo.text = "✅ 300×80px  •  $kb KB (আগের সিগনেচার)"
                binding.cardSignaturePreview.visibility = View.VISIBLE
            }
        }
    }
}
