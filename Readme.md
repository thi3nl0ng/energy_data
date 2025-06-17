A. Analyze and specify the requirements
1. Get data from provided APi 
The API call is in this format
  - https://api.energidataservice.dk/dataset/ProductionConsumptionSettlement?offset=0&limit=100&start=2025-06-07T00:00&end=2025-06-17T00:00&sort=HourUTC%20DESC
  - Inspect the api, I do see it is with the following query params:
    1. offset (start from 0)
    2. limit (default 100)
    3. start: datetime
    4. end: datetime
    5. sort: default by "HourUTC DESC"
The datetime format for [end] and [start] - valid format: yyyy, yyyy-MM, yyyy-MM-dd, yyyy-MM-ddTHH:mm.
After checking the API, I know that the response if in this format, that we need to know to code the model

<details> <summary><strong>Data response</strong></summary>
{
  "total": 358240,
  "sort": "HourUTC DESC",
  "limit": 100,
  "dataset": "ProductionConsumptionSettlement",
  "records": [
    {
      "HourUTC": "2025-06-06T04:00:00",
      "HourDK": "2025-06-06T06:00:00",
      "PriceArea": "DK1",
      "CentralPowerMWh": 5.108670,
      "LocalPowerMWh": 95.071576,
      "CommercialPowerMWh": 63.143923,
      "LocalPowerSelfConMWh": 52.683792,
      "OffshoreWindLt100MW_MWh": 110.087382,
      "OffshoreWindGe100MW_MWh": 453.699088,
      "OnshoreWindLt50kW_MWh": 1.320471,
      "OnshoreWindGe50kW_MWh": 503.008770,
      "HydroPowerMWh": 1.202952,
      "SolarPowerLt10kW_MWh": 1.766157,
      "SolarPowerGe10Lt40kW_MWh": 0.357542,
      "SolarPowerGe40kW_MWh": 58.275288,
      "SolarPowerSelfConMWh": 3.152441,
      "UnknownProdMWh": 0.594790,
      "ExchangeNO_MWh": -243.407600,
      "ExchangeSE_MWh": 605.672000,
      "ExchangeGE_MWh": 1560.030000,
      "ExchangeNL_MWh": 1.611000,
      "ExchangeGB_MWh": -986.425988,
      "ExchangeGreatBelt_MWh": -0.300000,
      "GrossConsumptionMWh": 2286.652254,
      "GridLossTransmissionMWh": 67.555762,
      "GridLossInterconnectorsMWh": 8.446600,
      "GridLossDistributionMWh": 68.872520,
      "PowerToHeatMWh": 65.256634
    }
  ]
}

</details>

2. Data type of properties / attributes
Checking the meta data in this  meta API https://api.energidataservice.dk/meta/dataset/ProductionConsumptionSettlement
So it is useful for set datatype of attributes

3. Requirements
- API in Kotlin / Java
- Reads data from this API in real time
- Generates average values for the last 5 minutes
- This is done every 1 minute
- Assume that the result is to be used as input in a ML model

B. Solution
1. We need to setup a scheduled job run every 1 minute in Kotlin
2. The task is doing the following action:
- Query and get the latest data from the last call
- Calculate the average value (on attributes we choose) in the 5 minutes 
- The result will be saved into .csv file in a folder with pre-defined format with datetime
- Provide a simple chart based on that result
- Set up configuration in file for robust and dynamic. The attributes for computing average are also configurable
- I use a param dataBuffer to temporally store, not database. In real context, can have other methods


- Attention/Note: 
  1. Timezone (datetime) in the data is in UTC (the string value is without "Z" at the ending)
  2. The datetime of data is not realtime, mean, when we try to get the date without any filter (17.06.2025, latest data is 08.06.2025), it is like 8/9 days ahead
  so get the last 5 minutes data mean from the HourUTC   
  3. Because of limitation of time, I do not organize the code perfectly, deal with number precision, optimize or write test and add log and error handling

3. How: when the task runs, it just gets the first row to get the latest updated time, then set cut off to 5 minutes, 
then based on those values, will make another real call to get the real data (5 minutes update data)
- Then calculate the average on that result
- Save it to file (.csv) for further processing (feks. feed to ML)
- A simple chart is provided in Visualization.kt. It can be set in another process, but I make it simple for demo, then  by calling from the main

4. The project has 5 main .kt files 
- Main.kt
- ConfigLoader.kt
- PostProcessing.kt
- ResponseModel.kt
- Visualization.kt
- Build by running in terminal:  .\gradlew.bat build 
- Run by .\gradlew.bat run 

When run, data is calculated, and save to a file in data folder every 1 minute, example of data saved as in \data\averages\20250617\averages.csv

<details> <summary><strong>Example data in csv file</strong></summary>
runtime,timestamp,CentralPowerMWh,LocalPowerMWh,CommercialPowerMWh
2025-06-17T16:18,2025-06-08T06:00,23.051769999999998,52.6545175,57.007275
2025-06-17T16:19,2025-06-08T06:00,23.051769999999998,52.6545175,57.007275
2025-06-17T16:37,2025-06-08T06:00,23.051769999999998,52.6545175,57.007275
2025-06-17T16:43,2025-06-08T06:00,23.051769999999998,52.6545175,57.007275
</details>

Chart: is using xchart, and example is "data\example_chart.png"