package com.braffdev.steganofy.ui.converter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.braffdev.steganofy.R
import com.braffdev.steganofy.databinding.ActivityUnitConverterBinding
import com.braffdev.steganofy.ui.common.applyWindowInsetsAsPadding
import com.braffdev.steganofy.ui.main.MainActivity
import com.braffdev.steganofy.ui.setup.EasterEggSetupActivity

class UnitConverterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnitConverterBinding

    private val categories = ConversionEngine.categories
    private var currentCategoryIndex = 0
    private var selectedFromIndex = 0
    private var selectedToIndex = 1
    private var suppressSpinnerEvents = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // First-launch: redirect to Easter egg setup if not yet configured
        val prefs = getSharedPreferences(EasterEggSetupActivity.PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(EasterEggSetupActivity.KEY_CONFIGURED, false)) {
            startActivity(Intent(this, EasterEggSetupActivity::class.java))
            finish()
            return
        }

        binding = ActivityUnitConverterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applyWindowInsetsAsPadding()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        setupCategorySpinner()
        setupUnitSpinners()
        setupInputWatcher()
        setupSwapButton()

        savedInstanceState?.let {
            currentCategoryIndex = it.getInt(KEY_CATEGORY, 0)
            selectedFromIndex = it.getInt(KEY_FROM, 0)
            selectedToIndex = it.getInt(KEY_TO, 1)
            suppressSpinnerEvents = true
            binding.spinnerCategory.setSelection(currentCategoryIndex)
            updateUnitSpinners()
            binding.spinnerFrom.setSelection(selectedFromIndex)
            binding.spinnerTo.setSelection(selectedToIndex)
            suppressSpinnerEvents = false
            binding.editTextValue.setText(it.getString(KEY_INPUT, ""))
            recalculate()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CATEGORY, currentCategoryIndex)
        outState.putInt(KEY_FROM, selectedFromIndex)
        outState.putInt(KEY_TO, selectedToIndex)
        outState.putString(KEY_INPUT, binding.editTextValue.text.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_converter, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_steganography -> {
                startActivity(Intent(this, MainActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun categoryNames() = categories.map { getString(it.nameResId) }
    private fun unitNames(categoryIndex: Int) = categories[categoryIndex].units.map { getString(it.nameResId) }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                if (suppressSpinnerEvents) return
                currentCategoryIndex = pos
                selectedFromIndex = 0
                selectedToIndex = 1
                updateUnitSpinners()
                recalculate()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupUnitSpinners() {
        updateUnitSpinners()

        binding.spinnerFrom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                if (suppressSpinnerEvents) return
                selectedFromIndex = pos
                recalculate()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spinnerTo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                if (suppressSpinnerEvents) return
                selectedToIndex = pos
                recalculate()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateUnitSpinners() {
        val names = unitNames(currentCategoryIndex)
        val fromAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val toAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        suppressSpinnerEvents = true
        binding.spinnerFrom.adapter = fromAdapter
        binding.spinnerTo.adapter = toAdapter
        binding.spinnerFrom.setSelection(selectedFromIndex.coerceIn(0, names.size - 1))
        binding.spinnerTo.setSelection(selectedToIndex.coerceIn(0, names.size - 1))
        suppressSpinnerEvents = false
    }

    private fun setupInputWatcher() {
        binding.editTextValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { recalculate() }
        })
    }

    private fun setupSwapButton() {
        binding.buttonSwap.setOnClickListener {
            val tmp = selectedFromIndex
            selectedFromIndex = selectedToIndex
            selectedToIndex = tmp
            suppressSpinnerEvents = true
            binding.spinnerFrom.setSelection(selectedFromIndex)
            binding.spinnerTo.setSelection(selectedToIndex)
            suppressSpinnerEvents = false
            val result = binding.textViewResult.text.toString()
            if (result.isNotEmpty() && result != "—") {
                binding.editTextValue.setText(result)
                binding.editTextValue.setSelection(result.length)
            } else recalculate()
        }
    }

    private fun recalculate() {
        val input = binding.editTextValue.text.toString()
        val value = input.toDoubleOrNull()
        if (value == null) {
            binding.textViewResult.text = if (input.isEmpty()) "" else "—"
            return
        }

        val category = categories[currentCategoryIndex]
        val units = category.units
        val fromUnit = units.getOrElse(selectedFromIndex) { units[0] }
        val toUnit = units.getOrElse(selectedToIndex) { units[0] }

        // 🔐 User-configured Easter egg
        val prefs = getSharedPreferences(EasterEggSetupActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val secretValue = prefs.getString(EasterEggSetupActivity.KEY_VALUE, "") ?: ""
        if (secretValue.isNotEmpty() &&
            input == secretValue &&
            currentCategoryIndex == prefs.getInt(EasterEggSetupActivity.KEY_CATEGORY, -1) &&
            selectedFromIndex == prefs.getInt(EasterEggSetupActivity.KEY_FROM, -1) &&
            selectedToIndex == prefs.getInt(EasterEggSetupActivity.KEY_TO, -1)) {
            startActivity(Intent(this, MainActivity::class.java))
            return
        }

        val result = ConversionEngine.convert(value, fromUnit, toUnit, category.id)
        binding.textViewResult.text = ConversionEngine.formatResult(result)
    }

    companion object {
        private const val KEY_CATEGORY = "key_category"
        private const val KEY_FROM = "key_from"
        private const val KEY_TO = "key_to"
        private const val KEY_INPUT = "key_input"
    }
}
