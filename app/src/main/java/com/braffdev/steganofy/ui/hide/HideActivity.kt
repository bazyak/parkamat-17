package com.braffdev.steganofy.ui.hide

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.braffdev.steganofy.R
import com.braffdev.steganofy.databinding.HideActivityBinding
import com.braffdev.steganofy.ui.common.applyWindowInsetsAsPadding
import com.braffdev.steganofy.ui.common.OperationStatus
import com.braffdev.steganofy.ui.common.progress.ProgressErrorFragment
import com.braffdev.steganofy.ui.common.progress.ProgressRunningFragment
import com.braffdev.steganofy.ui.hide.success.HideSuccessFragment
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.core.scope.Scope

class HideActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope: Scope by activityScope()
    private lateinit var binding: HideActivityBinding
    private val viewModel: HideViewModel by scope.inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HideActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applyWindowInsetsAsPadding()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        viewModel.liveDataStatus.observe(this) { setStatus(it) }
        viewModel.liveEventCancelWarning.observe(this) { showCancelWarningDialog() }
        viewModel.initialize()
    }

    private fun showCancelWarningDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.cancel)
            .setMessage(R.string.cancel_confirmation)
            .setPositiveButton(R.string.yes) { _, _ -> finish() }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()
    }

    override fun finish() {
        viewModel.finish()
        super.finish()
    }

    override fun onBackPressed() {
        viewModel.backPressed()
    }

    private fun setStatus(status: OperationStatus) {
        when (status) {
            OperationStatus.RUNNING ->
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ProgressRunningFragment.newInstance()).commit()

            OperationStatus.SUCCESS ->
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, HideSuccessFragment.newInstance()).commit()

            OperationStatus.ERROR ->
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ProgressErrorFragment.newInstance(R.string.hide_failed)).commit()

            OperationStatus.IMAGE_TOO_SIMPLE ->
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ProgressErrorFragment.newInstance(R.string.hide_image_too_simple_content)).commit()
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, HideActivity::class.java)
        }
    }
}
