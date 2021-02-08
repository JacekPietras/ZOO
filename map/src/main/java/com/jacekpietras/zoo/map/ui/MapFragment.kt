package com.jacekpietras.zoo.map.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.jacekpietras.zoo.core.binding.viewBinding
import com.jacekpietras.zoo.core.extensions.observe
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.databinding.FragmentMapBinding
import com.jacekpietras.zoo.map.model.MapEffect
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import com.jacekpietras.zoo.tracking.GpsPermissionChecker
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapFragment : Fragment(R.layout.fragment_map) {

    private val viewModel by viewModel<MapViewModel>()
    private val binding: FragmentMapBinding by viewBinding(FragmentMapBinding::bind)
    private val permissionChecker = GpsPermissionChecker(fragment = this)

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
            onDescriptionNeeded = { toast("description needed $it") },
            onFailed = { toast(R.string.location_unavailable) },
            onPermission = { viewModel.onMyLocationClicked() },
        )
    }

    private fun toast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    }

    private fun toast(@StringRes textRes: Int) {
        toast(getString(textRes))
    }
}