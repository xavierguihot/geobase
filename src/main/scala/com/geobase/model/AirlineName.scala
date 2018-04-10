package com.geobase.model

import com.geobase.error.GeoBaseException

import scala.util.{Try, Success, Failure}

/** An airline with its name.
  *
  * @author Chems-Eddine Ouaari
  * @since 2018-04
  */
private[geobase] final case class AirlineName(
    airlineCode: String,
    airlineName: String
) {

  def name(): Try[String] = airlineName match {
    case "" =>
      Failure(
        GeoBaseException(
          "No name available for airline \"" + airlineCode + "\""))
    case _ => Success(airlineName)
  }
}
