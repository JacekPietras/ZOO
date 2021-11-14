package com.jacekpietras.zoo.planner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.google.android.material.composethemeadapter.MdcTheme

class PlannerFragment : Fragment() {

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View = ComposeView(requireContext()).apply {
        setContent {
            MdcTheme {
                Text(
                    text = "Dodaj trase predefiniowaną\nDodaj obiad o danej godzinie i miejscu\nDodaj karmienie które chcesz zobaczyć\nPrzegląd trasy z godzinami",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }
    }
}