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
    iataCode: String,
    cityCode: String,
    countryCode: String,
    rawLatitude: String,
    rawLongitude: String,
    rawTimeZone: String,
    locationType: String
) {

  def isAirport(): Boolean = locationType == "A"

  def city(): Try[String] = cities() match {
    case Success(city :: _) => Success(city)
    case Failure(exception) => Failure(exception)
  }

  def cities(): Try[List[String]] = cityCode.length match {

    case 3 => Success(List(cityCode))

    case x if x >= 3 =>
      cityCode.split("\\,", -1).toList.filter(_.length == 3) match {
        case Nil =>
          Failure(
            GeoBaseException(
              "No city available for airport \"" + iataCode + "\""))
        case cities => Success(cities)
      }

    case _ =>
      Failure(
        GeoBaseException("No city available for airport \"" + iataCode + "\""))
  }

  def country(): Try[String] = extract(countryCode, "country")

  def timeZone(): Try[String] = extract(rawTimeZone, "time zone")

  def latitude(): Try[Double] = extractCoordinate(rawLatitude, "latitude")

  def longitude(): Try[Double] = extractCoordinate(rawLongitude, "longitude")

  private def extract(field: String, name: String): Try[String] = field match {
    case "" =>
      Failure(
        GeoBaseException(
          "No " + name + " available for location \"" + iataCode + "\""))
    case _ => Success(field)
  }

  private def extractCoordinate(field: String, name: String): Try[Double] =
    Try(field.toDouble) match {
      case Success(coordinate) => Success(coordinate / 180d * Pi)
      case Failure(_) =>
        Failure(
          GeoBaseException(
            "No " + name + " available for location \"" + iataCode + "\""))
    }
}
