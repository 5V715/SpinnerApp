package dev.silas.spinner

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import dev.silas.spinner.model.Entry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import spinnerapp.composeapp.generated.resources.Res
import spinnerapp.composeapp.generated.resources.arrow_down
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Composable
@Preview
fun App() {

    MaterialTheme {
        val scope = rememberCoroutineScope()
        var showSpinner by remember { mutableStateOf(false) }
        var spins by remember { mutableStateOf(false) }
        var spin by remember { mutableStateOf(0f) }
        val entries = remember { mutableStateListOf<Entry>() }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(!showSpinner) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    AddElement(entries)
                    ListDisplay(entries)
                }
            }
            Button(onClick = { showSpinner = !showSpinner }) {
                Text(
                    when (showSpinner) {
                        true -> "Back"
                        else -> "Let's Go!"
                    }
                )
            }
            AnimatedVisibility(showSpinner) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        enabled = !spins,
                        onClick = {
                            if (!spins) {
                                spins = true
                                scope.launch {
                                    val until = Random.nextFloat() * 1_000f
                                    var current = 0f
                                    while (current <= until) {
                                        delay(5.milliseconds)
                                        println("current: $current, until: $until")
                                        spin = current++
                                    }
                                    spins = false
                                }
                            }
                        }) {
                        Text("SPIN!")
                    }
                    Image(
                        modifier = Modifier.height(20.dp).width(20.dp),
                        painter = painterResource(Res.drawable.arrow_down),
                        contentDescription = "arrow pointing down"
                    )
                    SpinWheel(
                        modifier = Modifier.rotate(spin),
                        items = entries
                    )
                }
            }
        }
    }
}


@Composable
fun ListDisplay(target: MutableList<Entry>) {
    target.forEach { entry ->
        Row {
            Text(entry.name)
            Button(onClick = {
                target.removeAll { it.name == entry.name }
            }) {
                Text("-")
            }
        }
    }
}


@Composable
fun AddElement(target: MutableList<Entry>) {

    var text: String by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            TextField(
                value = text,
                onValueChange = { value ->
                    text = value
                },
                singleLine = true,
                label = { Text("Name: ") }
            )
            Button(onClick = {
                if (text.isNotEmpty() && target.any { it.name == text }.not()) {
                    target.add(0, Entry(text))
                    text = ""
                }
            }) {
                Text("Add")
            }
        }
    }

}

@Composable
internal fun SpinWheelSlice(
    modifier: Modifier = Modifier,
    size: Dp,
    color: Color,
    degree: Float,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                drawArc(
                    color = color,
                    startAngle = -90f - (degree / 2),
                    sweepAngle = degree,
                    useCenter = true,
                )
            }
    ) {
        Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp)) {
            content()
        }
    }
}


@Composable
internal fun SpinWheel(
    modifier: Modifier = Modifier,
    items: List<Entry>,
) {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta, Color.Cyan)
    BoxWithConstraints(modifier = modifier) {

        val degreesPerItems = 360f / items.size.toFloat()
        val size = min(this.maxHeight, this.maxWidth)

        items.forEachIndexed { index, item ->
            SpinWheelSlice(
                modifier = Modifier.rotate(degrees = degreesPerItems * index),
                size = size,
                color = colors[index % colors.size],
                degree = degreesPerItems,
                content = {
                    Text(item.name)
                }
            )
        }
    }
}

fun List<Color>.toBrush(endY: Float): Brush =
    if (this.size == 1) {
        Brush.verticalGradient(colors = this)
    } else {
        val colorStops = this.mapIndexed { index, color ->
            val stop = if (index == 0) 0f else (index.toFloat() + 1f) / this.size.toFloat()
            Pair(stop, color)
        }.toTypedArray()
        Brush.verticalGradient(
            colorStops = colorStops,
            endY = endY,
        )
    }