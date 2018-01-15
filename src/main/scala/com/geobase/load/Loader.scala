package com.geobase.load

import com.geobase.model.{Airline, AirportOrCity, Country}

import java.text.SimpleDateFormat

import java.util.TimeZone

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
	  * 	https://github.com/opentraveldata/opentraveldata
	  */
	def loadAirportsAndCities(): Map[String, AirportOrCity] = {

		Source.fromURL(
			getClass.getResource("/optd_por_public.csv"), "UTF-8"
		).getLines().map(
			line => line.split("\\^", -1)
		).filter(
			// Only lines for which the last date of validity is not definied:
			splittedLine => splittedLine(14).isEmpty
		).map(
			splittedLine => {

				val airportOrCityCode = splittedLine(0)

				val location = AirportOrCity(
					iataCode 	 = airportOrCityCode,
					cityCode 	 = splittedLine(36).split('|')(0),
					countryCode  = splittedLine(16),
					latitude 	 = splittedLine(8),
					longitude 	 = splittedLine(9),
					timeZone 	 = splittedLine(31),
					locationType = splittedLine(41)
				)

				(airportOrCityCode, location)
			}
		).toList.groupBy(
			_._1
		).map{
			// Since we have iata codes shared by both an airport and a city
			// (NCE for instance), we choose to only keep the airport one:
			case (airportOrCityCode, locations) =>

				val location = locations.map{
					case (airportOrCityCode, location) => location
				}.foldLeft(locations.head._2) {
					(locA, locB) => if (locA.isAirport()) locA else locB
				}

				(airportOrCityCode, location)
		}.toMap
	}

	/** Loads the country dataset */
	def loadCountries(): Map[String, Country] = {

		Source.fromURL(
			getClass.getResource("/countries.csv")
		).getLines().filter(
			line => !line.startsWith("#") // Remove the header
		).map(
			line => {

				val splittedLine = line.split("\\^", -1)

				val countryCode = splittedLine(0)

				val country = Country(
					countryCode     = countryCode,
					currencyCode 	= splittedLine(1),
					continentCode 	= splittedLine(2),
					iataZone 		= splittedLine(3)
				)

				(countryCode, country)
			}
		).toMap
	}

	/** Loads the airline dataset */
	def loadAirlines(): Map[String, Airline] = {

		Source.fromURL(
			getClass.getResource("/airlines.csv")
		).getLines().filter(
			line => !line.startsWith("#") // Remove the header
		).map(
			line => {

				val splittedLine = line.split("\\^", -1)

				val airlineCode = splittedLine(0)
				val countryCode = splittedLine(1)

				(airlineCode, Airline(airlineCode, countryCode))
			}
		).toMap
	}
}
