package com.geobase.model

import com.geobase.error.GeoBaseException

import scala.util.{Try, Success, Failure}

/** A country.
  *
  * @author Xavier Guihot
  * @since 2017-04
  */
private[geobase] final case class Country(
    countryCode: String,
    currencyCode: String,
    continentCode: String,
    iataZoneCode: String
) {

  def continent(): Try[String] = extract(continentCode, "continent")
  def iataZone(): Try[String] = extract(iataZoneCode, "iata zone")
  def currency(): Try[String] = extract(currencyCode, "currency")

  private def extract(field: String, name: String): Try[String] = field match {
    case "" =>
      Failure(
        GeoBaseException(
          "No " + name + " available for country \"" + countryCode + "\""))
    case _ => Success(field)
  }
}
