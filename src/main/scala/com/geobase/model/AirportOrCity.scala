package com.geobase.model

import com.geobase.error.GeoBaseException

import math.Pi

import scala.util.{Try, Success, Failure}

/** A geographical point such as an airport or a city.
  *
  * All dimensions in the constructor are given as String. This, in order to
  * throw exceptions on missing or wrong values during casts when methods are
  * effectively called and not when reading the data file.
  *
  * @author Xavier Guihot
  * @since 2017-01
  *
  * @param iataCode the airport or city iata code
  * @param cityCode the city code for this iataCode (only ussefull when the iata
  * code is an airport).
  * @param countryCode the country code for this iataCode
  * @param latitude the latitude for this iataCode
  * @param longitude the longitude for this iataCode
  * @param timeZone the time zone for this iataCode
  * @param locationType either "A" (airport) or "C" (city)
  */
private[geobase] final case class AirportOrCity(
	iataCode: String, cityCode: String, countryCode: String,
	latitude: String, longitude: String,
	timeZone: String, locationType: String
) {

	def isAirport(): Boolean = locationType == "A"

	def getCity(): Try[String] = getCities() match {
		case Success(city :: _) => Success(city)
		case Failure(exception) => Failure(exception)
	}

	def getCities(): Try[List[String]] = cityCode.length match {

		case 3 => Success(List(cityCode))

		case x if x >= 3 => cityCode.split("\\,", -1).toList.filter(_.length == 3) match {
			case Nil => Failure(GeoBaseException("No city available for airport \"" + iataCode + "\""))
			case cities => Success(cities)
		}

		case _ => Failure(GeoBaseException("No city available for airport \"" + iataCode + "\""))
	}

	def getCountry(): Try[String] = countryCode match {
		case "" => Failure(GeoBaseException(
		           "No country available for location \"" + iataCode + "\""))
		case _ => Success(countryCode)
	}

	def getTimeZone(): Try[String] = timeZone match {
		case "" => Failure(GeoBaseException(
			"No time zone available for location \"" + iataCode + "\""))
		case _ => Success(timeZone)
	}

	def getLongitude(): Try[Double] = Try(longitude.toDouble) match {
		case Success(longitude) => Success(longitude / 180d * Pi)
		case Failure(_) => Failure(GeoBaseException(
		                   "No longitude available for location \"" + iataCode + "\""))
	}

	def getLatitude(): Try[Double] = Try(latitude.toDouble) match {
		case Success(latitude) => Success(latitude / 180d * Pi)
		case Failure(_) => Failure(GeoBaseException(
		                   "No latitude available for location \"" + iataCode + "\""))
	}
}
