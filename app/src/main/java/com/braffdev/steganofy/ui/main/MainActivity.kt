package com.braffdev.steganofy.ui.main

import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.braffdev.steganofy.databinding.MainActivityBinding
import com.braffdev.steganofy.ui.common.applyWindowInsetsAsPadding
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.core.scope.Scope

class MainActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope: Scope by activityScope()
    private lateinit var binding: MainActivityBinding
    private val viewModel: MainViewModel by scope.inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applyWindowInsetsAsPadding()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        viewModel.liveEventStartActivity.observe(this) { startActivity(it) }
        viewModel.liveEventStartAnimation.observe(this) { startAnimation() }
        viewModel.liveEventStopAnimation.observe(this) { stopAnimation() }

        binding.buttonHide.setOnClickListener { viewModel.hideClicked() }
        binding.buttonReveal.setOnClickListener { viewModel.revealClicked() }
        binding.buttonInfo.setOnClickListener { viewModel.aboutClicked() }
        binding.viewAnimationForeground.setOnClickListener { viewModel.aboutClicked() }

        val animatedVectorDrawable = binding.viewAnimationForeground.drawable as AnimatedVectorDrawable
        animatedVectorDrawable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                viewModel.animationEnded()
            }
        })
    }

    override fun onResume() {
        viewModel.resume()
        super.onResume()
    }

    private fun startAnimation() {
        (binding.viewAnimationForeground.drawable as AnimatedVectorDrawable).start()
        (binding.viewAnimationBackground.drawable as AnimatedVectorDrawable).start()
    }

    private fun stopAnimation() {
        (binding.viewAnimationForeground.drawable as AnimatedVectorDrawable).stop()
        (binding.viewAnimationForeground.drawable as AnimatedVectorDrawable).reset()
        (binding.viewAnimationBackground.drawable as AnimatedVectorDrawable).stop()
        (binding.viewAnimationBackground.drawable as AnimatedVectorDrawable).reset()
    }
}