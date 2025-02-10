package com.example.mentalhealthemotion.ui.theme

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mentalhealthemotion.Data.MoodEntryViewModel
import com.example.mentalhealthemotion.Data.MoodType
import com.example.mentalhealthemotion.Data.UserViewModel
import com.example.mentalhealthemotion.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.data.Entry


@Composable
fun StatisticPage(
    onNavigate: (String) -> Unit,
    toEntryPage: () -> Unit,
    moodEntryViewModel: MoodEntryViewModel,
    userViewModel: UserViewModel
) {
    val moodCounts by moodEntryViewModel.moodCounts.observeAsState(emptyList())
    val user by userViewModel.currentUser.observeAsState()
    val scrollState = rememberScrollState()
    val barEntries by moodEntryViewModel.moodActivityChartData.collectAsState()


    val labels by moodEntryViewModel.moodActivityLabels.collectAsState()
    val weeklyBarEntries by moodEntryViewModel.weeklyMoodChartData.collectAsState()
    val weeklyLabels by moodEntryViewModel.weeklyMoodLabels.collectAsState()
    var chartType by remember { mutableStateOf("Bar") } // Bar or Line chart toggle

    LaunchedEffect(user?.userID) {
        user?.userID?.let { userId ->
            moodEntryViewModel.countMoodsForCurrentMonth(userId)
            moodEntryViewModel.createMoodActivityDataForMonth(userId)
            moodEntryViewModel.createMoodDataForCurrentWeek(userId)
        }
    }

    val moodImages = mapOf(
        "awful" to R.drawable.awful,
        "bad" to R.drawable.bad,
        "meh" to R.drawable.meh,
        "good" to R.drawable.good,
        "rad" to R.drawable.rad
    )

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Mood Chart Title
            Row{
                Icon(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "Back",
                    tint = Color(0xFF2E3E64),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { toEntryPage()  }
                )
                Spacer(modifier = Modifier.width(35.dp))
                Text(
                    text = "Mood Statistics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E3E64)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Placeholder for Chart

            // Check if all mood types have zero Y-axis values for all days in the week
            val isWeeklyChartEmpty = weeklyBarEntries.isEmpty() || weeklyBarEntries.all { (_, entries) ->
                entries.all { entry -> entry.y == 0f } // Check if all counts are 0
            }

            val isMonthlyChartEmpty = barEntries.isEmpty() || barEntries.values.all { it.isEmpty() }

            if (isMonthlyChartEmpty) {
                Text(
                    text = "No mood graphs available due to insufficient data.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else {
                barEntries.forEach { (moodType, entries) ->
                    val activityLabels = labels[moodType] ?: emptyList()
                    moodTypeChart(moodType, entries, activityLabels, chartType)
                }
            }

            if (isWeeklyChartEmpty) {
                Text(
                    text = "No weekly graphs available due to insufficient data.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else {
                weeklyBarEntries.forEach { (moodType, entries) ->
                    weeklyMoodTypeChart(moodType, entries, weeklyLabels, chartType)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Toggle Button for Chart Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { chartType = "Bar" },
                    modifier = Modifier
                        .width(120.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5AB9FFF))
                ) {
                    Text(
                        text = "Bar Chart",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
                Button(
                    onClick = { chartType = "Line" },
                    modifier = Modifier
                        .width(120.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5AB9FFF))
                )
                {
                    Text(
                        text = "Line Chart",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Divider(color = Color.LightGray, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(30.dp))

            // Mood Count of the Month Section
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Mood Count of the Month",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E3E64)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        moodCounts.forEach { mood ->
                            val iconResId = moodImages[mood.moodType] ?: R.drawable.meh
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = mood.count.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E3E64)
                                )
                                Image(
                                    painter = painterResource(id = iconResId),
                                    contentDescription = mood.moodType,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .padding(top = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = mood.moodType,
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Divider(color = Color.LightGray, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(30.dp))
            Spacer(modifier = Modifier.height(30.dp))
        }

        // Bottom Navigation Bar
        BottomNavigationBar(
            onNavigate = onNavigate,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

val moodColors = mapOf(
    MoodType.meh to Color(0xFFFFEA00), //Yellow
    MoodType.bad to Color(0xFFFFAC1C), //Orange
    MoodType.awful to Color(0xFFFF1439), //Red
    MoodType.good to Color(0xFF32CD32), //Green
    MoodType.rad to Color(0xFF2008000) //Dark Green
)

@Composable
fun weeklyMoodTypeChart(
    moodType: MoodType,
    barEntries: List<BarEntry>,
    weeklyMoodLabels: List<String>,
    chartType: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Number of entries of the mood ")
                    withStyle(
                        style = SpanStyle(
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    ) { // Change color and style for moodType
                        append("\"$moodType\"")
                    }
                    append(" by weekdays")
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E3E64),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (chartType == "Bar") {
                    BarChartView(barEntries, weeklyMoodLabels, Color(0xFF2E3E64))
                } else {
                    LineChartView(
                        barEntries.map { Entry(it.x, it.y) },
                        weeklyMoodLabels,
                        Color(0xFF2E3E64)
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
}


@Composable
fun moodTypeChart(
    moodType: MoodType,
    barEntries: List<BarEntry>,
    activityLabels: List<String>,
    chartType: String
) {
    val chartColor = moodColors[moodType] ?: Color.Gray
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Percentage of ")
                    withStyle(
                        style = SpanStyle(
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    ) { // Change color and style for moodType
                        append("\"$moodType\"")
                    }
                    append(" days that has these activities")
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E3E64),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (chartType == "Bar") {
                    BarChartView(barEntries, activityLabels, chartColor)
                } else {
                    LineChartView(barEntries.map { Entry(it.x, it.y) }, activityLabels, chartColor)
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
fun BarChartView(entries: List<BarEntry>, labels: List<String>, chartColor: Color) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.labelRotationAngle = 45f  // Rotate labels to avoid overlap
                xAxis.isGranularityEnabled = true
                xAxis.setDrawGridLines(false)

                axisLeft.setDrawGridLines(false)
                axisLeft.granularity = 1f  // Ensure Y-axis increments by 1
                axisLeft.isGranularityEnabled = true
                axisLeft.axisMinimum = 0f  // Start from 0
                axisLeft.axisMaximum = entries.maxOf { it.y } + 1f

                axisRight.isEnabled = false
                legend.isEnabled = false
                extraBottomOffset = 10f

                data = BarData(BarDataSet(entries, "Moods").apply {
                    color = chartColor.toArgb()
                    valueTextColor = Color.Black.toArgb()
                })
                invalidate()
            }
        }
    )
}

@Composable
fun LineChartView(entries: List<Entry>, labels: List<String>, chartColor: Color) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.labelRotationAngle = 45f
                xAxis.isGranularityEnabled = true
                xAxis.setDrawGridLines(false)

                axisLeft.setDrawGridLines(false)
                axisLeft.granularity = 1f  // Ensure Y-axis increments by 1
                axisLeft.isGranularityEnabled = true
                axisLeft.axisMinimum = 0f  // Start from 0
                axisLeft.axisMaximum = entries.maxOf { it.y } + 1f

                axisRight.isEnabled = false
                legend.isEnabled = false

                data = LineData(LineDataSet(entries, "Moods").apply {
                    color = chartColor.toArgb()
                    valueTextColor = Color.Black.toArgb()
                })
                invalidate()
            }
        }
    )
}

