package com.jacekpietras.zoo.catalogue.ui

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativePaint
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.google.accompanist.glide.GlideImage
import com.jacekpietras.zoo.catalogue.model.CatalogueViewState
import com.jacekpietras.zoo.catalogue.viewmodel.CatalogueViewModel
import com.jacekpietras.zoo.core.extensions.getColorFromAttr
import org.koin.androidx.viewmodel.ext.android.viewModel

class CatalogueFragment : Fragment() {

    private val viewModel by viewModel<CatalogueViewModel>()
    private lateinit var textPaintStroke: NativePaint
    private lateinit var textPaintStroke2: NativePaint
    private lateinit var textPaint: NativePaint

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        ComposeView(requireContext()).apply {
            setContent {
                val listTextSize = with(LocalDensity.current) { 18.sp.toPx() }
                textPaintStroke = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    style = android.graphics.Paint.Style.STROKE
                    textSize = listTextSize
                    color = requireContext().getColorFromAttr(android.R.attr.colorPrimary)
                    strokeWidth = listTextSize / 2
                    strokeMiter = 1f
                    strokeJoin = android.graphics.Paint.Join.ROUND
                    strokeCap = android.graphics.Paint.Cap.ROUND
                }
                textPaintStroke2 = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    style = android.graphics.Paint.Style.STROKE
                    textSize = listTextSize
                    color = requireContext().getColorFromAttr(android.R.attr.colorPrimary)
                    strokeWidth = listTextSize / 3
                    strokeMiter = 1f
                    strokeJoin = android.graphics.Paint.Join.ROUND
                    strokeCap = android.graphics.Paint.Cap.ROUND
                }

                textPaint = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    style = android.graphics.Paint.Style.FILL
                    textSize = listTextSize
                    color = android.graphics.Color.WHITE
                }
                AnimalList()
            }
        }

    @Composable
    fun AnimalList() {
        val viewState: CatalogueViewState by viewModel.viewState.collectAsState(initial = CatalogueViewState())

        MaterialTheme {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            ) {
                items(viewState.animalList) { animal ->
                    Card(
                        shape = RoundedCornerShape(4.dp),
                        backgroundColor = Color.LightGray,
                        elevation = 4.dp,
                        modifier = Modifier
                            .height(96.dp)
                            .fillMaxSize(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(onClick = { }),
                        ) {
                            GlideImage(
                                data = animal.img ?: "no image",
                                contentDescription = animal.name,
                                fadeIn = true,
                                contentScale = ContentScale.Crop,
                            )
                            OutlinedTextView(text = "${animal.name} - ${animal.regionInZoo}")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun BoxScope.OutlinedTextView(text:String) {
        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        val textWidth =
            with(LocalDensity.current) { bounds.width().toFloat() + 4.dp.toPx() }
        Canvas(
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            onDraw = {
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        text,
                        -textWidth,
                        -4.dp.toPx(),
                        textPaintStroke,
                    )
                    canvas.nativeCanvas.drawText(
                        text,
                        -textWidth,
                        -4.dp.toPx(),
                        textPaintStroke2,
                    )
                    canvas.nativeCanvas.drawText(
                        text,
                        -textWidth,
                        -4.dp.toPx(),
                        textPaint,
                    )
                }
            }
        )
    }
}
