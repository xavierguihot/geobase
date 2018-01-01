package com.geobase.model

import com.geobase.error.GeoBaseException

import org.scalatest.FunSuite

/** Testing facility for AirportOrCity.
  *
  * @author Xavier Guihot
  * @since 2018-01
  */
class AirportOrCityTest extends FunSuite {

	test("City Getter") {

		// Norm:
		assert(AirportOrCity("ORY", "PAR", "", "", "", "", "").getCity().get === "PAR")

		// Shared airport between cities:
		assert(AirportOrCity("AZA", "PHX,MSC", "", "", "", "", "").getCity().get === "PHX")

		// Empty country field:
		val exceptionThrown = intercept[GeoBaseException] {
			AirportOrCity("ORY", "", "", "", "", "", "").getCity().get
		}
		assert(exceptionThrown.getMessage === "No city available for airport \"ORY\"")
	}

	test("Cities Getter") {

		// Norm:
		assert(AirportOrCity("ORY", "PAR", "", "", "", "", "").getCities().get === List("PAR"))

		// Shared airport between cities:
		var location = AirportOrCity("AZA", "PHX,MSC", "", "", "", "", "")
		assert(location.getCities().get === List("PHX", "MSC"))

		// Empty country field:
		val exceptionThrown = intercept[GeoBaseException] {
			AirportOrCity("ORY", "", "", "", "", "", "").getCities().get
		}
		assert(exceptionThrown.getMessage === "No city available for airport \"ORY\"")

		// Shared airport between cities, but with an empty city:
		location = AirportOrCity("AZA", "PHX,MSC,", "", "", "", "", "")
		assert(location.getCities().get === List("PHX", "MSC"))
		location = AirportOrCity("AZA", "PHX,,MSC", "", "", "", "", "")
		assert(location.getCities().get === List("PHX", "MSC"))
	}

	test("Country Getter") {

		// Norm:
		assert(AirportOrCity("ORY", "", "FR", "", "", "", "").getCountry().get === "FR")

		// Empty country field:
		val exceptionThrown = intercept[GeoBaseException] {
			AirportOrCity("ORY", "", "", "", "", "", "").getCountry().get
		}
		assert(exceptionThrown.getMessage === "No country available for location \"ORY\"")
	}

	test("Time Zone Getter") {

		// Norm:
		val location = AirportOrCity("ORY", "", "", "", "", "Europe/Paris", "")
		assert(location.getTimeZone().get === "Europe/Paris")

		// Empty country field:
		val exceptionThrown = intercept[GeoBaseException] {
			AirportOrCity("ORY", "", "", "", "", "", "").getCountry().get
		}
		assert(exceptionThrown.getMessage === "No country available for location \"ORY\"")
	}
}
