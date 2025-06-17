/**
 *
 * This class is to map/load the configuration setup in config file config.properties
 */
import java.util.Properties

object Config {
    private val props = Properties()

    init {
        val input = {}::class.java.classLoader.getResourceAsStream("config.properties")
            ?: error("config.properties not found")
        props.load(input)
    }

    val baseUrl: String get() = props.getProperty("baseUrl")
    val limit: Int get() = props.getProperty("limit").toInt()
    val intervalMillis: Long get() = props.getProperty("intervalMillis").toLong()
    val averageWindowMinutes: Long get() = props.getProperty("averageWindowMinutes").toLong()
    val trackedFields: List<String> get() = props.getProperty("trackedFields").split(",").map { it.trim() }
    val outputDataFolder: String get() = props.getProperty("outputDataFolder")
}
