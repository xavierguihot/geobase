package com.geobase.model

import com.geobase.error.GeoBaseException

import scala.util.Success

import org.scalatest.FunSuite

/** Testing facility for Airline.
  *
  * @author Xavier Guihot, Chems-Eddine Ouaari
  * @since 2018-01
  */
class AirlineTest extends FunSuite {

  test("Airline to country") {

    assert(Airline("AF", "FR", "Air France").country === Success("FR"))

    // Empty country field:
    val exceptionThrown = intercept[GeoBaseException] {
      Airline("AF", "", "Air France").country.get
    }
    assert(
      exceptionThrown.getMessage === "No country available for airline \"AF\"")
  }

  test("Airline to name") {

    assert(Airline("AH", "DZ", "Air Algerie").name === Success("Air Algerie"))

    // Empty country field:
    val exceptionThrown = intercept[GeoBaseException] {
      Airline("AH", "DZ", "").name.get
    }
    assert(
      exceptionThrown.getMessage === "No name available for airline \"AH\"")
  }
}
