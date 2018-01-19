package com.geobase.model

import com.geobase.error.GeoBaseException

import scala.util.Success

import org.scalatest.FunSuite

/** Testing facility for AirportOrCity.
  *
  * @author Xavier Guihot
  * @since 2018-01
  */
class AirportOrCityTest extends FunSuite {

  test("City Getter") {

    // Norm:
    var city = AirportOrCity("ORY", "PAR", "", "", "", "", "").getCity()
    assert(city === Success("PAR"))

    // Shared airport between cities:
    city = AirportOrCity("AZA", "PHX,MSC", "", "", "", "", "").getCity()
    assert(city === Success("PHX"))

    // Empty country field:
    val exceptionThrown = intercept[GeoBaseException] {
      AirportOrCity("ORY", "", "", "", "", "", "").getCity().get
    }
    assert(
      exceptionThrown.getMessage === "No city available for airport \"ORY\"")
  }

  test("Cities Getter") {

    // Norm:
    var cities = AirportOrCity("ORY", "PAR", "", "", "", "", "").getCities()
    assert(cities === Success(List("PAR")))

    // Shared airport between cities:
    cities = AirportOrCity("AZA", "PHX,MSC", "", "", "", "", "").getCities()
    assert(cities === Success(List("PHX", "MSC")))

    // Empty country field:
    val exceptionThrown = intercept[GeoBaseException] {
      AirportOrCity("ORY", "", "", "", "", "", "").getCities().get
    }
    assert(
      exceptionThrown.getMessage === "No city available for airport \"ORY\"")

    // Shared airport between cities, but with an empty city:
    cities = AirportOrCity("AZA", "PHX,MSC,", "", "", "", "", "").getCities()
    assert(cities === Success(List("PHX", "MSC")))
    cities = AirportOrCity("AZA", "PHX,,MSC", "", "", "", "", "").getCities()
    assert(cities === Success(List("PHX", "MSC")))
  }

  test("Country Getter") {

    // Norm:
    val country = AirportOrCity("ORY", "", "FR", "", "", "", "").getCountry()
    assert(country === Success("FR"))

    // Empty country field:
    val exceptionThrown = intercept[GeoBaseException] {
      AirportOrCity("ORY", "", "", "", "", "", "").getCountry().get
    }
    assert(
      exceptionThrown.getMessage === "No country available for location \"ORY\"")
  }

  test("Time Zone Getter") {

    // Norm:
    val location = AirportOrCity("ORY", "", "", "", "", "Europe/Paris", "")
    assert(location.getTimeZone() === Success("Europe/Paris"))

    // Empty country field:
    val exceptionThrown = intercept[GeoBaseException] {
      AirportOrCity("ORY", "", "", "", "", "", "").getCountry().get
    }
    assert(
      exceptionThrown.getMessage === "No country available for location \"ORY\"")
  }
}
