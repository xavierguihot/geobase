package com.geobase.model

import com.geobase.error.GeoBaseException

import scala.util.{Try, Success, Failure}

/** An airline.
  *
  * @author Xavier Guihot, Chems-Eddine Ouaari
  * @since 2017-04
  */
private[geobase] final case class Airline(
    airlineCode: String,
    countryCode: String,
    airlineName: String
) {

  def country: Try[String] = countryCode match {
    case "" =>
      Failure(
        GeoBaseException(
          "No country available for airline \"" + airlineCode + "\""))
    case _ => Success(countryCode)
  }

  def name: Try[String] = airlineName match {
    case "" =>
      Failure(
        GeoBaseException(
          "No name available for airline \"" + airlineCode + "\""))
    case _ => Success(airlineName)
  }
}
