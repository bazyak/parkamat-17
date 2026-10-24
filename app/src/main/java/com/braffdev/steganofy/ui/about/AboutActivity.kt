package com.braffdev.steganofy.ui.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.braffdev.steganofy.R
import com.braffdev.steganofy.databinding.AboutActivityBinding
import com.braffdev.steganofy.ui.common.applyWindowInsetsAsPadding
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.core.scope.Scope

class AboutActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope: Scope by activityScope()
    private lateinit var binding: AboutActivityBinding
    private val viewModel: AboutViewModel by scope.inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AboutActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applyWindowInsetsAsPadding()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_close_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.about_title)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        viewModel.initialize()
        viewModel.liveEventStartActivity.observe(this) { startActivity(it) }
        viewModel.liveDataAppInfo.observe(this) { binding.textViewAboutVersion.text = it }

        binding.buttonAboutEmail.setOnClickListener { viewModel.emailClicked() }
        binding.buttonAboutGitHub.setOnClickListener { viewModel.gitHubClicked() }
        binding.textViewAboutLicenses.setOnClickListener { viewModel.licensesClicked() }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}