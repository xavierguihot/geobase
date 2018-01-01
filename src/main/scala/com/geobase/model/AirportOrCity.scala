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
private[geobase] case class AirportOrCity(
	iataCode: String, cityCode: String, countryCode: String,
	latitude: String, longitude: String,
	timeZone: String, locationType: String
) {

	def isAirport(): Boolean = locationType == "A"

	def getCity(): Try[String] = {

		getCities() match {
			case Success(cities) => Success(cities.head)
			case Failure(exception) => Failure(exception)
		}
	}

	def getCities(): Try[List[String]] = {

		if (cityCode.length == 3)
			Success(List(cityCode))

		// In case of an airport attached to several cities ("PHX,MSC"):
		if (cityCode.length >= 3)
			cityCode.split("\\,", -1).toList.filter(_.length == 3) match {
				case Nil => Failure(GeoBaseException("No city available for airport \"" + iataCode + "\""))
				case cities => Success(cities)
			}

		// Raws for which the city field is empty:
		else
			Failure(GeoBaseException("No city available for airport \"" + iataCode + "\""))
	}

	def getCountry(): Try[String] = {

		if (countryCode.length == 2)
			Success(countryCode)
		else
			Failure(GeoBaseException("No country available for location \"" + iataCode + "\""))
	}

	/** Returns the longitude.
	  *
	  * The raw longitude field might be empty and thus not castable.
	  */
	def getLongitude(): Try[Double] = Try(longitude.toDouble / 180 * Pi)

	/** Returns the latitude.
	  *
	  * The raw latitude field might be empty and thus not castable.
	  */
	def getLatitude(): Try[Double] = Try(latitude.toDouble / 180 * Pi)

	def getTimeZone(): Try[String] = {

		timeZone match {
			case "" => Failure(GeoBaseException("No time zone available for location \"" + iataCode + "\""))
			case _ => Success(timeZone)
		}
	}
}
