package com.jacekpietras.zoo.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.jacekpietras.zoo.R
import com.jacekpietras.zoo.core.binding.viewBinding
import com.jacekpietras.zoo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setViews()
    }

    override fun onBackPressed() {
        // https://issuetracker.google.com/issues/139738913
        if (isTaskRoot
            && supportFragmentManager.primaryNavigationFragment
                ?.childFragmentManager?.backStackEntryCount == 0
            && supportFragmentManager.backStackEntryCount == 0
        ) {
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        // https://issuetracker.google.com/issues/139738913
        if (isTaskRoot) {
            finishAfterTransition()
        }
        super.onDestroy()
    }

    private fun setViews() {
        binding.navView.setupWithNavController(findNavController())
    }

    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.findNavController()
    }
}
