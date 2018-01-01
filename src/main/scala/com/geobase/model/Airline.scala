package com.geobase.model

import com.geobase.error.GeoBaseException

import scala.util.{Try, Success, Failure}

/** An airline.
  *
  * @author Xavier Guihot
  * @since 2017-04
  */
private[geobase] case class Airline(airlineCode: String, countryCode: String) {

	def getCountry(): Try[String] = {

		countryCode match {

			// Airline raws for which the country field is empty:
			case "" => { 
				val errorMessage =
					"No country available for airline \"" + airlineCode + "\""
				Failure(GeoBaseException(errorMessage))
			}

			case _ => Success(countryCode)
		}
	}
}
