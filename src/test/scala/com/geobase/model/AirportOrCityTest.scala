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

  test("Airport to city") {

    // Norm:
    var city = AirportOrCity("ORY", "PAR", "", "", "", "", "").city
    assert(city === Success("PAR"))

    // Shared airport between cities:
    city = AirportOrCity("AZA", "PHX,MSC", "", "", "", "", "").city
    assert(city === Success("PHX"))

    // Empty country field:
    val exceptionThrown = intercept[GeoBaseException] {
      AirportOrCity("ORY", "", "", "", "", "", "").city.get
    }
    assert(exceptionThrown.getMessage === "No city available for airport \"ORY\"")
  }

  test("Airport to cities") {

    // Norm:
    var cities = AirportOrCity("ORY", "PAR", "", "", "", "", "").cities
    assert(cities === Success(List("PAR")))

    // Shared airport between cities:
    cities = AirportOrCity("AZA", "PHX,MSC", "", "", "", "", "").cities
    assert(cities === Success(List("PHX", "MSC")))

    // Empty country field:
    val exceptionThrown = intercept[GeoBaseException] {
      AirportOrCity("ORY", "", "", "", "", "", "").cities.get
    }
    assert(exceptionThrown.getMessage === "No city available for airport \"ORY\"")

    // Shared airport between cities, but with an empty city:
    cities = AirportOrCity("AZA", "PHX,MSC,", "", "", "", "", "").cities
    assert(cities === Success(List("PHX", "MSC")))
    cities = AirportOrCity("AZA", "PHX,,MSC", "", "", "", "", "").cities
    assert(cities === Success(List("PHX", "MSC")))
  }

  test("Location to country") {

    // Norm:
    val country = AirportOrCity("ORY", "", "FR", "", "", "", "").country
    assert(country === Success("FR"))

    // Empty country field:
    val exceptionThrown = intercept[GeoBaseException] {
      AirportOrCity("ORY", "", "", "", "", "", "").country.get
    }
    assert(exceptionThrown.getMessage === "No country available for location \"ORY\"")
  }

  test("Location to time zone") {

    // Norm:
    val location = AirportOrCity("ORY", "", "", "", "", "Europe/Paris", "")
    assert(location.timeZone === Success("Europe/Paris"))

    // Empty country field:
    val exceptionThrown = intercept[GeoBaseException] {
      AirportOrCity("ORY", "", "", "", "", "", "").timeZone.get
    }
    assert(exceptionThrown.getMessage === "No time zone available for location \"ORY\"")
  }

  test("Location to latitude/longitude") {

    // Norm:
    val location = AirportOrCity("ORY", "", "", "31.95063", "-85.12718", "", "")
    assert(location.latitude === Success(0.5576436915864759d))
    assert(location.longitude === Success(-1.4857495739378663d))

    // Empty country field:
    val exceptionThrownLat = intercept[GeoBaseException] {
      AirportOrCity("ORY", "", "", "", "", "", "").latitude.get
    }
    assert(exceptionThrownLat.getMessage === "No latitude available for location \"ORY\"")
    val exceptionThrownLon = intercept[GeoBaseException] {
      AirportOrCity("ORY", "", "", "", "", "", "").longitude.get
    }
    assert(exceptionThrownLon.getMessage === "No longitude available for location \"ORY\"")
  }
}
