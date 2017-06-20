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

	test("Item Existence within Mapping") {

		val checkExistence = PrivateMethod[GeoBase]('checkExistence)

		val dummyLocation = AirportOrCity("", "", "", "", "", "", "")

		// No exception thrown:
		geoBase.invokePrivate(checkExistence("CDG", Map("CDG" -> dummyLocation)))

		// Exception thrown:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.invokePrivate(checkExistence("...", Map("CDG" -> dummyLocation)))
		}
		val expectedMessage = "No entry in GeoBase for \"...\""
		assert(exceptionThrown.getMessage === expectedMessage)
	}

	test("Airport to City Getter") {

		// Basic cases:
		assert(geoBase.getCityForAirport("ORY") === "PAR")
		assert(geoBase.getCityForAirportOrElse("ORY", "###") === "PAR")
		assert(geoBase.getCityForAirportOrElse("ORY") === "PAR")
		assert(geoBase.getCityForAirport("CDG") === "PAR")
		assert(geoBase.getCityForAirport("JFK") === "NYC")
		assert(geoBase.getCityForAirport("NCE") === "NCE")

		// Case where several cities are given for one airport:
		assert(geoBase.getCityForAirport("AZA") === "PHX") // PHX,MSC
		assert(geoBase.getCityForAirportOrElse("AZA", "###") === "PHX") // PHX,MSC

		// Unknown airport:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.getCityForAirport("...")
		}
		val expectedMessage = "No entry in GeoBase for \"...\""
		assert(exceptionThrown.getMessage === expectedMessage)

		// Unknown airport:
		assert(geoBase.getCityForAirportOrElse("...", "###") === "###")
		assert(geoBase.getCityForAirportOrElse("...") === "")

		// A city as input will return the city itself:
		assert(geoBase.getCityForAirport("PAR") === "PAR")
		assert(geoBase.getCityForAirportOrElse("PAR", "###") === "PAR")
	}

	test("Airport to Cities Getter") {

		// Basic cases:
		assert(geoBase.getCitiesForAirport("ORY") === List("PAR"))
		assert(geoBase.getCitiesForAirportOrElse("ORY", List()) === List("PAR"))
		assert(geoBase.getCitiesForAirportOrElse("ORY") === List("PAR"))
		assert(geoBase.getCitiesForAirport("CDG") === List("PAR"))
		assert(geoBase.getCitiesForAirport("JFK") === List("NYC"))
		assert(geoBase.getCitiesForAirport("NCE") === List("NCE"))

		// Case where several cities are given for one airport:
		assert(geoBase.getCitiesForAirport("AZA") === List("PHX", "MSC"))
		assert(geoBase.getCitiesForAirportOrElse("AZA", List()) === List("PHX", "MSC"))

		// Unknown airport:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.getCitiesForAirport("...")
		}
		val expectedMessage = "No entry in GeoBase for \"...\""
		assert(exceptionThrown.getMessage === expectedMessage)

		// Unknown airport:
		assert(geoBase.getCitiesForAirportOrElse("...", List()) === List())

		// A city as input will return the city itself:
		assert(geoBase.getCitiesForAirport("PAR") === List("PAR"))
		assert(geoBase.getCitiesForAirportOrElse("PAR", List()) === List("PAR"))
	}

	test("Airport/City to Country Getter") {

		assert(geoBase.getCountryForAirport("ORY") === "FR")
		assert(geoBase.getCountryForCity("ORY") === "FR")
		assert(geoBase.getCountryForAirportOrElse("ORY", "") === "FR")
		assert(geoBase.getCountryForCityOrElse("ORY", "") === "FR")
		assert(geoBase.getCountryForAirport("CDG") === "FR")
		assert(geoBase.getCountryForAirport("JFK") === "US")
		assert(geoBase.getCountryForAirport("NCE") === "FR")
		assert(geoBase.getCountryForAirport("PAR") === "FR")

		// Unknown airport/city:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.getCountryForAirport("...")
		}
		val expectedMessage = "No entry in GeoBase for \"...\""
		assert(exceptionThrown.getMessage === expectedMessage)

		// Unknown airport/city:
		assert(geoBase.getCountryForAirportOrElse("...",  "??") === "??")
		assert(geoBase.getCountryForCityOrElse("...",  "??") === "??")
		assert(geoBase.getCountryForAirportOrElse("...",  "") === "")
	}

	test("Location to Continent Getter") {

		// Various input types (airport, city, country):
		assert(geoBase.getContinentForLocation("ORY") === "EU")
		assert(geoBase.getContinentForLocationOrElse("ORY", "") === "EU")
		assert(geoBase.getContinentForLocation("LON") === "EU")
		assert(geoBase.getContinentForLocation("FR") === "EU")
		assert(geoBase.getContinentForLocation("JFK") === "NA")
		assert(geoBase.getContinentForLocation("AU") === "OC")
		assert(geoBase.getContinentForLocation("HK") === "AS")
		assert(geoBase.getContinentForLocation("BUE") === "SA")
		assert(geoBase.getContinentForLocation("AN") === "NA")
		assert(geoBase.getContinentForLocation("ZA") === "AF")
		assert(geoBase.getContinentForLocationOrElse("ZA", "") === "AF")

		// Aliases:
		assert(geoBase.getContinentForAirport("ORY") === "EU")
		assert(geoBase.getContinentForAirportOrElse("ORY", "") === "EU")
		assert(geoBase.getContinentForCity("ORY") === "EU")
		assert(geoBase.getContinentForCityOrElse("ORY", "") === "EU")
		assert(geoBase.getContinentForCountry("ORY") === "EU")
		assert(geoBase.getContinentForCountryOrElse("ORY", "") === "EU")

		// Unknown location:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.getContinentForLocation("..")
		}
		val expectedMessage = "No entry in GeoBase for \"..\""
		assert(exceptionThrown.getMessage === expectedMessage)

		// Unknown location:
		assert(geoBase.getContinentForLocationOrElse("..", "??") === "??")
		assert(geoBase.getContinentForLocationOrElse("..", "") === "")
	}

	test("Location to IATA Zone Getter") {

		// Various input types (airport, city, country):
		assert(geoBase.getIataZoneForLocation("ORY") === "21")
		assert(geoBase.getIataZoneForLocationOrElse("ORY", "") === "21")
		assert(geoBase.getIataZoneForLocation("LON") === "21")
		assert(geoBase.getIataZoneForLocation("FR") === "21")
		assert(geoBase.getIataZoneForLocation("JFK") === "11")
		assert(geoBase.getIataZoneForLocation("AU") === "32")
		assert(geoBase.getIataZoneForLocation("HK") === "31")
		assert(geoBase.getIataZoneForLocation("BUE") === "13")
		assert(geoBase.getIataZoneForLocation("ZA") === "23")
		assert(geoBase.getIataZoneForLocationOrElse("ZA", "") === "23")

		// Aliases:
		assert(geoBase.getIataZoneForAirport("ORY") === "21")
		assert(geoBase.getIataZoneForAirportOrElse("ORY", "") === "21")
		assert(geoBase.getIataZoneForCity("ORY") === "21")
		assert(geoBase.getIataZoneForCityOrElse("ORY", "") === "21")
		assert(geoBase.getIataZoneForCountry("ORY") === "21")
		assert(geoBase.getIataZoneForCountryOrElse("ORY", "") === "21")

		// Unknown location:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.getIataZoneForLocation("..")
		}
		val expectedMessage = "No entry in GeoBase for \"..\""
		assert(exceptionThrown.getMessage === expectedMessage)

		// Unknown location:
		assert(geoBase.getIataZoneForLocationOrElse("..", "??") === "??")
		assert(geoBase.getIataZoneForLocationOrElse("..", "") === "")
	}

	test("Location to Currency  Getter") {

		// Various input types (city, country):
		assert(geoBase.getCurrencyForCountry("PAR") === "EUR")
		assert(geoBase.getCurrencyForCountryOrElse("PAR", "") === "EUR")
		assert(geoBase.getCurrencyForCountry("JFK") === "USD")
		assert(geoBase.getCurrencyForCountry("FR") === "EUR")
		assert(geoBase.getCurrencyForCountry("AU") === "AUD")
		assert(geoBase.getCurrencyForCountryOrElse("AU", "") === "AUD")

		// Aliases:
		assert(geoBase.getCurrencyForCity("PAR") === "EUR")
		assert(geoBase.getCurrencyForCityOrElse("PAR", "") === "EUR")

		// Unknown location:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.getCurrencyForCountry("..")
		}
		val expectedMessage = "No entry in GeoBase for \"..\""
		assert(exceptionThrown.getMessage === expectedMessage)

		// Unknown location:
		assert(geoBase.getCurrencyForCountryOrElse("..", "???") === "???")
		assert(geoBase.getCurrencyForCountryOrElse("..", "") === "")
	}

	test("Airline to Country Getter") {

		assert(geoBase.getCountryForAirline("BA") === "GB")
		assert(geoBase.getCountryForAirlineOrElse("BA", "") === "GB")
		assert(geoBase.getCountryForAirline("AF") === "FR")
		assert(geoBase.getCountryForAirline("AA") === "US")
		assert(geoBase.getCountryForAirline("LH") === "DE")

		// Unknown airline:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.getCountryForAirline("..")
		}
		val expectedMessage = "No entry in GeoBase for \"..\""
		assert(exceptionThrown.getMessage === expectedMessage)

		// Unknown airline:
		assert(geoBase.getCountryForAirlineOrElse("..", "??") === "??")
		assert(geoBase.getCountryForAirlineOrElse("..", "") === "")
	}

	test("Distance Getter") {

		assert(geoBase.getDistanceBetween("ORY", "NCE") === 676)
		assert(geoBase.getDistanceBetween("NCE", "ORY") === 676)

		assert(geoBase.getDistanceBetweenOrElse("ORY", "NCE", -1) === 676)

		assert(geoBase.getDistanceBetween("ORY", "CDG") === 35)

		assert(geoBase.getDistanceBetween("ORY", "ORY") === 0)

		// Unknown airport/city:
		var exceptionThrown = intercept[GeoBaseException] {
			geoBase.getDistanceBetween("...", "CDG")
		}
		var expectedMessage = (
			"One of airports/cities ... or CDG is either not entry in GeoBase or " +
			"doesn't have a valid latitude or longitude."
		)
		assert(exceptionThrown.getMessage === expectedMessage)

		exceptionThrown = intercept[GeoBaseException] {
			geoBase.getDistanceBetween("CDG", "...")
		}
		expectedMessage = (
			"One of airports/cities CDG or ... is either not entry in GeoBase or " +
			"doesn't have a valid latitude or longitude."
		)
		assert(exceptionThrown.getMessage === expectedMessage)

		// Unknown airport/city:
		assert(geoBase.getDistanceBetweenOrElse("...", "NCE", -1) === -1)
		assert(geoBase.getDistanceBetweenOrElse("CDG", "...", 0) === 0)
	}

	test("Geo Type Getter (domestic, conti., interconti.)") {

		assert(geoBase.getGeoType(List("CDG", "ORY")) === GeoType.DOMESTIC)

		assert(geoBase.getGeoType(List("CDG", "PAR")) === GeoType.DOMESTIC)
		assert(geoBase.getGeoType(List("CDG", "NCE", "NCE", "TLS")) === GeoType.DOMESTIC)

		assert(geoBase.getGeoType(List("CDG", "FRA")) === GeoType.CONTINENTAL)
		assert(geoBase.getGeoType(List("CDG", "TLS", "DUB", "FRA")) === GeoType.CONTINENTAL)

		assert(geoBase.getGeoType(List("CDG", "JFK")) === GeoType.INTER_CONTINENTAL)
		assert(geoBase.getGeoType(List("PAR", "JFK")) === GeoType.INTER_CONTINENTAL)
		assert(geoBase.getGeoType(List("PAR", "NYC")) === GeoType.INTER_CONTINENTAL)
		assert(geoBase.getGeoType(List("CDG", "TLS", "JFK", "MEX")) === GeoType.INTER_CONTINENTAL)

		assert(geoBase.getGeoType(List("FR", "FR")) === GeoType.DOMESTIC)
		assert(geoBase.getGeoType(List("FR", "FR", "DE")) === GeoType.CONTINENTAL)
		assert(geoBase.getGeoType(List("FR", "GB", "DE")) === GeoType.CONTINENTAL)
		assert(geoBase.getGeoType(List("FR", "PAR", "DUB")) === GeoType.CONTINENTAL)
		assert(geoBase.getGeoType(List("US", "PAR", "DUB")) === GeoType.INTER_CONTINENTAL)

		// Empty list of airports/cities:
		var invalidExceptionThrown = intercept[InvalidParameterException] {
			geoBase.getGeoType(List("CDG"))
		}
		var invalidExpectedMessage = (
			"GeoBase can't provide a Geography Type if only one " +
			"airport or city is provided"
		)
		assert(invalidExceptionThrown.getMessage === invalidExpectedMessage)
		invalidExceptionThrown = intercept[InvalidParameterException] {
			geoBase.getGeoType(List())
		}
		invalidExpectedMessage = (
			"GeoBase can't provide a Geography Type for an empty list of " +
			"airports/cities"
		)
		assert(invalidExceptionThrown.getMessage === invalidExpectedMessage)

		// Unknown airport/city:
		var exceptionThrown = intercept[GeoBaseException] {
			geoBase.getGeoType(List("CDG", "..."))
		}
		var expectedMessage = "No entry in GeoBase for \"...\""
		assert(exceptionThrown.getMessage === expectedMessage)
		exceptionThrown = intercept[GeoBaseException] {
			geoBase.getGeoType(List("US", "CDG", "NCE", "aaa"))
		}
		expectedMessage = "No entry in GeoBase for \"aaa\""
		assert(exceptionThrown.getMessage === expectedMessage)

		// Unknown IATA zone for a country:
		exceptionThrown = intercept[GeoBaseException] {
			geoBase.getGeoType(List("FR", "XX"))
		}
		expectedMessage = "No entry in GeoBase for \"XX\""
		assert(exceptionThrown.getMessage === expectedMessage)
	}

	test("Local Date to GMT Date") {

		// 1: French summer time:
		assert(geoBase.localDateToGMT("20160606_1627", "NCE") == "20160606_1427")
		assert(geoBase.localDateToGMT("20160606_1627", "NCE", "yyyyMMdd_HHmm") == "20160606_1427")

		// 2: LON
		assert(geoBase.localDateToGMT("20160606_1527", "LON") == "20160606_1427")

		// 3: NYC:
		assert(geoBase.localDateToGMT("20160606_1027", "JFK") == "20160606_1427")

		// 4: With a change of day:
		assert(geoBase.localDateToGMT("20160606_2227", "NYC") == "20160607_0227")

		// 5: French winter time:
		assert(geoBase.localDateToGMT("20160212_1627", "NCE") == "20160212_1527")

		// 6: Another format:
		val computedGmtDate = geoBase.localDateToGMT(
			"2016-06-06T22:27", "NYC", "yyyy-MM-dd'T'HH:mm"
		)
		assert(computedGmtDate === "2016-06-07T02:27")

		// 7: With an invalid airport/city:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.localDateToGMT("20160212_1627", "...")
		}
		val expectedMessage = "No entry in GeoBase for \"...\""
		assert(exceptionThrown.getMessage === expectedMessage)
	}

	test("Offset Getter from Local Date") {

		assert(geoBase.getOffsetForLocalDate("20170712", "NCE") === 120)
		assert(geoBase.getOffsetForLocalDate("20170712", "NCE", "yyyyMMdd") === 120)
		assert(geoBase.getOffsetForLocalDate("2017-07-12", "NCE", "yyyy-MM-dd") === 120)

		assert(geoBase.getOffsetForLocalDate("20171224", "NCE") === 60)

		assert(geoBase.getOffsetForLocalDate("20171224", "NYC") === -300)
	}

	test("GMT Date to Local Date") {

		// 1: French summer time:
		assert(geoBase.gmtDateToLocal("20160606_1427", "NCE") == "20160606_1627")
		assert(geoBase.gmtDateToLocal("20160606_1427", "NCE", "yyyyMMdd_HHmm") == "20160606_1627")

		// 2: LON
		assert(geoBase.gmtDateToLocal("20160606_1427", "LON") == "20160606_1527")

		// 3: NYC:
		assert(geoBase.gmtDateToLocal("20160606_1427", "JFK") == "20160606_1027")

		// 4: With a change of day:
		assert(geoBase.gmtDateToLocal("20160607_0227", "NYC") == "20160606_2227")

		// 5: French winter time:
		assert(geoBase.gmtDateToLocal("20160212_1527", "NCE") == "20160212_1627")

		// 6: Another format:
		val computedGmtDate = geoBase.gmtDateToLocal(
			"2016-06-07T02:27", "NYC", "yyyy-MM-dd'T'HH:mm"
		)
		assert(computedGmtDate === "2016-06-06T22:27")

		// 7: With an invalid airport/city:
		val exceptionThrown = intercept[GeoBaseException] {
			geoBase.gmtDateToLocal("20160212_1627", "...")
		}
		val expectedMessage = "No entry in GeoBase for \"...\""
		assert(exceptionThrown.getMessage === expectedMessage)
	}

	test("Trip Duration (EFT) Getter Between two Local Dates") {

		// 1: Origin = destination and departure time = arrival date:
		var computedTripDuration = geoBase.getTripDurationFromLocalDates(
			"20160606_1627", "NCE", "20160606_1627", "NCE"
		)
		assert(computedTripDuration === 0f)

		// 2: Within same time zone:
		computedTripDuration = geoBase.getTripDurationFromLocalDates(
			"20160606_1627", "NCE", "20160606_1757", "CDG"
		)
		assert(computedTripDuration === 1.5f)

		// 3: With a different time zone:
		computedTripDuration = geoBase.getTripDurationFromLocalDates(
			"20160606_1627", "CDG", "20160606_1757", "JFK"
		)
		assert(computedTripDuration === 7.5f)

		// 4: With a different time zone and a change of date:
		computedTripDuration = geoBase.getTripDurationFromLocalDates(
			"20160606_2327", "CDG", "20160607_0057", "JFK"
		)
		assert(computedTripDuration === 7.5f)

		// 5: With an invalid origin city/ariport:
		var exceptionThrown = intercept[GeoBaseException] {
			geoBase.getTripDurationFromLocalDates(
				"20160606_1627", "...", "20160606_1757", "CDG"
			)
		}
		var expectedMessage = "No entry in GeoBase for \"...\""
		assert(exceptionThrown.getMessage === expectedMessage)

		// 6: A negative EFT:
		exceptionThrown = intercept[GeoBaseException] {
			geoBase.getTripDurationFromLocalDates(
				"20160607_0057", "JFK", "20160606_2327", "CDG"
			)
		}
		expectedMessage = (
			"The trip duration computed is negative (maybe you've inverted " +
			"departure/origin and arrival/destination)"
		)
		assert(exceptionThrown.getMessage === expectedMessage)

		// 7: Let's try the alias:
		computedTripDuration = geoBase.getEFTfromLocalDates(
			"20160606_1627", "CDG", "20160606_1757", "JFK"
		)
		assert(computedTripDuration === 7.5f)

		// 8: The trip duration in minutes:
		computedTripDuration = geoBase.getEFTfromLocalDates(
			"20160606_1627", "CDG", "20160606_1757", "JFK", unit = "minutes"
		)
		assert(computedTripDuration === 450f)

		// 9: With a specific format:
		computedTripDuration = geoBase.getTripDurationFromLocalDates(
			"2016-06-06T16:27", "CDG", "2016-06-06T17:57", "JFK",
			format = "yyyy-MM-dd'T'HH:mm"
		)
		assert(computedTripDuration === 7.5f)

		// 10: With a specific format and in minutes:
		computedTripDuration = geoBase.getTripDurationFromLocalDates(
			"2016-06-06T16:27", "CDG", "2016-06-06T17:57", "JFK",
			format = "yyyy-MM-dd'T'HH:mm", unit = "minutes"
		)
		assert(computedTripDuration === 450f)

		// 11: The alias, with a specific format and in minutes:
		computedTripDuration = geoBase.getEFTfromLocalDates(
			"2016-06-06T16:27", "CDG", "2016-06-06T17:57", "JFK",
			format = "yyyy-MM-dd'T'HH:mm", unit = "minutes"
		)
		assert(computedTripDuration === 450f)

		// 12: Let's try an invalid unit:
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

	test("Get Day of Week") {
		// 1: A monday is "1":
		assert(geoBase.getDayOfWeek("20160606") === "1")
		// 2: A sunday is "7":
		assert(geoBase.getDayOfWeek("20160612") === "7")
		// 3: A tuesday is "2":
		assert(geoBase.getDayOfWeek("20160614") === "2")
		// 4: A tuesday is "2":
		assert(geoBase.getDayOfWeek("2016-06-14", "yyyy-MM-dd") === "2")
	}

	test("Get Nearby Airports") {

		// 1: Invalid radius (negative):
		var exceptionThrown = intercept[InvalidParameterException] {
			geoBase.getNearbyAirports("CDG", -50)
		}
		var expectedMessage = "No negative radius allowed"
		assert(exceptionThrown.getMessage === expectedMessage)

		// 2: Invalid radius (zero):
		exceptionThrown = intercept[InvalidParameterException] {
			geoBase.getNearbyAirports("CDG", 0)
		}
		expectedMessage = "No negative radius allowed"
		assert(exceptionThrown.getMessage === expectedMessage)

		// 3: Normal use case:
		val expectedAirports = List("LBG", "CSF", "ORY", "VIY", "POX", "TNF")
		assert(geoBase.getNearbyAirports("CDG", 50) === expectedAirports)

		// 4: Normal use case with the detail of distances:
		val computedAirports = geoBase.getNearbyAirportsWithDetails("CDG", 50)
		val expectedAirportsWithDetails = List(
			("LBG", 9), ("CSF", 27), ("ORY", 35),
			("VIY", 37), ("POX", 38), ("TNF", 44)
		)
		assert(computedAirports === expectedAirportsWithDetails)

		// 5: Closer radius:
		assert(geoBase.getNearbyAirports("CDG", 10) === List("LBG"))

		// 6: No nearby airports:
		assert(geoBase.getNearbyAirports("CDG", 5) === List())
	}
}
