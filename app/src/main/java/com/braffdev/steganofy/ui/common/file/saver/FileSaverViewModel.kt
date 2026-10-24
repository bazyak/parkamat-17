package com.braffdev.steganofy.ui.common.file.saver

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import com.braffdev.steganofy.R
import com.braffdev.steganofy.service.ExecutionService
import com.braffdev.steganofy.service.FileService
import org.apache.commons.io.IOUtils

class FileSaverViewModel(val applicationContext: Context, val fileService: FileService, val executionService: ExecutionService) {

    private companion object {
        /** Папка Pictures во внутренней памяти, в терминах системного провайдера документов. */
        private val PICTURES_URI: Uri = DocumentsContract.buildDocumentUri(
            "com.android.externalstorage.documents",
            "primary:Pictures"
        )
    }

    val liveDataIntentForResult = MutableLiveData<Intent>()
    val liveDataIntent = MutableLiveData<Intent>()

    lateinit var temporaryFileUri: Uri
    lateinit var mimeType: String
    var buttonTextRes: Int = -1

    fun initialize(temporaryFileUri: Uri, mimeType: String, @StringRes buttonTextRes: Int) {
        this.temporaryFileUri = temporaryFileUri
        this.mimeType = mimeType
        this.buttonTextRes = buttonTextRes
    }

    fun saveFileClicked() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = mimeType
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.putExtra(Intent.EXTRA_TITLE, "output." + fileService.getFileExtension(mimeType))

        // Диалог сохранения сам по себе открывается там, где его закрыли в прошлый раз.
        // Просим начинать с Pictures, рядом с исходными картинками.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, PICTURES_URI)

        // Без обёртки в chooser: диалог сохранения в системе один, а лишний слой может
        // не донести до него дополнительные параметры интента.
        liveDataIntentForResult.value = intent
    }

    fun shareFileClicked() {
        executionService.executeInBackground {
            val intentShareFile = Intent(Intent.ACTION_SEND)
            intentShareFile.type = mimeType
            intentShareFile.putExtra(Intent.EXTRA_STREAM, temporaryFileUri)
            val intentChooser = Intent.createChooser(intentShareFile, applicationContext.getString(R.string.file_share))

            // Grant permissions to URI
            val resInfoList = applicationContext.packageManager.queryIntentActivities(
                intentChooser,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
            val permissions = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION

            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                applicationContext.grantUriPermission(packageName, temporaryFileUri, permissions)
            }

            // Send intent
            liveDataIntent.postValue(intentChooser)
        }
    }

    /**
     * Проверяет, ввёл ли пользователь имя файла. Системный диалог сохранения пустое поле
     * не запрещает: он создаёт файл с именем-заглушкой в скобках, вроде "(invalid).png".
     *
     * @param uri файл, созданный диалогом сохранения
     * @return true, если имя осталось незаполненным
     */
    fun isFileNameMissing(uri: Uri): Boolean {
        val name = try {
            fileService.getFileName(uri)
        } catch (e: Exception) {
            Log.w("FileSaverViewModel", "Can not read the file name of $uri", e)
            return false
        }

        val nameWithoutExtension = name.substringBeforeLast('.', name).trim()
        return nameWithoutExtension.isEmpty() ||
            (nameWithoutExtension.startsWith("(") && nameWithoutExtension.endsWith(")"))
    }

    /**
     * Удаляет файл, который диалог сохранения успел создать до того, как мы проверили имя.
     *
     * @param uri файл для удаления
     */
    fun discardFile(uri: Uri) {
        executionService.executeInBackground {
            try {
                DocumentsContract.deleteDocument(applicationContext.contentResolver, uri)
            } catch (e: Exception) {
                Log.w("FileSaverViewModel", "Can not delete $uri", e)
            }
        }
    }

    fun onFileUriChanged(uri: Uri) {
        writeFile(uri)
    }

    private fun writeFile(uri: Uri) {
        executionService.executeInBackground {
            val contentResolver = applicationContext.contentResolver
            val inputStream = contentResolver.openInputStream(temporaryFileUri)
            val outputStream = contentResolver.openOutputStream(uri)

            inputStream.use { outputStream.use { IOUtils.copy(inputStream, outputStream) } }
        }
    }
}
