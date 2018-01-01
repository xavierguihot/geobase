package com.geobase

import com.geobase.error.GeoBaseException
import com.geobase.model.{Airline, AirportOrCity, Country}

import org.scalatest.FunSuite
import org.scalatest.PrivateMethodTester

import java.security.InvalidParameterException

/** Testing facility for GeoBase.
  *
  * @author Xavier Guihot
  * @since 2016-05
  */
class GeoBaseTest extends FunSuite with PrivateMethodTester {

	val geoBase = new GeoBase()

	test("Airport to City Getter") {

		// Basic cases:
		assert(geoBase.getCityFor("ORY").get === "PAR")
		assert(geoBase.getCityFor("CDG").get === "PAR")
		assert(geoBase.getCityFor("JFK").get === "NYC")
		assert(geoBase.getCityFor("NCE").get === "NCE")

		// Case where several cities are given for one airport:
		assert(geoBase.getCityFor("AZA").get === "PHX") // PHX,MSC

		// Unknown airport:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.getCityFor("...").get
		}
		assert(exceptionThrown.getMessage === "Unknown airport \"...\"")

		// A city as input will return the city itself:
		assert(geoBase.getCityFor("PAR").get === "PAR")
	}

	test("Airport to Cities Getter") {

		// Basic cases:
		assert(geoBase.getCitiesFor("ORY").get === List("PAR"))
		assert(geoBase.getCitiesFor("CDG").get === List("PAR"))
		assert(geoBase.getCitiesFor("JFK").get === List("NYC"))
		assert(geoBase.getCitiesFor("NCE").get === List("NCE"))

		// Case where several cities are given for one airport:
		assert(geoBase.getCitiesFor("AZA").get === List("PHX", "MSC"))

		// Unknown airport:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.getCitiesFor("...").get
		}
		assert(exceptionThrown.getMessage === "Unknown airport \"...\"")

		// A city as input will return the city itself:
		assert(geoBase.getCitiesFor("PAR").get === List("PAR"))
	}

	test("Airport/City to Country Getter") {

		assert(geoBase.getCountryFor("ORY").get === "FR")
		assert(geoBase.getCountryFor("CDG").get === "FR")
		assert(geoBase.getCountryFor("JFK").get === "US")
		assert(geoBase.getCountryFor("NCE").get === "FR")
		assert(geoBase.getCountryFor("PAR").get === "FR")
		assert(geoBase.getCountryFor("FR").get === "FR")

		// Unknown airport/city:
		var exceptionThrown = intercept[GeoBaseException] {
			geoBase.getCountryFor("...").get
		}
		assert(exceptionThrown.getMessage === "Unknown location \"...\"")

		// Unkown country:
		exceptionThrown = intercept[GeoBaseException] {
			geoBase.getCountryFor("").get
		}
		assert(exceptionThrown.getMessage === "Unknown location \"\"")
	}

	test("Location to Continent Getter") {

		// Various input location types (airport, city, country):
		assert(geoBase.getContinentFor("ORY").get === "EU")
		assert(geoBase.getContinentFor("LON").get === "EU")
		assert(geoBase.getContinentFor("FR").get === "EU")
		assert(geoBase.getContinentFor("JFK").get === "NA")
		assert(geoBase.getContinentFor("AU").get === "OC")
		assert(geoBase.getContinentFor("HK").get === "AS")
		assert(geoBase.getContinentFor("BUE").get === "SA")
		assert(geoBase.getContinentFor("AN").get === "NA")
		assert(geoBase.getContinentFor("ZA").get === "AF")

		// Unknown location:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.getContinentFor("..").get
		}
		assert(exceptionThrown.getMessage === "Unknown country \"..\"")
	}

	test("Location to IATA Zone Getter") {

		// Various input location types (airport, city, country):
		assert(geoBase.getIataZoneFor("ORY").get === "21")
		assert(geoBase.getIataZoneFor("LON").get === "21")
		assert(geoBase.getIataZoneFor("FR").get === "21")
		assert(geoBase.getIataZoneFor("JFK").get === "11")
		assert(geoBase.getIataZoneFor("AU").get === "32")
		assert(geoBase.getIataZoneFor("HK").get === "31")
		assert(geoBase.getIataZoneFor("BUE").get === "13")
		assert(geoBase.getIataZoneFor("ZA").get === "23")

		// Unknown location:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.getIataZoneFor("..").get
		}
		assert(exceptionThrown.getMessage === "Unknown country \"..\"")
	}

	test("Location to Currency Getter") {

		// Various input location types (airport, city, country):
		assert(geoBase.getCurrencyFor("PAR").get === "EUR")
		assert(geoBase.getCurrencyFor("JFK").get === "USD")
		assert(geoBase.getCurrencyFor("FR").get === "EUR")
		assert(geoBase.getCurrencyFor("AU").get === "AUD")

		// Unknown location:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.getCurrencyFor("..").get
		}
		assert(exceptionThrown.getMessage === "Unknown country \"..\"")
	}

	test("Airline to Country Getter") {

		assert(geoBase.getCountryForAirline("BA").get === "GB")
		assert(geoBase.getCountryForAirline("AF").get === "FR")
		assert(geoBase.getCountryForAirline("AA").get === "US")
		assert(geoBase.getCountryForAirline("LH").get === "DE")

		// Unknown airline:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.getCountryForAirline("..").get
		}
		assert(exceptionThrown.getMessage === "Unknown airline \"..\"")
	}

	test("Distance Getter") {

		assert(geoBase.getDistanceBetween("ORY", "NCE").get === 676)
		assert(geoBase.getDistanceBetween("NCE", "ORY").get === 676)

		assert(geoBase.getDistanceBetween("ORY", "CDG").get === 35)

		assert(geoBase.getDistanceBetween("ORY", "ORY").get === 0)

		// Unknown airport/city:
		var exceptionThrown = intercept[GeoBaseException] {
			geoBase.getDistanceBetween("...", "CDG").get
		}
		assert(exceptionThrown.getMessage === "Unknown location \"...\"")

		exceptionThrown = intercept[GeoBaseException] {
			geoBase.getDistanceBetween("CDG", "...").get
		}
		assert(exceptionThrown.getMessage === "Unknown location \"...\"")
	}

	test("Geo Type Getter (domestic, conti., interconti.)") {

		assert(geoBase.getGeoType(List("CDG", "ORY")).get === GeoType.DOMESTIC)

		assert(geoBase.getGeoType(List("CDG", "PAR")).get === GeoType.DOMESTIC)
		assert(geoBase.getGeoType(List("CDG", "NCE", "NCE", "TLS")).get === GeoType.DOMESTIC)
		assert(geoBase.getGeoType(List("CDG", "NCE", "NCE", "CDG")).get === GeoType.DOMESTIC)

		assert(geoBase.getGeoType(List("CDG", "FRA")).get === GeoType.CONTINENTAL)
		assert(geoBase.getGeoType(List("CDG", "TLS", "DUB", "FRA")).get === GeoType.CONTINENTAL)
		assert(geoBase.getGeoType(List("CDG", "TLS", "DUB", "FRA", "CDG")).get === GeoType.CONTINENTAL)

		assert(geoBase.getGeoType(List("CDG", "JFK")).get === GeoType.INTER_CONTINENTAL)
		assert(geoBase.getGeoType(List("PAR", "JFK")).get === GeoType.INTER_CONTINENTAL)
		assert(geoBase.getGeoType(List("PAR", "NYC")).get === GeoType.INTER_CONTINENTAL)
		assert(geoBase.getGeoType(List("CDG", "TLS", "JFK", "MEX")).get === GeoType.INTER_CONTINENTAL)

		assert(geoBase.getGeoType(List("FR", "FR")).get === GeoType.DOMESTIC)
		assert(geoBase.getGeoType(List("FR", "FR", "DE")).get === GeoType.CONTINENTAL)
		assert(geoBase.getGeoType(List("FR", "GB", "DE")).get === GeoType.CONTINENTAL)
		assert(geoBase.getGeoType(List("FR", "PAR", "DUB")).get === GeoType.CONTINENTAL)
		assert(geoBase.getGeoType(List("US", "PAR", "DUB")).get === GeoType.INTER_CONTINENTAL)

		// Empty list of airports/cities:
		val invalidExceptionThrown = intercept[InvalidParameterException] {
			geoBase.getGeoType(List("CDG")).get
		}
		val invalidExpectedMessage = (
			"GeoBase needs at least 2 locations to compute a Geography Type"
		)
		assert(invalidExceptionThrown.getMessage === invalidExpectedMessage)

		// Unknown airport/city:
		var exceptionThrown = intercept[GeoBaseException] {
			geoBase.getGeoType(List("CDG", "...")).get
		}
		assert(exceptionThrown.getMessage === "Unknown location \"...\"")
		exceptionThrown = intercept[GeoBaseException] {
			geoBase.getGeoType(List("US", "CDG", "NCE", "aaa")).get
		}
		assert(exceptionThrown.getMessage === "Unknown location \"aaa\"")
		exceptionThrown = intercept[GeoBaseException] {
			geoBase.getGeoType(List("US", "bbb", "NCE", "aaa")).get
		}
		assert(exceptionThrown.getMessage === "Unknown locations \"bbb\", \"aaa\"")

		// Unknown IATA zone for a country:
		exceptionThrown = intercept[GeoBaseException] {
			geoBase.getGeoType(List("FR", "XX")).get
		}
		assert(exceptionThrown.getMessage === "Unknown country \"XX\"")
		exceptionThrown = intercept[GeoBaseException] {
			geoBase.getGeoType(List("FR", "XX", "..")).get
		}
		assert(exceptionThrown.getMessage === "Unknown countries \"XX\", \"..\"")
	}

	test("Local Date to GMT Date") {

		// 1: French summer time:
		assert(geoBase.localDateToGMT("20160606_1627", "NCE").get == "20160606_1427")
		assert(geoBase.localDateToGMT("20160606_1627", "NCE", "yyyyMMdd_HHmm").get == "20160606_1427")

		// 2: LON
		assert(geoBase.localDateToGMT("20160606_1527", "LON").get == "20160606_1427")

		// 3: NYC:
		assert(geoBase.localDateToGMT("20160606_1027", "JFK").get == "20160606_1427")

		// 4: With a change of day:
		assert(geoBase.localDateToGMT("20160606_2227", "NYC").get == "20160607_0227")

		// 5: French winter time:
		assert(geoBase.localDateToGMT("20160212_1627", "NCE").get == "20160212_1527")

		// 6: Another format:
		val computedGmtDate = geoBase.localDateToGMT(
			"2016-06-06T22:27", "NYC", "yyyy-MM-dd'T'HH:mm"
		)
		assert(computedGmtDate.get === "2016-06-07T02:27")

		// 7: With an invalid airport/city:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.localDateToGMT("20160212_1627", "...").get
		}
		assert(exceptionThrown.getMessage === "Unknown location \"...\"")
	}

	test("Offset Getter from Local Date") {

		assert(geoBase.getOffsetForLocalDate("20170712", "NCE").get === 120)
		assert(geoBase.getOffsetForLocalDate("20170712", "NCE", "yyyyMMdd").get === 120)
		assert(geoBase.getOffsetForLocalDate("2017-07-12", "NCE", "yyyy-MM-dd").get === 120)

		assert(geoBase.getOffsetForLocalDate("20171224", "NCE").get === 60)

		assert(geoBase.getOffsetForLocalDate("20171224", "NYC").get === -300)
	}

	test("GMT Date to Local Date") {

		// 1: French summer time:
		assert(geoBase.gmtDateToLocal("20160606_1427", "NCE").get == "20160606_1627")
		assert(geoBase.gmtDateToLocal("20160606_1427", "NCE", "yyyyMMdd_HHmm").get == "20160606_1627")

		// 2: LON
		assert(geoBase.gmtDateToLocal("20160606_1427", "LON").get == "20160606_1527")

		// 3: NYC:
		assert(geoBase.gmtDateToLocal("20160606_1427", "JFK").get == "20160606_1027")

		// 4: With a change of day:
		assert(geoBase.gmtDateToLocal("20160607_0227", "NYC").get == "20160606_2227")

		// 5: French winter time:
		assert(geoBase.gmtDateToLocal("20160212_1527", "NCE").get == "20160212_1627")

		// 6: Another format:
		val computedGmtDate = geoBase.gmtDateToLocal(
			"2016-06-07T02:27", "NYC", "yyyy-MM-dd'T'HH:mm"
		)
		assert(computedGmtDate.get === "2016-06-06T22:27")

		// 7: With an invalid airport/city:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.gmtDateToLocal("20160212_1627", "...").get
		}
		assert(exceptionThrown.getMessage === "Unknown location \"...\"")
	}

	test("Trip Duration Getter Between two Local Dates") {

		// 1: Origin = destination and departure time = arrival date:
		var computedTripDuration = geoBase.getTripDurationFromLocalDates(
			"20160606_1627", "NCE", "20160606_1627", "NCE"
		)
		assert(computedTripDuration.get === 0f)

		// 2: Within same time zone:
		computedTripDuration = geoBase.getTripDurationFromLocalDates(
			"20160606_1627", "NCE", "20160606_1757", "CDG"
		)
		assert(computedTripDuration.get === 1.5f)

		// 3: With a different time zone:
		computedTripDuration = geoBase.getTripDurationFromLocalDates(
			"20160606_1627", "CDG", "20160606_1757", "JFK"
		)
		assert(computedTripDuration.get === 7.5f)

		// 4: With a different time zone and a change of date:
		computedTripDuration = geoBase.getTripDurationFromLocalDates(
			"20160606_2327", "CDG", "20160607_0057", "JFK"
		)
		assert(computedTripDuration.get === 7.5f)

		// 5: With an invalid origin city/ariport:
		var exceptionThrown = intercept[GeoBaseException] {
			geoBase.getTripDurationFromLocalDates(
				"20160606_1627", "...", "20160606_1757", "CDG"
			).get
		}
		assert(exceptionThrown.getMessage === "Unknown location \"...\"")

		// 6: A negative trip duration:
		exceptionThrown = intercept[GeoBaseException] {
			geoBase.getTripDurationFromLocalDates(
				"20160607_0057", "JFK", "20160606_2327", "CDG"
			).get
		}
		val expectedMessage = (
			"The trip duration computed is negative (maybe you've inverted " +
			"departure/origin and arrival/destination)"
		)
		assert(exceptionThrown.getMessage === expectedMessage)

		// 7: The trip duration in minutes:
		computedTripDuration = geoBase.getTripDurationFromLocalDates(
			"20160606_1627", "CDG", "20160606_1757", "JFK", unit = "minutes"
		)
		assert(computedTripDuration.get === 450f)

		// 8: With a specific format:
		computedTripDuration = geoBase.getTripDurationFromLocalDates(
			"2016-06-06T16:27", "CDG", "2016-06-06T17:57", "JFK",
			format = "yyyy-MM-dd'T'HH:mm"
		)
		assert(computedTripDuration.get === 7.5f)

		// 9: With a specific format and in minutes:
		computedTripDuration = geoBase.getTripDurationFromLocalDates(
			"2016-06-06T16:27", "CDG", "2016-06-06T17:57", "JFK",
			format = "yyyy-MM-dd'T'HH:mm", unit = "minutes"
		)
		assert(computedTripDuration.get === 450f)

		// 10: Let's try an invalid unit:
		val invalidExceptionThrown = intercept[InvalidParameterException] {
			geoBase.getTripDurationFromLocalDates(
				"20160606_1627", "NCE", "20160606_1757", "CDG", unit = "osef"
			)
		}
		val invalidExpectedMessage = (
			"Option \"unit\" can only take value \"hours\" or " +
			"\"minutes\" but not \"osef\""
		)
		assert(invalidExceptionThrown.getMessage === invalidExpectedMessage)
	}

	test("Get Nearby Airports") {

		// 1: Invalid radius (negative):
		var exceptionThrown = intercept[InvalidParameterException] {
			geoBase.getNearbyAirports("CDG", -50).get
		}
		assert(exceptionThrown.getMessage === "No negative radius allowed")

		// 2: Invalid radius (zero):
		exceptionThrown = intercept[InvalidParameterException] {
			geoBase.getNearbyAirports("CDG", 0).get
		}
		assert(exceptionThrown.getMessage === "No negative radius allowed")

		// 3: Normal use case:
		val expectedAirports = List("LBG", "CSF", "ORY", "VIY", "POX", "TNF")
		assert(geoBase.getNearbyAirports("CDG", 50).get === expectedAirports)

		// 4: Normal use case with the detail of distances:
		val computedAirports = geoBase.getNearbyAirportsWithDetails("CDG", 50).get
		val expectedAirportsWithDetails = List(
			("LBG", 9), ("CSF", 27), ("ORY", 35),
			("VIY", 37), ("POX", 38), ("TNF", 44)
		)
		assert(computedAirports === expectedAirportsWithDetails)

		// 5: Closer radius:
		assert(geoBase.getNearbyAirports("CDG", 10).get === List("LBG"))

		// 6: No nearby airports:
		assert(geoBase.getNearbyAirports("CDG", 5).get === List())

		// 7: Unknown location:
		val exceptionThrown2 = intercept[GeoBaseException] {
			geoBase.getNearbyAirports("...", 20).get
		}
		assert(exceptionThrown2.getMessage === "Unknown location \"...\"")
	}
}
