package com.geobase.model

import com.geobase.error.GeoBaseException

import scala.util.Success

import org.scalatest.FunSuite

/** Testing facility for Airline.
  *
  * @author Xavier Guihot
  * @since 2018-01
  */
class AirlineTest extends FunSuite {

  test("Airline to country") {

    assert(Airline("AF", "FR").country === Success("FR"))

    // Empty country field:
    val exceptionThrown = intercept[GeoBaseException] {
      Airline("AF", "").country.get
    }
    assert(
      exceptionThrown.getMessage === "No country available for airline \"AF\"")
  }
}
