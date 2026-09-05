package com.braffdev.steganofy.ui.setup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.braffdev.steganofy.R
import com.braffdev.steganofy.databinding.ActivityEasterEggSetupBinding
import com.braffdev.steganofy.ui.converter.ConversionEngine
import com.braffdev.steganofy.ui.converter.UnitConverterActivity

class EasterEggSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEasterEggSetupBinding

    private val categories = ConversionEngine.categories
    private var selectedCategoryIndex = 0
    private var selectedFromIndex = 0
    private var selectedToIndex = 1
    private var suppressEvents = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEasterEggSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCategorySpinner()
        setupUnitSpinners()

        binding.buttonConfirm.setOnClickListener {
            val value = binding.editTextSecretValue.text.toString().trim()
            if (value.isEmpty()) {
                Toast.makeText(this, R.string.setup_error_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveAndProceed(selectedCategoryIndex, selectedFromIndex, selectedToIndex, value)
        }

        binding.buttonSkip.setOnClickListener {
            saveAndProceed(0, 0, 0, "")
        }
    }

    private fun categoryNames() = categories.map { getString(it.nameResId) }
    private fun unitNames(index: Int) = categories[index].units.map { getString(it.nameResId) }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                if (suppressEvents) return
                selectedCategoryIndex = pos
                selectedFromIndex = 0
                selectedToIndex = 1
                updateUnitSpinners()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupUnitSpinners() {
        updateUnitSpinners()

        binding.spinnerFrom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                if (suppressEvents) return
                selectedFromIndex = pos
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spinnerTo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                if (suppressEvents) return
                selectedToIndex = pos
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateUnitSpinners() {
        val names = unitNames(selectedCategoryIndex)
        val fromAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val toAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        suppressEvents = true
        binding.spinnerFrom.adapter = fromAdapter
        binding.spinnerTo.adapter = toAdapter
        binding.spinnerFrom.setSelection(0)
        binding.spinnerTo.setSelection(minOf(1, names.size - 1))
        suppressEvents = false
    }

    private fun saveAndProceed(catIdx: Int, fromIdx: Int, toIdx: Int, value: String) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_CONFIGURED, true)
            .putInt(KEY_CATEGORY, catIdx)
            .putInt(KEY_FROM, fromIdx)
            .putInt(KEY_TO, toIdx)
            .putString(KEY_VALUE, value)
            .apply()
        startActivity(Intent(this, UnitConverterActivity::class.java))
        finish()
    }

    companion object {
        const val PREFS_NAME = "parkamat_prefs"
        const val KEY_CONFIGURED = "easter_egg_configured"
        const val KEY_CATEGORY = "easter_egg_category"
        const val KEY_FROM = "easter_egg_from"
        const val KEY_TO = "easter_egg_to"
        const val KEY_VALUE = "easter_egg_value"
    }
}
