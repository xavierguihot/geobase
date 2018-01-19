package com.geobase.model

import com.geobase.error.GeoBaseException

import org.scalatest.FunSuite

/** Testing facility for Airline.
  *
  * @author Xavier Guihot
  * @since 2018-01
  */
class AirlineTest extends FunSuite {

  test("Country Getter") {

    assert(Airline("AF", "FR").getCountry().get === "FR")

    // Empty country field:
    val exceptionThrown = intercept[GeoBaseException] {
      Airline("AF", "").getCountry().get
    }
    assert(
      exceptionThrown.getMessage === "No country available for airline \"AF\"")
  }
}
