/**
 *
 * Data model from meta api and api response
 */

import kotlinx.serialization.Serializable

@Serializable
data class PowerApiResponse(
    val total: Int,
    val sort: String,
    val limit: Int,
    val dataset: String,
    val records: List<Record>
)

@Serializable
data class Record(
    val HourUTC: String,
    val HourDK: String,
    val PriceArea: String,
    val CentralPowerMWh: Double?,
    val LocalPowerMWh: Double?,
    val CommercialPowerMWh: Double?,
    val LocalPowerSelfConMWh: Double?,
    val OffshoreWindLt100MW_MWh: Double?,
    val OffshoreWindGe100MW_MWh: Double?,
    val OnshoreWindLt50kW_MWh: Double?,
    val OnshoreWindGe50kW_MWh: Double?,
    val HydroPowerMWh: Double?,
    val SolarPowerLt10kW_MWh: Double?,
    val SolarPowerGe10Lt40kW_MWh: Double?,
    val SolarPowerGe40kW_MWh: Double?,
    val SolarPowerSelfConMWh: Double?,
    val UnknownProdMWh: Double?,
    val ExchangeNO_MWh: Double?,
    val ExchangeSE_MWh: Double?,
    val ExchangeGE_MWh: Double?,
    val ExchangeNL_MWh: Double?,
    val ExchangeGB_MWh: Double?,
    val ExchangeGreatBelt_MWh: Double?,
    val GrossConsumptionMWh: Double?,
    val GridLossTransmissionMWh: Double?,
    val GridLossInterconnectorsMWh: Double?,
    val GridLossDistributionMWh: Double?,
    val PowerToHeatMWh: Double?
)
{
    fun getField(name: String): Double? {
        return when (name) {
            "CentralPowerMWh" -> CentralPowerMWh
            "LocalPowerMWh" -> LocalPowerMWh
            "CommercialPowerMWh" -> CommercialPowerMWh
            "LocalPowerSelfConMWh" -> LocalPowerSelfConMWh
            "OffshoreWindLt100MW_MWh" -> OffshoreWindLt100MW_MWh
            "OffshoreWindGe100MW_MWh" -> OffshoreWindGe100MW_MWh
            "OnshoreWindLt50kW_MWh" -> OnshoreWindLt50kW_MWh
            "HydroPowerMWh" -> HydroPowerMWh
            "SolarPowerLt10kW_MWh" -> SolarPowerLt10kW_MWh
            "SolarPowerGe10Lt40kW_MWh" -> SolarPowerGe10Lt40kW_MWh
            "SolarPowerGe40kW_MWh" -> SolarPowerGe40kW_MWh
            "SolarPowerSelfConMWh" -> SolarPowerSelfConMWh
            "UnknownProdMWh" -> UnknownProdMWh
            "ExchangeNO_MWh" -> ExchangeNO_MWh
            "ExchangeSE_MWh" -> ExchangeSE_MWh
            "ExchangeGE_MWh" -> ExchangeGE_MWh
            "ExchangeNL_MWh" -> ExchangeNL_MWh
            "ExchangeGB_MWh" -> ExchangeGB_MWh
            "ExchangeGreatBelt_MWh" -> ExchangeGreatBelt_MWh
            "GrossConsumptionMWh" -> GrossConsumptionMWh
            "GridLossTransmissionMWh" -> GridLossTransmissionMWh
            // add more cases or use reflection below
            else -> null
        }
    }
}