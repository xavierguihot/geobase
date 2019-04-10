package com.geobase.model

import com.geobase.error.GeoBaseException

import scala.util.Success

import org.scalatest.FunSuite

/** Testing facility for Country.
  *
  * @author Xavier Guihot
  * @since 2018-01
  */
class CountryTest extends FunSuite {

  test("Country to continent") {

    assert(Country("FR", "", "EU", "").continent === Success("EU"))

    // Empty continent field:
    val exceptionThrown = intercept[GeoBaseException] {
      Country("FR", "", "", "").continent.get
    }
    assert(exceptionThrown.getMessage === "No continent available for country \"FR\"")
  }

  test("Country to iata zone") {

    assert(Country("FR", "", "", "21").iataZone === Success("21"))

    // Empty iata zone field:
    val exceptionThrown = intercept[GeoBaseException] {
      Country("FR", "", "", "").iataZone.get
    }
    assert(exceptionThrown.getMessage === "No iata zone available for country \"FR\"")
  }

  test("Country to currency") {

    assert(Country("FR", "EUR", "", "").currency === Success("EUR"))

    // Empty currency field:
    val exceptionThrown = intercept[GeoBaseException] {
      Country("FR", "", "", "").currency.get
    }
    assert(exceptionThrown.getMessage === "No currency available for country \"FR\"")
  }
}
