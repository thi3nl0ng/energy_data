/**
 *
 * Output the computed result to file
 */

import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * [writeAveragesToTimestampedCsv] – This function write data to csv file for later processing like feed to ML model
 * The folder structure: data/averages/yyyyMMdd/averages.csv
 * @param: runtime, time the API is called, then is used to save the result here
 * @param: timestamp, the timestamp that data is updated (from API)
 * @param: data, the average computed result
 */
fun writeAveragesToTimestampedCsv(runtime: ZonedDateTime, timestamp: ZonedDateTime, data: Map<String, Double>) {
    // Folder path: base folder + yyyyMMdd from runtime
    val dayFolderName = runtime.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

    val folder = File(Config.outputDataFolder, dayFolderName)
    if (!folder.exists()) {
        folder.mkdirs()
    }

    val fileName = "averages.csv"
    val file = File(folder, fileName)

    val header = "runtime,timestamp," + data.keys.joinToString(",")
    val line = runtime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) + "," + timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) + "," +
            data.values.joinToString(",")

    // Write header only if file doesn't exist
    if (!file.exists()) {
        file.writeText(header + "\n")
    }

    // Append the new data line
    file.appendText(line + "\n")

    println("Saved average data to ${file.absolutePath}")
}

