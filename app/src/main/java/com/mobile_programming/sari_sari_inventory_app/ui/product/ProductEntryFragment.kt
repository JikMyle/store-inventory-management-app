package com.mobile_programming.sari_sari_inventory_app.ui.product

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.setPadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.mobile_programming.sari_sari_inventory_app.MainActivity
import com.mobile_programming.sari_sari_inventory_app.R
import com.mobile_programming.sari_sari_inventory_app.databinding.FragmentProductEntryBinding
import com.mobile_programming.sari_sari_inventory_app.ui.AppViewModelProvider
import com.mobile_programming.sari_sari_inventory_app.utils.TextInputErrorType
import com.mobile_programming.sari_sari_inventory_app.utils.productImageDir
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Calendar

class ProductEntryFragment : Fragment() {
    private lateinit var binding: FragmentProductEntryBinding
    private lateinit var navController: NavController
    private val viewModel: ProductEntryViewModel by viewModels { AppViewModelProvider.Factory }

    // There are two temp image values since one is for the camera
    // In case the camera does not return with an image
    private var tempImage: File? = null
    private lateinit var tempCameraImage: File
    private val tempImageName = "tempImage"
    private val tempImageExtension = ".jpg"

    private val cameraRequestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            val context = requireContext()

            if(isGranted) {
                launchCamera(context)
            } else {
                context.showPermissionRequiredToast(
                    getString(R.string.camera)
                )
            }
        }

    private val cameraLauncher =
        registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                tempImage?.delete()
                tempImage = tempCameraImage

                tempImage?.let { file ->
                    setImage(requireContext().getFileUri(file))
                }
            } else {
                tempCameraImage.delete()
            }
        }

    // This launcher check for the current device SDK
    // and calls the appropriate API to get media from
    private val pickImagePermissionRequestLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if(isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickImagesLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                } else {
                    getContentLauncher.launch("image/*")
                }
            } else {
                context?.showPermissionRequiredToast(
                    getString(R.string.gallery)
                )
            }
        }

    private val pickImagesLauncher =
        registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri : Uri? ->
            uri?.let {
                createTempImage(it)
            }
        }

    private val getContentLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                createTempImage(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()

        binding = FragmentProductEntryBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        navController = (activity as MainActivity).findNavController(R.id.main_nav_host_fragment)

        viewModel.uiState.asLiveData().observe(
            viewLifecycleOwner
        ) { newState ->
            val errorMap = newState.detailsErrorMap

            binding.productNumberField.apply {
                val fieldError = errorMap[ProductEntryViewModel.PRODUCT_NUMBER_ERROR_KEY]

                isErrorEnabled = fieldError != null
                error = fieldError?.let {
                    if (it == TextInputErrorType.DuplicateFound) {
                        getString(
                            it.stringResourceId,
                            ProductEntryViewModel.PRODUCT_NUMBER_ERROR_KEY
                        )
                    } else {
                        getString(it.stringResourceId)
                    }
                }
            }

            checkIfTextInputBlank(
                textField = binding.productNameField,
                errorKey = ProductEntryViewModel.PRODUCT_NAME_ERROR_KEY
            )

            checkIfTextInputBlank(
                textField = binding.productPriceField,
                errorKey = ProductEntryViewModel.PRODUCT_PRICE_ERROR_KEY
            )

            checkIfTextInputBlank(
                textField = binding.productStockField,
                errorKey = ProductEntryViewModel.PRODUCT_STOCK_ERROR_KEY
            )

            viewModel.uiState.value.productDetails.imageUri.let { uri ->
                if (uri == null) {
                    binding.productImage.setImageResource(R.drawable.baseline_camera_alt_24)
                    binding.productImage.setPadding(
                        resources.getDimension(R.dimen.padding_extra_large).toInt()
                    )
                } else {
                    binding.productImage.setImageURI(uri)
                    binding.productImage.setPadding(0)
                }
            }
        }

        binding.productNumberField.editText?.doAfterTextChanged {
            viewModel.updateUiState(
                viewModel.uiState.value.productDetails.copy(
                    productNumber = it.toString()
                )
            )
        }

        binding.productNameField.editText?.doAfterTextChanged {
            viewModel.updateUiState(
                viewModel.uiState.value.productDetails.copy(
                    productName = it.toString()
                )
            )
        }

        binding.productPriceField.editText?.doAfterTextChanged {
            viewModel.updateUiState(
                viewModel.uiState.value.productDetails.copy(
                    price = it.toString()
                )
            )
        }

        binding.productStockField.editText?.doAfterTextChanged {
            viewModel.updateUiState(
                viewModel.uiState.value.productDetails.copy(
                    stock = it.toString()
                )
            )
        }

        binding.addProductButton.setOnClickListener {
            lifecycleScope.launch {
                tempImage?.let { file ->
                    val newImageFileName =
                        "${Calendar.getInstance().timeInMillis}.jpeg"

                    val newImageFile = context.copyFileToInternalStorage(
                        oldFile = file,
                        newFileName = newImageFileName,
                        child = productImageDir
                    )

                    val newImageUri = context.getFileUri(newImageFile)

                    viewModel.updateUiState(
                        productDetails = viewModel.uiState.value.productDetails.copy(
                            imageUri = newImageUri
                        )
                    )
                }

                viewModel.insertProduct()
                Toast.makeText(
                    context,
                    getString(R.string.new_product_added),
                    Toast.LENGTH_SHORT
                ).show()

                navController.popBackStack()
            }
        }

        binding.addPhotoButton.setOnClickListener {
            showImageSourceSelector()
        }

        binding.removePhotoButton.setOnClickListener {
            clearImage()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tempImage?.delete()
    }

    private fun clearImage() {
        viewModel.updateUiState(
            viewModel.uiState.value.productDetails.copy(
                imageUri = null
            )
        )

        tempImage?.delete()
        tempImage = null
    }

    private fun setImage(uri: Uri) {
        viewModel.updateUiState(
            viewModel.uiState.value.productDetails.copy(
                imageUri = uri
            )
        )
    }

    private fun createTempImage(uri: Uri) {
        tempImage?.delete()
        tempImage = null

        tempImage = requireContext().storeTempImageInCache(
            uri = uri,
            prefix = tempImageName,
            suffix = tempImageExtension
        )

        tempImage?.let { file ->
            setImage(requireContext().getFileUri(file))
        }
    }

    private fun checkIfTextInputBlank(
        textField: TextInputLayout,
        errorKey: String
    ) {
        val errorMap = viewModel.uiState.value.detailsErrorMap

        textField.apply {
            val fieldError = errorMap[errorKey]

            isErrorEnabled = fieldError != null
            error = fieldError?.let { getString(it.stringResourceId) }
        }
    }

    private fun showImageSourceSelector() {
        val context = requireContext()
        val options = arrayOf(
            getString(R.string.camera),
            getString(R.string.gallery)
        )

        AlertDialog.Builder(context)
            .setTitle("Choose Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        cameraRequestPermissionLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    }

                    1 -> {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            // For API 34, READ_MEDIA_VISUAL_USER_SELECTED is used
                            // This feature is fairly new and provides partial access to media files
                            pickImagePermissionRequestLauncher.launch(
                                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                            )
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            // For API 33, READ_MEDIA_IMAGES is used
                            // This feature is an updated version of the getContent API
                            pickImagePermissionRequestLauncher.launch(
                                Manifest.permission.READ_MEDIA_IMAGES
                            )
                        } else {
                            // For API 32 and below, READ_EXTERNAL_STORAGE is used
                            pickImagePermissionRequestLauncher.launch(
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun launchCamera(
        context: Context
    ) {
        tempCameraImage = File.createTempFile(
            tempImageName,
            tempImageExtension
        ).apply { deleteOnExit() }

        cameraLauncher.launch(context.getFileUri(tempCameraImage))
    }
}

fun Context.storeTempImageInCache(
    uri: Uri,
    prefix: String,
    suffix: String,
): File {
    val inputStream = this.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream)

    val file = File.createTempFile(prefix, suffix).apply {
        deleteOnExit()
    }

    val outputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
    outputStream.close()

    return file
}

fun Context.getFileUri(
    file: File
): Uri {
    return FileProvider.getUriForFile(
        this,
        "${this.packageName}.provider",
        file
    )
}

fun Context.copyFileToInternalStorage(
    oldFile: File,
    newFileName: String,
    child: String
): File {
    val internalDir = File(this.filesDir.toString() + child)

    if (!internalDir.exists()) {
        internalDir.mkdir()
    }

    val newFile = File(internalDir, newFileName)

    try {
        val inputStream = FileInputStream(oldFile)
        val outputStream = FileOutputStream(newFile)
        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        inputStream.close()
        outputStream.flush()
        outputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return newFile
}

fun Context.showPermissionRequiredToast(permissionName: String) {
    Toast.makeText(
        this,
        this.getString(R.string.permission_required_toast, permissionName),
        Toast.LENGTH_SHORT
    ).show()
}