package com.jacekpietras.zoo.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.jacekpietras.zoo.R
import com.jacekpietras.zoo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        binding.navView.setupWithNavController(findNavController())
        setContentView(binding.root)
    }

//    override fun onBackPressed() {
//        // https://issuetracker.google.com/issues/139738913
//        if (isTaskRoot && isBackStackEmpty && SDK_INT == VERSION_CODES.Q) {
//            finishAfterTransition()
//        } else {
//            super.onBackPressed()
//        }
//    }
//
//    override fun onStop() {
//        // https://issuetracker.google.com/issues/139738913
//        Timber.v("leak hunt pause root:$isTaskRoot finishing:$isFinishing")
//        if (isTaskRoot && SDK_INT == VERSION_CODES.Q && isFinishing) {
//            finishAfterTransition()
//            if (!isServiceRunning(this, TrackingService::class.java)) {
//                killProcess(Process.myPid())
//            }
//        }
//        super.onStop()
//    }
//
//    override fun onDestroy() {
//        // https://issuetracker.google.com/issues/139738913
//        Timber.v("leak hunt destroy root:$isTaskRoot")
//        if (isTaskRoot && SDK_INT == VERSION_CODES.Q) {
//            finishAfterTransition()
//            if (!isServiceRunning(this, TrackingService::class.java)) {
//                killProcess(Process.myPid())
//            }
//        }
//        super.onDestroy()
//    }

    //    private val isBackStackEmpty
//        get() = supportFragmentManager.primaryNavigationFragment
//            ?.childFragmentManager?.backStackEntryCount == 0
//                && supportFragmentManager.backStackEntryCount == 0
//
    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.findNavController()
    }
}
