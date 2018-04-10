package com.geobase.load

import com.geobase.model.{Airline, AirportOrCity, Country}

import scala.io.Source

/** Functions called when initializating GeoBase (data loads).
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

    Source
      .fromURL(getClass.getResource("/optd_por_public.csv"), "UTF-8")
      .getLines()
      .map(_.split("\\^", -1))
      // Only lines for which the last date of validity is not definied:
      .filter(_(14).isEmpty)
      .map(splitLine => {

        val airportOrCityCode = splitLine(0)

        val location = AirportOrCity(
          iataCode = airportOrCityCode,
          cityCode = splitLine(36).split('|')(0),
          countryCode = splitLine(16),
          rawLatitude = splitLine(8),
          rawLongitude = splitLine(9),
          rawTimeZone = splitLine(31),
          locationType = splitLine(41)
        )

        (airportOrCityCode, location)
      })
      .toList
      .groupBy(_._1)
      .map {
        // Since we have iata codes shared by both an airport and a city
        // (NCE for instance), we choose to only keep the airport one:
        case (airportOrCityCode, locations) => {

          val location = locations.map {
            case (airportOrCityCode, location) => location
          }.foldLeft(locations.head._2) {
            case (locA, locB) => if (locA.isAirport()) locA else locB
          }

          (airportOrCityCode, location)
        }
      }
      .toMap
  }

  /** Loads the country dataset */
  def loadCountries(): Map[String, Country] = {

    Source
      .fromURL(getClass.getResource("/countries.csv"))
      .getLines()
      .filter(!_.startsWith("#")) // Remove the header
      .map(line => {

        val splitLine = line.split("\\^", -1)

        val countryCode = splitLine(0)

        val country = Country(
          countryCode = countryCode,
          currencyCode = splitLine(1),
          continentCode = splitLine(2),
          iataZoneCode = splitLine(3))

        (countryCode, country)
      })
      .toMap
  }

  /** Loads the airline dataset */
  def loadAirlines(): Map[String, Airline] = {

    // Airline code to airline name:
    val airlineNames = Source
      .fromURL(getClass.getResource("/optd_airlines.csv"))
      .getLines()
      .filter(!_.startsWith("pk^")) // Remove the header
      .map(line => {

        val splitLine = line.split("\\^", -1)

        val airlineCode = splitLine(5)
        val airlineName = splitLine(7)

        Airline(airlineCode, "", airlineName)
      })
      .toList

    // Airline code to country:
    val airlineCountries = Source
      .fromURL(getClass.getResource("/airlines.csv"))
      .getLines()
      .filter(!_.startsWith("#")) // Remove the header
      .map(line => {

        val splitLine = line.split("\\^", -1)

        val airlineCode = splitLine(0)
        val countryCode = splitLine(1)

        Airline(airlineCode, countryCode, "")
      })
      .toList

    (airlineNames ::: airlineCountries).groupBy {
      case Airline(code, _, _) => code
    }.map {
      // Only airline name or country available:
      case (code, List(airline)) => (code, airline)
      // Both country and at least one name available (when an airline has
      // several names, we take the first one):
      case (code, (Airline(_, "", name) :: _) :+ Airline(_, country, "")) =>
        (code, Airline(code, country, name))
      // No information on the country, but several names available:
      case (code, Airline(_, "", name) :: _) => (code, Airline(code, "", name))
    }.toMap
  }
}
