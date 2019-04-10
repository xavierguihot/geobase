package com.geobase

import com.geobase.GeoBase.StringExtensions
import com.geobase.error.GeoBaseException
import com.geobase.model.MINUTES
import com.geobase.model.{DOMESTIC, CONTINENTAL, INTER_CONTINENTAL}

import scala.util.Success

import org.scalatest.FunSuite

import com.holdenkarau.spark.testing.SharedSparkContext

/** Testing facility for GeoBase.
  *
  * @author Xavier Guihot
  * @since 2016-05
  */
class GeoBaseTest extends FunSuite with SharedSparkContext {

  // Let's load data (loading is lazy) in order not to impact tests duration:
  GeoBase.city("ORY")
  GeoBase.continent("FR")
  GeoBase.countryForAirline("BA")

  test("Airport to city") {

    // Basic cases:
    assert(GeoBase.city("ORY") === Success("PAR"))
    assert(GeoBase.city("CDG") === Success("PAR"))
    assert(GeoBase.city("JFK") === Success("NYC"))
    assert(GeoBase.city("NCE") === Success("NCE"))

    // Case where several cities are given for one airport:
    assert(GeoBase.city("AZA") === Success("PHX")) // PHX,MSC

    // Unknown airport:
    val exceptionThrown = intercept[GeoBaseException] { GeoBase.city("...").get }
    assert(exceptionThrown.getMessage === "Unknown airport \"...\"")

    // A city as input will return the city itself:
    assert(GeoBase.city("PAR") === Success("PAR"))

    // Pimped case:
    assert("CDG".city === Success("PAR"))
  }

  test("Airport to cities") {

    // Basic cases:
    assert(GeoBase.cities("ORY") === Success(List("PAR")))
    assert(GeoBase.cities("CDG") === Success(List("PAR")))
    assert(GeoBase.cities("JFK") === Success(List("NYC")))
    assert(GeoBase.cities("NCE") === Success(List("NCE")))

    // Case where several cities are given for one airport:
    assert(GeoBase.cities("AZA") === Success(List("PHX", "MSC")))

    // Unknown airport:
    val exceptionThrown = intercept[GeoBaseException] { GeoBase.cities("...").get }
    assert(exceptionThrown.getMessage === "Unknown airport \"...\"")

    // A city as input will return the city itself:
    assert(GeoBase.cities("PAR") === Success(List("PAR")))

    // Pimped case:
    assert("ORY".cities === Success(List("PAR")))
  }

  test("Location to country") {

    assert(GeoBase.country("ORY") === Success("FR"))
    assert(GeoBase.country("CDG") === Success("FR"))
    assert(GeoBase.country("JFK") === Success("US"))
    assert(GeoBase.country("NCE") === Success("FR"))
    assert(GeoBase.country("PAR") === Success("FR"))
    assert(GeoBase.country("FR") === Success("FR"))

    // Unknown airport/city:
    var exceptionThrown = intercept[GeoBaseException] { GeoBase.country("...").get }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")

    // Unknown country:
    exceptionThrown = intercept[GeoBaseException] { GeoBase.country("").get }
    assert(exceptionThrown.getMessage === "Unknown location \"\"")

    // Pimped case:
    assert("ORY".country === Success("FR"))
  }

  test("Location to continent") {

    // Various input location types (airport, city, country):
    assert(GeoBase.continent("ORY") === Success("EU"))
    assert(GeoBase.continent("LON") === Success("EU"))
    assert(GeoBase.continent("FR") === Success("EU"))
    assert(GeoBase.continent("JFK") === Success("NA"))
    assert(GeoBase.continent("AU") === Success("OC"))
    assert(GeoBase.continent("HK") === Success("AS"))
    assert(GeoBase.continent("BUE") === Success("SA"))
    assert(GeoBase.continent("AN") === Success("NA"))
    assert(GeoBase.continent("ZA") === Success("AF"))

    // Unknown location:
    val exceptionThrown = intercept[GeoBaseException] { GeoBase.continent("..").get }
    assert(exceptionThrown.getMessage === "Unknown country \"..\"")

    // Pimped case:
    assert("ORY".continent === Success("EU"))
  }

