package com.braffdev.steganofy.ui.common.file.saver

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.braffdev.steganofy.R
import com.braffdev.steganofy.databinding.FileSaverFragmentBinding
import org.koin.android.ext.android.inject

class FileSaverFragment : Fragment() {

    private lateinit var binding: FileSaverFragmentBinding
    private val viewModel: FileSaverViewModel by inject()
    lateinit var listener: FileSavedListener

    private val saveFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            val uri = result.data!!.data!!

            // Имя проверяем только здесь: диалог сохранения системный, помешать ему создать
            // файл с пустым именем нельзя. Убираем пустышку и остаёмся на экране, чтобы
            // пользователь нажал кнопку снова и ввёл нормальное имя.
            if (viewModel.isFileNameMissing(uri)) {
                viewModel.discardFile(uri)
                Toast.makeText(requireContext(), R.string.file_save_name_required, Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }

            viewModel.onFileUriChanged(uri)
            listener.onFileSaved(uri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FileSaverFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initialize(getTemporaryFileUri(), getMimeType(), getButtonTextRes())
        viewModel.liveDataIntentForResult.observe(this) { saveFileLauncher.launch(it) }
        viewModel.liveDataIntent.observe(this) { startActivity(it) }

        binding.buttonShareFile.setOnClickListener { viewModel.shareFileClicked() }
        binding.buttonSaveFile.setOnClickListener { viewModel.saveFileClicked() }

        binding.buttonSaveFile.setText(getButtonTextRes())
    }

    private fun getTemporaryFileUri(): Uri {
        return requireArguments().getParcelable(EXTRA_TEMPORARY_FILE_URI, Uri::class.java)!!
    }

    private fun getButtonTextRes(): Int {
        return requireArguments().getInt(EXTRA_TEXT_RES)
    }

    private fun getMimeType(): String {
        return requireArguments().getString(EXTRA_MIME_TYPE)!!
    }

    companion object {
        private const val EXTRA_TEMPORARY_FILE_URI = "tmpFileUri"
        private const val EXTRA_MIME_TYPE = "mimeType"
        private const val EXTRA_TEXT_RES = "textRes"

        fun newInstance(temporaryFileUri: Uri, @StringRes textRes: Int, mimeType: String): FileSaverFragment {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_TEMPORARY_FILE_URI, temporaryFileUri)
            bundle.putInt(EXTRA_TEXT_RES, textRes)
            bundle.putString(EXTRA_MIME_TYPE, mimeType)

            val fragment = FileSaverFragment()
            fragment.arguments = bundle

            return fragment
        }
    }
}
