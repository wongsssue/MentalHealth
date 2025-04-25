package com.example.mentalhealthemotion.ui.theme

import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun ColoringScreen() {
    val colors = listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan)
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var selectedShape by remember { mutableStateOf("Circle") }
    val shapes = remember { mutableStateListOf<ShapeData>() }
    val paths = remember { mutableStateListOf<PathData>() }
    var drawingMode by remember { mutableStateOf(false) }
    var eraserMode by remember { mutableStateOf(false) }
    var movingShapeIndex by remember { mutableStateOf<Int?>(null) }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        if (eraserMode) {
                            // Remove path if close to tap point
                            paths.removeAll { it.isNear(tapOffset) }
                            // Remove shape if tapped inside
                            shapes.removeAll { it.isInside(tapOffset) }
                        } else if (!drawingMode) {
                            val index = shapes.indexOfLast { it.isInside(tapOffset) }
                            if (index != -1) {
                                movingShapeIndex = index
                            } else {
                                shapes.add(ShapeData(tapOffset, selectedColor, 50f, selectedShape))
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (eraserMode) {
                                // Remove paths and shapes near drag start
                                paths.removeAll { it.isNear(offset) }
                                shapes.removeAll { it.isInside(offset) }
                            } else if (!drawingMode) {
                                movingShapeIndex = shapes.indexOfLast { it.isInside(offset) }
                            } else {
                                currentPath = Path().apply { moveTo(offset.x, offset.y) }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            if (eraserMode) {
                                // Erase paths and shapes as the user drags
                                paths.removeAll { it.isNear(change.position) }
                                shapes.removeAll { it.isInside(change.position) }
                            } else if (!drawingMode) {
                                movingShapeIndex?.let { index ->
                                    if (index in shapes.indices) {
                                        shapes[index] = shapes[index].copy(center = shapes[index].center + dragAmount)
                                    }
                                }
                            } else {
                                currentPath?.lineTo(change.position.x, change.position.y)
                            }
                        },
                        onDragEnd = {
                            movingShapeIndex = null
                            currentPath?.let { paths.add(PathData(it, selectedColor, 5f)) }
                            currentPath = null
                        }
                    )
                }
        ) {
            paths.forEach { drawPath(it.path, it.color, style = Stroke(it.strokeWidth)) }
            shapes.forEach { shape ->
                when (shape.type) {
                    "Circle" -> drawCircle(shape.color, shape.radius, shape.center)
                    "Square" -> drawRect(
                        shape.color,
                        Offset(shape.center.x - shape.radius, shape.center.y - shape.radius),
                        Size(shape.radius * 2, shape.radius * 2)
                    )
                    "Triangle" -> {
                        val path = Path().apply {
                            moveTo(shape.center.x, shape.center.y - shape.radius)
                            lineTo(shape.center.x - shape.radius, shape.center.y + shape.radius)
                            lineTo(shape.center.x + shape.radius, shape.center.y + shape.radius)
                            close()
                        }
                        drawPath(path, color = shape.color)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { drawingMode = !drawingMode; eraserMode = false }) {
                Text(if (drawingMode) "Switch to Shapes" else "Switch to Drawing")
            }
            Button(onClick = { eraserMode = !eraserMode; drawingMode = false }) {
                Text(if (eraserMode) "Disable Eraser" else "Enable Eraser")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Circle", "Square", "Triangle").forEach { shape ->
                Button(
                    onClick = { selectedShape = shape },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedShape == shape) Color.Gray else Color.LightGray
                    ),
                    enabled = !drawingMode && !eraserMode
                ) {
                    Text(shape)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(color, shape = CircleShape)
                        .clickable { selectedColor = color }
                )
            }
        }

        Button(
            onClick = { shapes.clear(); paths.clear() },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Clear Coloring")
        }
    }
}

// Data class for storing shape details
data class ShapeData(val center: Offset, val color: Color, val radius: Float, val type: String) {
    fun isInside(point: Offset): Boolean {
        return when (type) {
            "Circle" -> (center - point).getDistance() <= radius * 1.5f
            "Square" -> point.x in (center.x - radius)..(center.x + radius) &&
                    point.y in (center.y - radius)..(center.y + radius)
            "Triangle" -> {
                val path = Path().apply {
                    moveTo(center.x, center.y - radius)
                    lineTo(center.x - radius, center.y + radius)
                    lineTo(center.x + radius, center.y + radius)
                    close()
                }
                val bounds = path.getBounds()
                point.x in bounds.left..bounds.right && point.y in bounds.top..bounds.bottom
            }
            else -> false
        }
    }
}

data class PathData(val path: Path, val color: Color, val strokeWidth: Float) {
    fun isNear(point: Offset, threshold: Float = 20f): Boolean {
        val androidPath = path.asAndroidPath()
        val pathMeasure = android.graphics.PathMeasure(androidPath, false)
        val pos = FloatArray(2)
        var distance = 0f
        val step = 10f // Check every 10px

        while (distance < pathMeasure.length) {
            pathMeasure.getPosTan(distance, pos, null)
            if (Offset(pos[0], pos[1]).getDistance() <= threshold) {
                return true
            }
            distance += step
        }
        return false
    }
}
