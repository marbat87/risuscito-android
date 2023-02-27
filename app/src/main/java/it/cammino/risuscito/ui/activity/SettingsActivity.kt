package it.cammino.risuscito.ui.activity

import android.os.Bundle
import it.cammino.risuscito.R
import it.cammino.risuscito.databinding.ActivityFragmentHostBinding
import it.cammino.risuscito.ui.fragment.SettingsFragment

class SettingsActivity : ThemeableActivity() {

    private lateinit var binding: ActivityFragmentHostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFragmentHostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.detail_fragment, SettingsFragment())
            .commit()
    }
}
