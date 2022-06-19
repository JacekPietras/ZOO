package com.jacekpietras.zoo.planner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.jacekpietras.zoo.core.theme.ZooTheme
import com.jacekpietras.zoo.planner.R
import com.jacekpietras.zoo.planner.model.PlannerViewState
import com.jacekpietras.zoo.planner.viewmodel.PlannerViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlannerFragment : Fragment() {

    private val viewModel by viewModel<PlannerViewModel>()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View = ComposeView(requireContext()).apply {
        setContent {
            val viewState: PlannerViewState by viewModel.viewState.observeAsState(initial = PlannerViewState())

            ZooTheme {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                ) {
                    items(viewState.list) { item ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            backgroundColor = ZooTheme.colors.surface,
                            elevation = 4.dp,
                            modifier = Modifier
                                .fillMaxSize(),
                        ) {
                            Row {
                                Text(
                                    text = item.text.toString(LocalContext.current),
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .weight(1f)
                                        .align(Alignment.CenterVertically),
                                    style = MaterialTheme.typography.subtitle2,
                                    color = ZooTheme.colors.onSurface,
                                )

                                SideIconView(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    iconRes = R.drawable.ic_close_24,
                                    contentDescription = R.string.remove_from_plan,
                                    onClick = { viewModel.onRemove(item.regionId) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SideIconView(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    @StringRes contentDescription: Int,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier
            .then(Modifier.size(48.dp)),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(iconRes),
            tint = MaterialTheme.colors.onSurface,
            contentDescription = stringResource(contentDescription)
        )
    }
}