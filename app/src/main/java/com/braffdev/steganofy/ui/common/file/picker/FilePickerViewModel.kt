package com.braffdev.steganofy.ui.common.file.picker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import com.braffdev.steganofy.service.ExecutionService
import com.braffdev.steganofy.service.FileService

class FilePickerViewModel(val applicationContext: Context, val fileService: FileService, val executionService: ExecutionService) {

    val liveDataIntent = MutableLiveData<Intent>()
    val liveDataFileName = MutableLiveData<String>()
    val liveDataFileInfo = MutableLiveData<String>()

    lateinit var mimeType: String
    var pickerTextRes: Int = -1

    fun initialize(mimeType: String, @StringRes pickerTextRes: Int) {
        this.mimeType = mimeType
        this.pickerTextRes = pickerTextRes
    }

    fun selectFileClicked() {
        if (mimeType.startsWith("image")) {
            // Photo Picker всегда стартует в галерее и не помнит последний путь. Запускаем
            // его напрямую: это системный компонент, и обёртка в chooser может увести выбор
            // в стороннюю галерею, которая отдаёт файл из собственного кэша.
            liveDataIntent.value = Intent(MediaStore.ACTION_PICK_IMAGES)
            return
        }

        // Файлы произвольного типа Photo Picker не показывает, их берём через SAF.
        val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
        }

        liveDataIntent.value = Intent.createChooser(pickIntent, applicationContext.getString(pickerTextRes))
    }

    fun onFileUriChanged(uri: Uri) {
        executionService.executeInBackground {
            liveDataFileName.postValue(fileService.getFileName(uri))
            liveDataFileInfo.postValue(fileService.formatFileInfo(uri))
        }
    }
}
