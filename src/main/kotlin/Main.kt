/**
 *
 * This is the main class. Starting of the app when run
 */
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue

val dataBuffer = ConcurrentLinkedQueue<Pair<ZonedDateTime, Record>>()
val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

/**
 * [getLatestDataUpdated] – This function will get the last time data is updated
 *
 * @param: timeApiCalled, time the API is called
 * @param: client, http client
 * @return return the last updated data, field HourUTC.
 */
suspend fun getLatestDataUpdated(timeApiCalled:ZonedDateTime, client: HttpClient): ZonedDateTime
{
    val url = "${Config.baseUrl}?offset=0&end=${timeApiCalled.format(formatter)}&limit=1&sort=HourUTC%20DESC"

    println("API Url $url")

    //call the API and get response
    val response: PowerApiResponse = client.get(url).body()
    val records = response.records

    if (records.isEmpty()) return timeApiCalled

    // set the endDate to the time we call the API
    return records.maxOfOrNull { parseTime(it.HourUTC) } ?: ZonedDateTime.now((ZoneOffset.UTC))
}

/**
 * [fetchLatestData] – This function will fetch all data from the last 5 minutes updated based on time API is called
 *
 * @param: timeApiCalled, time the API is called
 * @param: client, http client
 * @return return all the latest data for this API call.
 */
suspend fun fetchLatestData(timeApiCalled:ZonedDateTime, client: HttpClient): List<Record> {
    val limit = Config.limit
    val allRecords = mutableListOf<Record>()
    var offset = 0
    val lastTimeDataUpdated = getLatestDataUpdated(timeApiCalled, client)
    val start= lastTimeDataUpdated.minusMinutes(Config.averageWindowMinutes)

    println("LastTime Data Updated $lastTimeDataUpdated")

    println("Look back 5 minutes at $start")

    while (true) {
        // Build the API url with from and end

        val url = "${Config.baseUrl}?offset=$offset&end=${timeApiCalled.format(formatter)}&start=${start.format(formatter)}&limit=$limit&sort=HourUTC%20DESC"

        println("API Url $url")

        //call the API and get response
        val response: PowerApiResponse = client.get(url).body()
        val records = response.records

        // check if empty
        if (records.isEmpty()) break

        // add this page response into the total
        allRecords.addAll(records)

        // increase offset
        offset += records.size

        //If over the limit, break
        if (records.size < limit) break
    }

    return allRecords
}

/**
 * [cleanOldData] – This function will remove the old data from dataBuffer
 *
 * @param: cutoff, it will remove all data older than the cutoff
 */
fun cleanOldData(cutoff: ZonedDateTime) {
    while (dataBuffer.isNotEmpty() && dataBuffer.peek().first.isBefore(cutoff)) {
        dataBuffer.poll()
    }
}

/**
 * [calculateAverages] – This function will calculate the average values of data in dataBuffer
 *
 * @return return a map with (key/property: average_value).
 * @throws
 */
fun calculateAverages(): Map<String, Double> {
    val records = dataBuffer.map { it.second }

    fun avg(fieldName: String): Double {
        val values = records.mapNotNull { it.getField(fieldName) }
        return if (values.isNotEmpty()) values.average() else 0.0
    }

    return Config.trackedFields.associateWith { avg(it) }
}

/**
 * [main] – This main function
 * Running as a console app, run by .\gradlew.bat run in the terminal
 * It runs every 1 minutes and query data from an API
 * Then extract the last 5 minutes data from the response *
 * Then it computes the average value
 *
 */
fun main() {
    runBlocking {
        // set http client
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        launch {
            while (isActive) {
                val nowUtc = ZonedDateTime.now(ZoneOffset.UTC)
                println("=== Scheduled job running at $nowUtc  ===")

                // call the API to get data
                val records = fetchLatestData(nowUtc, client)

                // get the latest time
                val latestTime = records.maxOfOrNull { parseTime(it.HourUTC) } ?: ZonedDateTime.now((ZoneOffset.UTC))

                // set cutoff to 5 minutes ago
                val cutoff = latestTime.minusMinutes(Config.averageWindowMinutes)

                // get only data from the cutoff (latest 5 minutes)
                val recent = records.map { parseTime(it.HourUTC) to it }
                    .filter { (time, _) -> time.isAfter(cutoff) && !time.isAfter(latestTime) }


                // add data to dataBuffer
                recent.forEach { dataBuffer.add(it) }

                // remove old data from dataBuffer
                cleanOldData(cutoff)

                val avgResults = calculateAverages()
                println("5-minute averages: $avgResults")

                // save result to cvs file for Post process, as required to feed it to a ML model
                writeAveragesToTimestampedCsv(nowUtc, latestTime, avgResults)

                // wait 1 minute, the task is scheduled to run after every 1 minutes
                delay(Config.intervalMillis)
            }
        }

        println("Press Enter to show chart")
        readLine()

        // This will show a chart for data in today folder
        plotAverageData(ZonedDateTime.now(ZoneOffset.UTC))
    }
}
