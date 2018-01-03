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

	test("Continent Getter") {

		assert(Country("FR", "", "EU", "").getContinent() === Success("EU"))

		// Empty continent field:
		val exceptionThrown = intercept[GeoBaseException] {
			Country("FR", "", "", "").getContinent().get
		}
		assert(exceptionThrown.getMessage === "No continent available for country \"FR\"")
	}

	test("Iata Zone Getter") {

		assert(Country("FR", "", "", "21").getIataZone() === Success("21"))

		// Empty iata zone field:
		val exceptionThrown = intercept[GeoBaseException] {
			Country("FR", "", "", "").getIataZone().get
		}
		assert(exceptionThrown.getMessage === "No iata zone available for country \"FR\"")
	}

	test("Currency Getter") {

		assert(Country("FR", "EUR", "", "").getCurrency() === Success("EUR"))

		// Empty currency field:
		val exceptionThrown = intercept[GeoBaseException] {
			Country("FR", "", "", "").getCurrency().get
		}
		assert(exceptionThrown.getMessage === "No currency available for country \"FR\"")
	}
}
