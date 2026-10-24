package com.braffdev.steganofy.ui.common.file.picker

import android.Manifest.permission.READ_MEDIA_IMAGES
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.braffdev.steganofy.R
import com.braffdev.steganofy.databinding.FilePickerFragmentBinding
import org.koin.android.ext.android.inject

class FilePickerFragment : Fragment() {

    private lateinit var binding: FilePickerFragmentBinding
    private val viewModel: FilePickerViewModel by inject()
    lateinit var listener: FilePickedListener

    private val filePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            val uri = result.data!!.data!!
            viewModel.onFileUriChanged(uri)
            listener.onFileChanged(uri)
        }
    }

    // Отказ не блокирует выбор: без разрешения файл будет прочитан так, как его отдаёт
    // Photo Picker, то есть ровно как до этой правки.
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.selectFileClicked()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FilePickerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initialize(getMimeType(), getPickerTextRes())
        viewModel.liveDataIntent.observe(this) { filePicker.launch(it) }
        viewModel.liveDataFileName.observe(this) { binding.textViewFileName.text = it }
        viewModel.liveDataFileInfo.observe(this) {
            binding.textViewFileInfo.visibility = View.VISIBLE
            binding.textViewFileInfo.text = it
        }

        binding.selectFile.setOnClickListener {
            if (needsMediaImagesPermission()) {
                requestPermissionLauncher.launch(READ_MEDIA_IMAGES)
            } else {
                viewModel.selectFileClicked()
            }
        }
        binding.selectFile.setText(getPickerTextRes())
        binding.textViewFileName.setText(getNothingSelectedTextRes())
    }

    private fun getMimeType(): String {
        return requireArguments().getString(EXTRA_MIME_TYPE)!!
    }

    private fun getPickerTextRes(): Int {
        return requireArguments().getInt(EXTRA_PICKER_TEXT_RES)
    }

    private fun getNothingSelectedTextRes(): Int {
        return requireArguments().getInt(EXTRA_NOTHING_SELECTED_TEXT_RES)
    }

    /**
     * Доступ к медиатеке нужен, чтобы прочитать выбранную картинку напрямую из MediaStore,
     * минуя базу Photo Picker с её устаревшим содержимым.
     */
    private fun needsMediaImagesPermission(): Boolean {
        if (!getMimeType().startsWith("image")) {
            return false
        }

        return ContextCompat.checkSelfPermission(requireContext(), READ_MEDIA_IMAGES) !=
            PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val EXTRA_MIME_TYPE = "mimeType"
        private const val EXTRA_PICKER_TEXT_RES = "pickerTextRes"
        private const val EXTRA_NOTHING_SELECTED_TEXT_RES = "nothingSelectedTextRes"

        fun newImageInstance(): FilePickerFragment {
            return newInstance("image/*", R.string.file_select_image, R.string.file_select_image_none)
        }

        fun newFileInstance(): FilePickerFragment {
            return newInstance("*/*", R.string.file_select, R.string.file_select_none)
        }

        private fun newInstance(mimeType: String, @StringRes pickerTextRes: Int, @StringRes nothingSelectedTextRes: Int): FilePickerFragment {
            val bundle = Bundle()
            bundle.putString(EXTRA_MIME_TYPE, mimeType)
            bundle.putInt(EXTRA_PICKER_TEXT_RES, pickerTextRes)
            bundle.putInt(EXTRA_NOTHING_SELECTED_TEXT_RES, nothingSelectedTextRes)

            val fragment = FilePickerFragment()
            fragment.arguments = bundle

            return fragment
        }
    }
}
