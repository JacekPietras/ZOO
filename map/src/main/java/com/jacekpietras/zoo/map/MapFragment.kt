package com.jacekpietras.zoo.map

import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jacekpietras.zoo.core.viewBinding
import com.jacekpietras.zoo.map.databinding.FragmentMapBinding

class MapFragment : Fragment(R.layout.fragment_map) {
    private val paint by lazy {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.RED
        }
    }
    private val dashedPaint by lazy {
        Paint().apply {
            color = Color.BLUE
            style = Paint.Style.STROKE
            strokeWidth = 2.dp
            pathEffect = DashPathEffect(floatArrayOf(8.dp, 8.dp), 0f)
        }
    }
    private val strokePaint by lazy {
        Paint().apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 2.dp
        }
    }
    private val binding: FragmentMapBinding by viewBinding(FragmentMapBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews()
    }

    private fun setViews() = with(binding) {
        mapView.objectList = listOf(
            MapItem(
                RectF(18f, 18f, 20f, 20f),
                paint
            ),
            MapItem(
                RectF(28f, 15f, 40f, 25f),
                paint
            ),
            MapItem(
                PathF(20f to 20f, 25f to 25f, 25f to 30f),
                strokePaint
            ),
            MapItem(
                PathF(21f to 20f, 26f to 25f, 26f to 30f, 20f to 20f),
                dashedPaint
            ),
            MapItem(
                PolygonF(19f to 22f, 24f to 25f, 24f to 30f, 18f to 22f),
                paint,
                onClick = { _, _ ->
                    Toast.makeText(
                        requireContext(),
                        "Polygon clicked!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        )
    }

    private val Int.dp: Float
        get() =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                toFloat(),
                requireContext().resources.displayMetrics
            )
}