  test("Location to iata zone") {

    // Various input location types (airport, city, country):
    assert(GeoBase.iataZone("ORY") === Success("21"))
    assert(GeoBase.iataZone("LON") === Success("21"))
    assert(GeoBase.iataZone("FR") === Success("21"))
    assert(GeoBase.iataZone("JFK") === Success("11"))
    assert(GeoBase.iataZone("AU") === Success("32"))
    assert(GeoBase.iataZone("HK") === Success("31"))
    assert(GeoBase.iataZone("BUE") === Success("13"))
    assert(GeoBase.iataZone("ZA") === Success("23"))

    // Unknown location:
    val exceptionThrown = intercept[GeoBaseException] { GeoBase.iataZone("..").get }
    assert(exceptionThrown.getMessage === "Unknown country \"..\"")

    // Pimped case:
    assert("ORY".iataZone === Success("21"))
  }

  test("Location to currency") {

    // Various input location types (airport, city, country):
    assert(GeoBase.currency("PAR") === Success("EUR"))
    assert(GeoBase.currency("JFK") === Success("USD"))
    assert(GeoBase.currency("FR") === Success("EUR"))
    assert(GeoBase.currency("AU") === Success("AUD"))

    // Unknown location:
    val exceptionThrown = intercept[GeoBaseException] { GeoBase.currency("..").get }
    assert(exceptionThrown.getMessage === "Unknown country \"..\"")

    // Pimped case:
    assert("PAR".currency === Success("EUR"))
  }

  test("Airline to country") {

    assert(GeoBase.countryForAirline("BA") === Success("GB"))
    assert(GeoBase.countryForAirline("AF") === Success("FR"))
    assert(GeoBase.countryForAirline("AA") === Success("US"))
    assert(GeoBase.countryForAirline("LH") === Success("DE"))

    // Unknown airline:
    val exceptionThrown = intercept[GeoBaseException] {
      GeoBase.countryForAirline("..").get
    }
    assert(exceptionThrown.getMessage === "Unknown airline \"..\"")
  }

  test("Airline to airline name") {

    assert(GeoBase.nameOfAirline("BA") === Success("British Airways"))
    assert(GeoBase.nameOfAirline("AF") === Success("Air France"))
    assert(GeoBase.nameOfAirline("AA") === Success("American Airlines"))
    assert(GeoBase.nameOfAirline("LH") === Success("Lufthansa"))
    assert(GeoBase.nameOfAirline("AH") === Success("Air Algerie"))

    // Several names for this airline code:
    assert(GeoBase.nameOfAirline("5K") === Success("Hi Fly"))

    // Unknown airline:
    val exceptionThrown = intercept[GeoBaseException] { GeoBase.nameOfAirline("..").get }
    assert(exceptionThrown.getMessage === "Unknown airline \"..\"")

    // Pimped case:
    assert("BA".name === Success("British Airways"))
  }

  test("Location to time zone") {
    assert(GeoBase.timeZone("CDG") === Success("Europe/Paris"))
    assert(GeoBase.timeZone("PAR") === Success("Europe/Paris"))
    assert(GeoBase.timeZone("JFK") === Success("America/New_York"))
    assert(GeoBase.timeZone("NYC") === Success("America/New_York"))
    assert(GeoBase.timeZone("BOS") === Success("America/New_York"))
    assert(GeoBase.timeZone("LAX") === Success("America/Los_Angeles"))
    assert(GeoBase.timeZone("DUB") === Success("Europe/Dublin"))

    // Unknown location:
    val exceptionThrown = intercept[GeoBaseException] { GeoBase.timeZone("...").get }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")

    // Pimped case:
    assert("CDG".timeZone === Success("Europe/Paris"))
  }

