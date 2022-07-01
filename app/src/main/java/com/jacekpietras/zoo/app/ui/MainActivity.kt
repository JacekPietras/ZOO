package com.jacekpietras.zoo.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jacekpietras.zoo.core.theme.ZooTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZooTheme {
                MainActivityView()
            }
        }
    }


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val binding = ActivityMainBinding.inflate(layoutInflater)
//        binding.navView.setupWithNavController(findNavController())
//        setContentView(binding.root)
//    }
//
//    private fun findNavController(): NavController {
//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        return navHostFragment.findNavController()
//    }
}
