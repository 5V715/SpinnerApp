package dev.silas.spinner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
                    },
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
                                    val until = Random.nextFloat() * 500f
                                    var current = 0f
                                    while (current <= until) {
                                        delay(3.milliseconds)
                                        spin = current++
                                    }
                                    spins = false
                                }
                            }
                        },
                    ) {
                        Text("SPIN!")
                    }
                    Image(
                        modifier = Modifier.height(20.dp).width(20.dp),
                        painter = painterResource(Res.drawable.arrow_down),
                        contentDescription = "arrow pointing down",
                    )
                    SpinWheel(
                        modifier = Modifier.rotate(spin),
                        entries = entries,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListDisplay(entries: MutableList<Entry>) {
    entries.forEach { entry ->
        Row {
            Text(
                text = entry.name,
            )
            Button(onClick = {
                entries.removeAll { it.name == entry.name }
            }) {
                Text(
                    text = "-",
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddElement(target: MutableList<Entry>) {
    var text: String by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        FlowRow {
            TextField(
                value = text,
                onValueChange = { value ->
                    text = value
                },
                singleLine = true,
                label = { Text("Name: ") },
            )
            Button(
                modifier = Modifier.fillMaxRowHeight(),
                onClick = {
                    text.split(",").forEach { it ->
                        if (it.isNotEmpty() && target.any { it.name == text }.not()) {
                            target.add(0, Entry(it))
                        }
                    }
                    text = ""
                },
            ) {
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
        modifier =
            modifier
                .size(size)
                .drawBehind {
                    drawArc(
                        color = color,
                        startAngle = -90f - (degree / 2),
                        sweepAngle = degree,
                        useCenter = true,
                    )
                },
    ) {
        Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp)) {
            content()
        }
    }
}

@Composable
internal fun SpinWheel(
    modifier: Modifier = Modifier,
    entries: MutableList<Entry>,
) {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta, Color.Cyan)
    BoxWithConstraints(modifier = modifier) {
        val degreesPerItems = 360f / entries.size.toFloat()
        val size = min(this.maxHeight, this.maxWidth)

        entries.forEachIndexed { index, item ->
            SpinWheelSlice(
                modifier = Modifier.rotate(degrees = degreesPerItems * index),
                size = size,
                color = colors[index % colors.size],
                degree = degreesPerItems,
                content = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            modifier = Modifier.background(Color.White),
                            textDecoration = TextDecoration.Underline,
                            text = item.name,
                            style =
                                TextStyle(
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        Button(
                            onClick = {
                                entries.removeAt(index)
                            },
                        ) {
                            Text("-")
                        }
                    }
                },
            )
        }
    }
}
