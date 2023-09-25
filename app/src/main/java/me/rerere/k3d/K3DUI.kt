package me.rerere.k3d

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun K3DController(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("K3D", style = MaterialTheme.typography.titleMedium)

        content()
    }
}

@Composable
fun K3DFloatController(
    modifier: Modifier = Modifier,
    label: String,
    getter: () -> Float,
    setter: (Float) -> Unit,
    min: Float = 0f,
    max: Float = 1f,
) {
    val state = remember { mutableFloatStateOf(getter()) }

    LaunchedEffect(Unit) {
        snapshotFlow { state.floatValue }
            .distinctUntilChanged()
            .collect {
                setter(it)
            }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )

        Slider(
            value = state.floatValue,
            onValueChange = {
                state.floatValue = it
            },
            valueRange = min..max,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "%.2f".format(state.floatValue),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}