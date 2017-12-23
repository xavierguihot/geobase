package com.geobase.model

import math.Pi

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

	/** Return the longitude */
	def getLongitude(): Option[Double] = {
		try {
			Some(longitude.toDouble / 180 * Pi)
		} catch {
			// The raw longitude field might be empty and thus not castable:
			case nfe: NumberFormatException => None
		}
	}

	/** Return the latitude */
	def getLatitude(): Option[Double] = {
		try {
			Some(latitude.toDouble / 180 * Pi)
		} catch {
			// The raw latitude field might be empty and thus not castable:
			case nfe: NumberFormatException => None
		}
	}
}
