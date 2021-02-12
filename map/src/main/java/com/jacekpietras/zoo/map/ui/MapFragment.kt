package com.jacekpietras.zoo.map.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jacekpietras.zoo.core.binding.viewBinding
import com.jacekpietras.zoo.core.extensions.observe
import com.jacekpietras.zoo.map.BuildConfig
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.databinding.FragmentMapBinding
import com.jacekpietras.zoo.map.model.MapEffect
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import com.jacekpietras.zoo.tracking.GpsPermissionRequester
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapFragment : Fragment(R.layout.fragment_map) {

    private val viewModel by viewModel<MapViewModel>()
    private val binding: FragmentMapBinding by viewBinding(FragmentMapBinding::bind)
    private val permissionChecker = GpsPermissionRequester(fragment = this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        setListeners()
    }

    private fun setObservers() = with(viewModel.viewState) {
        observe(userPosition) { binding.mapView.userPosition = it }
        observe(mapData) { binding.mapView.objectList = it }
        observe(worldBounds) { binding.mapView.worldBounds = it }
        observe(effect) {
            when (it) {
                is MapEffect.ShowToast -> toast(it.text)
            }
        }
    }

    private fun setListeners() = with(binding) {
        uploadButton.setOnClickListener { viewModel.onUploadClicked() }
        myLocationButton.setOnClickListener { checkGpsPermission() }
    }

    private fun checkGpsPermission() {
        permissionChecker.checkPermissions(
            rationaleTitle = R.string.gps_permission_rationale_title,
            rationaleContent = R.string.gps_permission_rationale_content,
            deniedTitle = R.string.gps_permission_denied_title,
            deniedContent = R.string.gps_permission_denied_content,
            onFailed = { if (BuildConfig.DEBUG) toast("Location denied") },
            onPermission = { viewModel.onMyLocationClicked() },
        )
    }

    private fun toast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    }
}