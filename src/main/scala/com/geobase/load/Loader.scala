package com.geobase.load

import com.geobase.model.{Airline, AirportOrCity, Country}

import scala.io.Source

/** Functions called when initializing GeoBase (data loads).
  *
  * @author Xavier Guihot
  * @since 2017-01
  */
private[geobase] object Loader {

  /** Loads airports and cities from opentraveldata data file.
    *
    * Data source:
    *   https://github.com/opentraveldata/opentraveldata
    */
  def loadAirportsAndCities(): Map[String, AirportOrCity] = {

    println("GeoBase: Loading airports and cities")

    Source
      .fromURL(getClass.getResource("/optd_por_public.csv"), "UTF-8")
      .getLines
      .map(_.split("\\^", -1))
      .collect {
        // Only lines for which the last date of validity is not defined:
        case splitLine if splitLine(14).isEmpty =>
          AirportOrCity(
            iataCode     = splitLine(0),
            cityCode     = splitLine(36).split('|')(0),
            countryCode  = splitLine(16),
            rawLatitude  = splitLine(8),
            rawLongitude = splitLine(9),
            rawTimeZone  = splitLine(31),
            locationType = splitLine(41)
          )
      }
      .toList
      .groupBy(_.iataCode)
      // Since we have iata codes shared by both an airport and a city (NCE for instance), we choose
      // to only keep the airport one:
      .mapValues { _.reduceLeft((locA, locB) => if (locA.isAirport) locA else locB) }
      .map(identity) // Serialization
  }

  def loadCountries(): Map[String, Country] = {

    println("GeoBase: Loading countries")

    Source
      .fromURL(getClass.getResource("/countries.csv"))
      .getLines
      .drop(1) // Remove the header
      .map { line =>
        val splitLine = line.split("\\^", -1)

        val countryCode = splitLine(0)

        val country = Country(
          countryCode   = countryCode,
          currencyCode  = splitLine(1),
          continentCode = splitLine(2),
          iataZoneCode  = splitLine(3)
        )

        countryCode -> country
      }
      .toMap
  }

  def loadAirlines(): Map[String, Airline] = {

    println("GeoBase: Loading airlines")

    // Airline code to airline name:
    val airlineNames =
      Source
        .fromURL(getClass.getResource("/optd_airlines.csv"))
        .getLines
        .drop(1) // Remove the header
        .map { line =>
          val splitLine = line.split("\\^", -1)

          val airlineCode = splitLine(5)
          val airlineName = splitLine(7)

          Airline(airlineCode, "", airlineName)
        }
        .toList

    // Airline code to country:
    val airlineCountries =
      Source
        .fromURL(getClass.getResource("/airlines.csv"))
        .getLines
        .drop(1) // Remove the header
        .map { line =>
          val splitLine = line.split("\\^", -1)

          val airlineCode = splitLine(0)
          val countryCode = splitLine(1)

          Airline(airlineCode, countryCode, "")
        }
        .toList

    (airlineNames ::: airlineCountries)
      .groupBy(_.airlineCode)
      .mapValues { airlines =>
        val code    = airlines.head.airlineCode
        val name    = airlines.find(_.airlineName.nonEmpty).fold("")(_.airlineName)
        val country = airlines.find(_.countryCode.nonEmpty).fold("")(_.countryCode)
        Airline(code, country, name)
      }
      .map(identity) // Serialization
  }
}