  test("Distance between two locations") {

    assert(GeoBase.distanceBetween("ORY", "NCE") === Success(676))
    assert(GeoBase.distanceBetween("NCE", "ORY") === Success(676))

    assert(GeoBase.distanceBetween("ORY", "CDG") === Success(35))

    assert(GeoBase.distanceBetween("ORY", "ORY") === Success(0))

    // Unknown airport/city:
    var exceptionThrown = intercept[GeoBaseException] {
      GeoBase.distanceBetween("...", "CDG").get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")

    exceptionThrown = intercept[GeoBaseException] {
      GeoBase.distanceBetween("CDG", "...").get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")

    // Pimped case:
    assert("ORY".distanceWith("NCE") === Success(676))
  }

  test("Geo Type from locations (domestic, conti., interconti.)") {

    var computedGeoType = GeoBase.geoType(List("CDG", "ORY"))
    assert(computedGeoType === Success(DOMESTIC))
    computedGeoType = GeoBase.geoType(List("CDG", "PAR"))
    assert(computedGeoType === Success(DOMESTIC))

    computedGeoType = GeoBase.geoType(List("CDG", "NCE", "NCE", "TLS"))
    assert(computedGeoType === Success(DOMESTIC))
    computedGeoType = GeoBase.geoType(List("CDG", "NCE", "NCE", "CDG"))
    assert(computedGeoType === Success(DOMESTIC))

    computedGeoType = GeoBase.geoType(List("CDG", "FRA"))
    assert(computedGeoType === Success(CONTINENTAL))
    computedGeoType = GeoBase.geoType(List("CDG", "TLS", "DUB", "FRA"))
    assert(computedGeoType === Success(CONTINENTAL))
    computedGeoType = GeoBase.geoType(List("CDG", "TLS", "DUB", "FRA", "CDG"))
    assert(computedGeoType === Success(CONTINENTAL))

    computedGeoType = GeoBase.geoType(List("CDG", "JFK"))
    assert(computedGeoType === Success(INTER_CONTINENTAL))
    computedGeoType = GeoBase.geoType(List("PAR", "JFK"))
    assert(computedGeoType === Success(INTER_CONTINENTAL))
    computedGeoType = GeoBase.geoType(List("PAR", "NYC"))
    assert(computedGeoType === Success(INTER_CONTINENTAL))
    computedGeoType = GeoBase.geoType(List("CDG", "TLS", "JFK", "MEX"))
    assert(computedGeoType === Success(INTER_CONTINENTAL))

    computedGeoType = GeoBase.geoType(List("FR", "FR"))
    assert(computedGeoType === Success(DOMESTIC))
    computedGeoType = GeoBase.geoType(List("FR", "FR", "DE"))
    assert(computedGeoType === Success(CONTINENTAL))
    computedGeoType = GeoBase.geoType(List("FR", "GB", "DE"))
    assert(computedGeoType === Success(CONTINENTAL))
    computedGeoType = GeoBase.geoType(List("FR", "PAR", "DUB"))
    assert(computedGeoType === Success(CONTINENTAL))
    computedGeoType = GeoBase.geoType(List("US", "PAR", "DUB"))
    assert(computedGeoType === Success(INTER_CONTINENTAL))

    // Empty list of airports/cities:
    val invalidExceptionThrown = intercept[IllegalArgumentException] {
      GeoBase.geoType(List("CDG")).get
    }
    val invalidExpectedMessage =
      "requirement failed: at least 2 locations are needed to compute a " +
        "geography type"
    assert(invalidExceptionThrown.getMessage === invalidExpectedMessage)

    // Unknown airport/city:
    var exceptionThrown = intercept[GeoBaseException] {
      GeoBase.geoType(List("CDG", "...")).get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")
    exceptionThrown = intercept[GeoBaseException] {
      GeoBase.geoType(List("US", "CDG", "NCE", "aaa")).get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"aaa\"")
    exceptionThrown = intercept[GeoBaseException] {
      GeoBase.geoType(List("US", "bbb", "NCE", "aaa")).get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"bbb\"")

    // Unknown IATA zone for a country:
    exceptionThrown = intercept[GeoBaseException] {
      GeoBase.geoType(List("FR", "XX")).get
    }
    assert(exceptionThrown.getMessage === "Unknown country \"XX\"")
    exceptionThrown = intercept[GeoBaseException] {
      GeoBase.geoType(List("FR", "XX", "..")).get
    }
    assert(exceptionThrown.getMessage === "Unknown country \"XX\"")
  }

  test("Local date to gmt date") {

    // 1: French summer time:
    var gmtDate = GeoBase.localDateToGMT("20160606_1627", "NCE")
    assert(gmtDate === Success("20160606_1427"))

    // 2: LON
    gmtDate = GeoBase.localDateToGMT("20160606_1527", "LON")
    assert(gmtDate === Success("20160606_1427"))

    // 3: NYC:
    gmtDate = GeoBase.localDateToGMT("20160606_1027", "JFK")
    assert(gmtDate === Success("20160606_1427"))

    // 4: With a change of day:
    gmtDate = GeoBase.localDateToGMT("20160606_2227", "NYC")
    assert(gmtDate === Success("20160607_0227"))

    // 5: French winter time:
    gmtDate = GeoBase.localDateToGMT("20160212_1627", "NCE")
    assert(gmtDate === Success("20160212_1527"))

    // 6: Another format:
    gmtDate = GeoBase
      .localDateToGMT("2016-06-06T22:27", "NYC", "yyyy-MM-dd'T'HH:mm")
    assert(gmtDate === Success("2016-06-07T02:27"))

    // 7: With an invalid airport/city:
    val exceptionThrown = intercept[GeoBaseException] {
      GeoBase.localDateToGMT("20160212_1627", "...").get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")
  }

  test("Offset from local date") {

    var offset = GeoBase.offsetForLocalDate("20170712", "NCE")
    assert(offset === Success(120))
    offset = GeoBase.offsetForLocalDate("2017-07-12", "NCE", "yyyy-MM-dd")
    assert(offset === Success(120))

    offset = GeoBase.offsetForLocalDate("20171224", "NCE")
    assert(offset === Success(60))

    offset = GeoBase.offsetForLocalDate("20171224", "NYC")
    assert(offset === Success(-300))

    val exceptionThrown = intercept[GeoBaseException] {
      GeoBase.offsetForLocalDate("20171224", "...").get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")
  }

  test("GMT date to local date") {

    // 1: French summer time:
    var localDate = GeoBase.gmtDateToLocal("20160606_1427", "NCE")
    assert(localDate === Success("20160606_1627"))

    // 2: LON
    localDate = GeoBase.gmtDateToLocal("20160606_1427", "LON")
    assert(localDate === Success("20160606_1527"))

    // 3: NYC:
    localDate = GeoBase.gmtDateToLocal("20160606_1427", "JFK")
    assert(localDate === Success("20160606_1027"))

    // 4: With a change of day:
    localDate = GeoBase.gmtDateToLocal("20160607_0227", "NYC")
    assert(localDate === Success("20160606_2227"))

    // 5: French winter time:
    localDate = GeoBase.gmtDateToLocal("20160212_1527", "NCE")
    assert(localDate === Success("20160212_1627"))

    // 6: Another format:
    localDate = GeoBase.gmtDateToLocal("2016-06-07T02:27", "NYC", "yyyy-MM-dd'T'HH:mm")
    assert(localDate === Success("2016-06-06T22:27"))

    // 7: With an invalid airport/city:
    val exceptionThrown = intercept[GeoBaseException] {
      GeoBase.gmtDateToLocal("20160212_1627", "...").get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")
  }

  test("Trip duration between two local dates") {

    // 1: Origin = destination and departure time = arrival date:
    var computedTripDuration =
      GeoBase.tripDurationFromLocalDates("20160606_1627", "NCE", "20160606_1627", "NCE")
    assert(computedTripDuration === Success(0d))

    // 2: Within same time zone:
    computedTripDuration =
      GeoBase.tripDurationFromLocalDates("20160606_1627", "NCE", "20160606_1757", "CDG")
    assert(computedTripDuration === Success(1.5d))

    // 3: With a different time zone:
    computedTripDuration =
      GeoBase.tripDurationFromLocalDates("20160606_1627", "CDG", "20160606_1757", "JFK")
    assert(computedTripDuration === Success(7.5d))

    // 4: With a different time zone and a change of date:
    computedTripDuration =
      GeoBase.tripDurationFromLocalDates("20160606_2327", "CDG", "20160607_0057", "JFK")
    assert(computedTripDuration === Success(7.5d))

    // 5: With an invalid origin city/airport:
    var exceptionThrown = intercept[GeoBaseException] {
      GeoBase
        .tripDurationFromLocalDates("20160606_1627", "...", "20160606_1757", "CDG")
        .get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")

    // 6: A negative trip duration:
    exceptionThrown = intercept[GeoBaseException] {
      GeoBase
        .tripDurationFromLocalDates("20160607_0057", "JFK", "20160606_2327", "CDG")
        .get
    }
    val expectedMessage =
      "The trip duration computed is negative (maybe you've inverted " +
        "departure/origin and arrival/destination)"
    assert(exceptionThrown.getMessage === expectedMessage)

    // 7: The trip duration in minutes:
    computedTripDuration = GeoBase
      .tripDurationFromLocalDates("20160606_1627", "CDG", "20160606_1757", "JFK", unit = MINUTES)
    assert(computedTripDuration === Success(450d))

    // 8: With a specific format:
    computedTripDuration = GeoBase.tripDurationFromLocalDates(
      "2016-06-06T16:27",
      "CDG",
      "2016-06-06T17:57",
      "JFK",
      format = "yyyy-MM-dd'T'HH:mm")
    assert(computedTripDuration === Success(7.5d))

    // 9: With a specific format and in minutes:
    computedTripDuration = GeoBase.tripDurationFromLocalDates(
      "2016-06-06T16:27",
      "CDG",
      "2016-06-06T17:57",
      "JFK",
      format = "yyyy-MM-dd'T'HH:mm",
      unit   = MINUTES)
    assert(computedTripDuration === Success(450d))
  }

  test("Nearby airports") {

    // 1: Invalid radius (negative):
    var exceptionThrown = intercept[IllegalArgumentException] {
      GeoBase.nearbyAirports("CDG", -50).get
    }
    val expectedMessage = "requirement failed: radius must be strictly positive"
    assert(exceptionThrown.getMessage === expectedMessage)

    // 2: Invalid radius (zero):
    exceptionThrown = intercept[IllegalArgumentException] {
      GeoBase.nearbyAirports("CDG", 0).get
    }
    assert(exceptionThrown.getMessage === expectedMessage)

    // 3: Normal use case:
    val expectedAirports = List("LBG", "CSF", "ORY", "VIY", "POX", "TNF")
    assert(GeoBase.nearbyAirports("CDG", 50) === Success(expectedAirports))

    // 4: Normal use case with the detail of distances:
    val computedAirports = GeoBase.nearbyAirportsWithDetails("CDG", 50)
    val expectedAirportsWithDetails = List(
      ("LBG", 9),
      ("CSF", 27),
      ("ORY", 35),
      ("VIY", 37),
      ("POX", 38),
      ("TNF", 44)
    )
    assert(computedAirports === Success(expectedAirportsWithDetails))

    // 5: Closer radius:
    assert(GeoBase.nearbyAirports("CDG", 10) === Success(List("LBG")))

    // 6: No nearby airports:
    assert(GeoBase.nearbyAirports("CDG", 5) === Success(List()))

    // 7: Unknown location:
    val exceptionThrown2 = intercept[GeoBaseException] {
      GeoBase.nearbyAirports("...", 20).get
    }
    assert(exceptionThrown2.getMessage === "Unknown location \"...\"")

    // 8: Pimped case:
    assert("CDG".nearbyAirports(50) === Success(expectedAirports))
  }

  test("Check everything is Serializable for Spark") {

    val geoBaseBr = sc.broadcast(GeoBase)

    val airports = sc.parallelize(Array("ORY", "GAT"), 2)
    val cities   = airports.map(airport => geoBaseBr.value.city(airport))

    assert(cities.collect === Array(Success("PAR"), Success("GAT")))
  }
}
