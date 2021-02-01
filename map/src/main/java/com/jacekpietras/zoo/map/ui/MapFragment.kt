package com.jacekpietras.zoo.map.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.jacekpietras.zoo.core.binding.viewBinding
import com.jacekpietras.zoo.map.R
import com.jacekpietras.zoo.map.databinding.FragmentMapBinding
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapFragment : Fragment(R.layout.fragment_map) {

    private val viewModel by viewModel<MapViewModel>()

    private val binding: FragmentMapBinding by viewBinding(FragmentMapBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews()
        setObservers()
    }

    private fun setViews() = with(binding) {
//        mapView.objectList = listOf(
//            MapItem(
//                RectF(18f, 18f, 20f, 20f),
//                paint
//            ),
//            MapItem(
//                RectF(28f, 15f, 40f, 25f),
//                paint
//            ),
//            MapItem(
//                PathF(20f to 20f, 25f to 25f, 25f to 30f),
//                strokePaint
//            ),
//            MapItem(
//                PathF(21f to 20f, 26f to 25f, 26f to 30f, 20f to 20f),
//                dashedPaint
//            ),
//            MapItem(
//                PolygonF(19f to 22f, 24f to 25f, 24f to 30f, 18f to 22f),
//                paint,
//                onClick = { _, _ ->
//                    Toast.makeText(
//                        requireContext(),
//                        "Polygon clicked!",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            )
//        )
    }

    private fun setObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) {
            binding.mapView.objectList = it.mapData
        }
    }
}