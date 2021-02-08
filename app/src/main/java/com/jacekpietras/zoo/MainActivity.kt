package com.jacekpietras.zoo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.jacekpietras.zoo.core.binding.viewBinding
import com.jacekpietras.zoo.databinding.ActivityMainBinding
import com.jacekpietras.zoo.map.ui.MapFragment

class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setViews()
    }

    private fun setViews() {
        binding.navView.setupWithNavController(findNavController())
    }

    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.findNavController()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        toast("onActivity $requestCode $resultCode")

        (supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.get(0) as? MapFragment)
            ?.takeIf { it.checkInProgress == requestCode }
            ?.checkGpsPermission()
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
}
