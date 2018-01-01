package com.geobase.model

import com.geobase.error.GeoBaseException

import scala.util.{Try, Success, Failure}

/** A country.
  *
  * @author Xavier Guihot
  * @since 2017-04
  */
private[geobase] case class Country(
	countryCode: String, currencyCode: String, continentCode: String, iataZone: String
) {

	def getContinent(): Try[String] = {

		if (continentCode.length == 2)
			Success(continentCode)

		else {
			val exceptionMessage =
				"No continent available for country \"" + countryCode + "\""
			Failure(GeoBaseException(exceptionMessage))
		}
	}

	def getIataZone(): Try[String] = {

		if (iataZone.length == 2)
			Success(iataZone)

		else {
			val exceptionMessage =
				"No iata zone available for country \"" + countryCode + "\""
			Failure(GeoBaseException(exceptionMessage))
		}
	}

	def getCurrency(): Try[String] = {

		if (currencyCode.length == 3)
			Success(currencyCode)

		else {
			val exceptionMessage =
				"No currency available for country \"" + countryCode + "\""
			Failure(GeoBaseException(exceptionMessage))
		}
	}
}
