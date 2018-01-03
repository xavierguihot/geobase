package com.geobase.model

import com.geobase.error.GeoBaseException

import scala.util.{Try, Success, Failure}

/** A country.
  *
  * @author Xavier Guihot
  * @since 2017-04
  */
private[geobase] final case class Country(
	countryCode: String, currencyCode: String, continentCode: String, iataZone: String
) {

	def getContinent(): Try[String] = {
		continentCode match {
			case "" => Failure(GeoBaseException(
				"No continent available for country \"" + countryCode + "\""
			))
			case _ => Success(continentCode)
		}
	}

	def getIataZone(): Try[String] = {
		iataZone match {
			case "" => Failure(GeoBaseException(
				"No iata zone available for country \"" + countryCode + "\""
			))
			case _ => Success(iataZone) // Non-empty string
		}
	}

	def getCurrency(): Try[String] = {
		currencyCode match {
			case "" => Failure(GeoBaseException(
				"No currency available for country \"" + countryCode + "\""
			))
			case _ => Success(currencyCode) // Non-empty string
		}
	}
}
