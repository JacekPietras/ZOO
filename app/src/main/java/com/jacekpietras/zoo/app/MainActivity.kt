package com.jacekpietras.zoo.app

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Process
import android.os.Process.killProcess
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.jacekpietras.zoo.R
import com.jacekpietras.zoo.core.binding.viewBinding
import com.jacekpietras.zoo.databinding.ActivityMainBinding
import com.jacekpietras.zoo.tracking.TrackingService
import com.jacekpietras.zoo.tracking.isServiceRunning

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
            && SDK_INT == VERSION_CODES.Q
        ) {
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        // https://issuetracker.google.com/issues/139738913
        if (isTaskRoot && SDK_INT == VERSION_CODES.Q) {
            finishAfterTransition()
            if (!isServiceRunning(this, TrackingService::class.java)) {
                killProcess(Process.myPid())
            }
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
