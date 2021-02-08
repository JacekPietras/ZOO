package com.jacekpietras.zoo.map.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jacekpietras.zoo.core.binding.viewBinding
import com.jacekpietras.zoo.core.extensions.observe
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.databinding.FragmentMapBinding
import com.jacekpietras.zoo.map.model.MapEffect
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import com.jacekpietras.zoo.tracking.GpsPermissionChecker
import com.jacekpietras.zoo.tracking.GpsPermissionChecker.Callback
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapFragment : Fragment(R.layout.fragment_map) {

    private val viewModel by viewModel<MapViewModel>()
    private val binding: FragmentMapBinding by viewBinding(FragmentMapBinding::bind)
    private val permissionChecker = GpsPermissionChecker(fragment = this)
    var checkInProgress: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        setListeners()
    }

    private fun setObservers() = with(viewModel.viewState) {
        observe(userPosition) { binding.mapView.userPosition = it }
        observe(mapData) { binding.mapView.objectList = it }
        observe(effect) {
            when (it) {
                is MapEffect.ShowToast -> {
                    Toast.makeText(
                        requireContext(),
                        it.text,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setListeners() = with(binding) {
        uploadButton.setOnClickListener { viewModel.onUploadClicked() }
        myLocationButton.setOnClickListener { checkGpsPermission() }
    }

    fun checkGpsPermission() {
        checkInProgress = 0
        permissionChecker.checkPermissions(
            Callback(
                onDescriptionNeeded = { toast("description needed $it") },
                onFailed = { toast("failed") },
                onPermission = { toast("success") },
                onRequested = {
                    toast("requested $it")
                    checkInProgress = it
                },
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (checkInProgress == requestCode && resultCode == RESULT_OK) {
            checkGpsPermission()
        }
    }

    private fun toast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    }
}
