package com.geobase.model

import com.geobase.error.GeoBaseException

import scala.util.Success

import org.scalatest.FunSuite

/** Testing facility for Airline.
  *
  * @author Xavier Guihot
  * @since 2018-01
  */
class AirlineNameTest extends FunSuite {

  test("Airline to name") {

    assert(AirlineName("AH", "Air Algerie").name === Success("Air Algerie"))

    // Empty country field:
    val exceptionThrown = intercept[GeoBaseException] {
      AirlineName("AH", "").name.get
    }
    assert(
      exceptionThrown.getMessage === "No name available for airline \"AH\"")
  }
}
