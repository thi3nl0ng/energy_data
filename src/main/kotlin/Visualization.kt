/**
 *
 * This class is to setup and show a simple chart on the result in .csv file
 */

import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

/**
 * [parseTime] – This function parse a string to datetime
 *
 * @param: timeStr, input as string
 * @return return datetime in UTC.
 */
fun parseTime(timeStr: String): ZonedDateTime {
    val localTime = LocalDateTime.parse(timeStr)     // Parses "2025-06-08T06:00:00"
    return localTime.atZone(ZoneOffset.UTC)          // Assign UTC time zone explicitly
}

/**
 * [plotAverageData] – This will show a chart for data from the csv file
 *
 * @runtime: the time / today that a chart is shown
 * @return show the chart
 */
fun plotAverageData(runtime: ZonedDateTime) {
    val dayFolderName = runtime.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    val outputDataFolder = Config.outputDataFolder
    val csvPath = "$outputDataFolder/$dayFolderName/averages.csv"

    val runtimes = mutableListOf<Date>()
    val centralPower = mutableListOf<Double>()

    File(csvPath).useLines { lines ->
        val iterator = lines.iterator()
        if (iterator.hasNext()) iterator.next() // skip header

        while (iterator.hasNext()) {
            val line = iterator.next()
            val cols = line.split(',')

            // get the first column
            val runtimeStr = cols[0]

            // Example to show the first column with value (2 first columns are date)
            val centralPowerStr = cols[2]

            // Parse runtime (e.g. "2025-06-17T16:18")
            val zdt = parseTime(runtimeStr) // add seconds and Z for UTC
            val date = Date.from(zdt.toInstant())

            runtimes.add(date)
            centralPower.add(centralPowerStr.toDouble())
        }
    }

    val chart = XYChartBuilder()
        .width(800)
        .height(600)
        .title("Central Power MWh over Runtime")
        .xAxisTitle("Runtime")
        .yAxisTitle("CentralPowerMWh")
        .build()



    chart.addSeries("CentralPowerMWh", runtimes, centralPower)

    SwingWrapper(chart).displayChart()
}
