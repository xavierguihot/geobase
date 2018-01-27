package com.geobase

import com.geobase.error.GeoBaseException
import com.geobase.model.{DOMESTIC, CONTINENTAL, INTER_CONTINENTAL}

import scala.util.Success

import org.scalatest.FunSuite
import org.scalatest.PrivateMethodTester

/** Testing facility for GeoBase.
  *
  * @author Xavier Guihot
  * @since 2016-05
  */
class GeoBaseTest extends FunSuite with PrivateMethodTester {

  private val geoBase = new GeoBase()

  test("Airport to city") {

    // Basic cases:
    assert(geoBase.city("ORY") === Success("PAR"))
    assert(geoBase.city("CDG") === Success("PAR"))
    assert(geoBase.city("JFK") === Success("NYC"))
    assert(geoBase.city("NCE") === Success("NCE"))

    // Case where several cities are given for one airport:
    assert(geoBase.city("AZA") === Success("PHX")) // PHX,MSC

    // Unknown airport:
    val exceptionThrown = intercept[GeoBaseException] {
      geoBase.city("...").get
    }
    assert(exceptionThrown.getMessage === "Unknown airport \"...\"")

    // A city as input will return the city itself:
    assert(geoBase.city("PAR") === Success("PAR"))
  }

  test("Airport to cities") {

    // Basic cases:
    assert(geoBase.cities("ORY") === Success(List("PAR")))
    assert(geoBase.cities("CDG") === Success(List("PAR")))
    assert(geoBase.cities("JFK") === Success(List("NYC")))
    assert(geoBase.cities("NCE") === Success(List("NCE")))

    // Case where several cities are given for one airport:
    assert(geoBase.cities("AZA") === Success(List("PHX", "MSC")))

    // Unknown airport:
    val exceptionThrown = intercept[GeoBaseException] {
      geoBase.cities("...").get
    }
    assert(exceptionThrown.getMessage === "Unknown airport \"...\"")

    // A city as input will return the city itself:
    assert(geoBase.cities("PAR") === Success(List("PAR")))
  }

  test("Location to country") {

    assert(geoBase.country("ORY") === Success("FR"))
    assert(geoBase.country("CDG") === Success("FR"))
    assert(geoBase.country("JFK") === Success("US"))
    assert(geoBase.country("NCE") === Success("FR"))
    assert(geoBase.country("PAR") === Success("FR"))
    assert(geoBase.country("FR") === Success("FR"))

    // Unknown airport/city:
    var exceptionThrown = intercept[GeoBaseException] {
      geoBase.country("...").get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")

    // Unkown country:
    exceptionThrown = intercept[GeoBaseException] { geoBase.country("").get }
    assert(exceptionThrown.getMessage === "Unknown location \"\"")
  }

  test("Location to continent") {

    // Various input location types (airport, city, country):
    assert(geoBase.continent("ORY") === Success("EU"))
    assert(geoBase.continent("LON") === Success("EU"))
    assert(geoBase.continent("FR") === Success("EU"))
    assert(geoBase.continent("JFK") === Success("NA"))
    assert(geoBase.continent("AU") === Success("OC"))
    assert(geoBase.continent("HK") === Success("AS"))
    assert(geoBase.continent("BUE") === Success("SA"))
    assert(geoBase.continent("AN") === Success("NA"))
    assert(geoBase.continent("ZA") === Success("AF"))

    // Unknown location:
    val exceptionThrown = intercept[GeoBaseException] {
      geoBase.continent("..").get
    }
    assert(exceptionThrown.getMessage === "Unknown country \"..\"")
  }

  test("Location to iata zone") {

    // Various input location types (airport, city, country):
    assert(geoBase.iataZone("ORY") === Success("21"))
    assert(geoBase.iataZone("LON") === Success("21"))
    assert(geoBase.iataZone("FR") === Success("21"))
    assert(geoBase.iataZone("JFK") === Success("11"))
    assert(geoBase.iataZone("AU") === Success("32"))
    assert(geoBase.iataZone("HK") === Success("31"))
    assert(geoBase.iataZone("BUE") === Success("13"))
    assert(geoBase.iataZone("ZA") === Success("23"))

    // Unknown location:
    val exceptionThrown = intercept[GeoBaseException] {
      geoBase.iataZone("..").get
    }
    assert(exceptionThrown.getMessage === "Unknown country \"..\"")
  }

  test("Location to currency") {

    // Various input location types (airport, city, country):
    assert(geoBase.currency("PAR") === Success("EUR"))
    assert(geoBase.currency("JFK") === Success("USD"))
    assert(geoBase.currency("FR") === Success("EUR"))
    assert(geoBase.currency("AU") === Success("AUD"))

    // Unknown location:
    val exceptionThrown = intercept[GeoBaseException] {
      geoBase.currency("..").get
    }
    assert(exceptionThrown.getMessage === "Unknown country \"..\"")
  }

  test("Airline to country") {

    assert(geoBase.countryForAirline("BA") === Success("GB"))
    assert(geoBase.countryForAirline("AF") === Success("FR"))
    assert(geoBase.countryForAirline("AA") === Success("US"))
    assert(geoBase.countryForAirline("LH") === Success("DE"))

    // Unknown airline:
    val exceptionThrown = intercept[GeoBaseException] {
      geoBase.countryForAirline("..").get
    }
    assert(exceptionThrown.getMessage === "Unknown airline \"..\"")
  }

  test("Location to time zone") {
    assert(geoBase.timeZone("CDG") === Success("Europe/Paris"))
    assert(geoBase.timeZone("PAR") === Success("Europe/Paris"))
    assert(geoBase.timeZone("JFK") === Success("America/New_York"))
    assert(geoBase.timeZone("NYC") === Success("America/New_York"))
    assert(geoBase.timeZone("BOS") === Success("America/New_York"))
    assert(geoBase.timeZone("LAX") === Success("America/Los_Angeles"))
    assert(geoBase.timeZone("DUB") === Success("Europe/Dublin"))

    // Unknown location:
    val exceptionThrown = intercept[GeoBaseException] {
      geoBase.timeZone("...").get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")
  }

  test("Distance between two locations") {

    assert(geoBase.distanceBetween("ORY", "NCE") === Success(676))
    assert(geoBase.distanceBetween("NCE", "ORY") === Success(676))

    assert(geoBase.distanceBetween("ORY", "CDG") === Success(35))

    assert(geoBase.distanceBetween("ORY", "ORY") === Success(0))

    // Unknown airport/city:
    var exceptionThrown = intercept[GeoBaseException] {
      geoBase.distanceBetween("...", "CDG").get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")

    exceptionThrown = intercept[GeoBaseException] {
      geoBase.distanceBetween("CDG", "...").get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")
  }

  test("Geo Type from locations (domestic, conti., interconti.)") {

    var computedGeoType = geoBase.geoType(List("CDG", "ORY"))
    assert(computedGeoType === Success(DOMESTIC))
    computedGeoType = geoBase.geoType(List("CDG", "PAR"))
    assert(computedGeoType === Success(DOMESTIC))

    computedGeoType = geoBase.geoType(List("CDG", "NCE", "NCE", "TLS"))
    assert(computedGeoType === Success(DOMESTIC))
    computedGeoType = geoBase.geoType(List("CDG", "NCE", "NCE", "CDG"))
    assert(computedGeoType === Success(DOMESTIC))

    computedGeoType = geoBase.geoType(List("CDG", "FRA"))
    assert(computedGeoType === Success(CONTINENTAL))
    computedGeoType = geoBase.geoType(List("CDG", "TLS", "DUB", "FRA"))
    assert(computedGeoType === Success(CONTINENTAL))
    computedGeoType = geoBase.geoType(List("CDG", "TLS", "DUB", "FRA", "CDG"))
    assert(computedGeoType === Success(CONTINENTAL))

    computedGeoType = geoBase.geoType(List("CDG", "JFK"))
    assert(computedGeoType === Success(INTER_CONTINENTAL))
    computedGeoType = geoBase.geoType(List("PAR", "JFK"))
    assert(computedGeoType === Success(INTER_CONTINENTAL))
    computedGeoType = geoBase.geoType(List("PAR", "NYC"))
    assert(computedGeoType === Success(INTER_CONTINENTAL))
    computedGeoType = geoBase.geoType(List("CDG", "TLS", "JFK", "MEX"))
    assert(computedGeoType === Success(INTER_CONTINENTAL))

    computedGeoType = geoBase.geoType(List("FR", "FR"))
    assert(computedGeoType === Success(DOMESTIC))
    computedGeoType = geoBase.geoType(List("FR", "FR", "DE"))
    assert(computedGeoType === Success(CONTINENTAL))
    computedGeoType = geoBase.geoType(List("FR", "GB", "DE"))
    assert(computedGeoType === Success(CONTINENTAL))
    computedGeoType = geoBase.geoType(List("FR", "PAR", "DUB"))
    assert(computedGeoType === Success(CONTINENTAL))
    computedGeoType = geoBase.geoType(List("US", "PAR", "DUB"))
    assert(computedGeoType === Success(INTER_CONTINENTAL))

    // Empty list of airports/cities:
    val invalidExceptionThrown = intercept[IllegalArgumentException] {
      geoBase.geoType(List("CDG")).get
    }
    val invalidExpectedMessage =
      "requirement failed: at least 2 locations are needed to compute a " +
        "geography type"
    assert(invalidExceptionThrown.getMessage === invalidExpectedMessage)

    // Unknown airport/city:
    var exceptionThrown = intercept[GeoBaseException] {
      geoBase.geoType(List("CDG", "...")).get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")
    exceptionThrown = intercept[GeoBaseException] {
      geoBase.geoType(List("US", "CDG", "NCE", "aaa")).get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"aaa\"")
    exceptionThrown = intercept[GeoBaseException] {
      geoBase.geoType(List("US", "bbb", "NCE", "aaa")).get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"bbb\"")

    // Unknown IATA zone for a country:
    exceptionThrown = intercept[GeoBaseException] {
      geoBase.geoType(List("FR", "XX")).get
    }
    assert(exceptionThrown.getMessage === "Unknown country \"XX\"")
    exceptionThrown = intercept[GeoBaseException] {
      geoBase.geoType(List("FR", "XX", "..")).get
    }
    assert(exceptionThrown.getMessage === "Unknown country \"XX\"")
  }

  test("Local date to gmt date") {

    // 1: French summer time:
    var gmtDate = geoBase.localDateToGMT("20160606_1627", "NCE")
    assert(gmtDate === Success("20160606_1427"))
    gmtDate = geoBase.localDateToGMT("20160606_1627", "NCE", "yyyyMMdd_HHmm")
    assert(gmtDate === Success("20160606_1427"))

    // 2: LON
    gmtDate = geoBase.localDateToGMT("20160606_1527", "LON")
    assert(gmtDate === Success("20160606_1427"))

    // 3: NYC:
    gmtDate = geoBase.localDateToGMT("20160606_1027", "JFK")
    assert(gmtDate === Success("20160606_1427"))

    // 4: With a change of day:
    gmtDate = geoBase.localDateToGMT("20160606_2227", "NYC")
    assert(gmtDate === Success("20160607_0227"))

    // 5: French winter time:
    gmtDate = geoBase.localDateToGMT("20160212_1627", "NCE")
    assert(gmtDate === Success("20160212_1527"))

    // 6: Another format:
    gmtDate = geoBase
      .localDateToGMT("2016-06-06T22:27", "NYC", "yyyy-MM-dd'T'HH:mm")
    assert(gmtDate === Success("2016-06-07T02:27"))

    // 7: With an invalid airport/city:
    val exceptionThrown = intercept[GeoBaseException] {
      geoBase.localDateToGMT("20160212_1627", "...").get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")
  }

  test("Offset from local date") {

    var offset = geoBase.offsetForLocalDate("20170712", "NCE")
    assert(offset === Success(120))
    offset = geoBase.offsetForLocalDate("20170712", "NCE", "yyyyMMdd")
    assert(offset === Success(120))
    offset = geoBase.offsetForLocalDate("2017-07-12", "NCE", "yyyy-MM-dd")
    assert(offset === Success(120))

    offset = geoBase.offsetForLocalDate("20171224", "NCE")
    assert(offset === Success(60))

    offset = geoBase.offsetForLocalDate("20171224", "NYC")
    assert(offset === Success(-300))
  }

  test("GMT date to local date") {

    // 1: French summer time:
    var localDate = geoBase.gmtDateToLocal("20160606_1427", "NCE")
    assert(localDate === Success("20160606_1627"))

    localDate = geoBase.gmtDateToLocal("20160606_1427", "NCE", "yyyyMMdd_HHmm")
    assert(localDate === Success("20160606_1627"))

    // 2: LON
    localDate = geoBase.gmtDateToLocal("20160606_1427", "LON")
    assert(localDate === Success("20160606_1527"))

    // 3: NYC:
    localDate = geoBase.gmtDateToLocal("20160606_1427", "JFK")
    assert(localDate === Success("20160606_1027"))

    // 4: With a change of day:
    localDate = geoBase.gmtDateToLocal("20160607_0227", "NYC")
    assert(localDate === Success("20160606_2227"))

    // 5: French winter time:
    localDate = geoBase.gmtDateToLocal("20160212_1527", "NCE")
    assert(localDate === Success("20160212_1627"))

    // 6: Another format:
    localDate =
      geoBase.gmtDateToLocal("2016-06-07T02:27", "NYC", "yyyy-MM-dd'T'HH:mm")
    assert(localDate === Success("2016-06-06T22:27"))

    // 7: With an invalid airport/city:
    val exceptionThrown = intercept[GeoBaseException] {
      geoBase.gmtDateToLocal("20160212_1627", "...").get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")
  }

  test("Trip duration between two local dates") {

    // 1: Origin = destination and departure time = arrival date:
    var computedTripDuration = geoBase.tripDurationFromLocalDates(
      "20160606_1627",
      "NCE",
      "20160606_1627",
      "NCE")
    assert(computedTripDuration === Success(0d))

    // 2: Within same time zone:
    computedTripDuration = geoBase.tripDurationFromLocalDates(
      "20160606_1627",
      "NCE",
      "20160606_1757",
      "CDG")
    assert(computedTripDuration === Success(1.5d))

    // 3: With a different time zone:
    computedTripDuration = geoBase.tripDurationFromLocalDates(
      "20160606_1627",
      "CDG",
      "20160606_1757",
      "JFK")
    assert(computedTripDuration === Success(7.5d))

    // 4: With a different time zone and a change of date:
    computedTripDuration = geoBase.tripDurationFromLocalDates(
      "20160606_2327",
      "CDG",
      "20160607_0057",
      "JFK")
    assert(computedTripDuration === Success(7.5d))

    // 5: With an invalid origin city/ariport:
    var exceptionThrown = intercept[GeoBaseException] {
      geoBase
        .tripDurationFromLocalDates(
          "20160606_1627",
          "...",
          "20160606_1757",
          "CDG"
        )
        .get
    }
    assert(exceptionThrown.getMessage === "Unknown location \"...\"")

    // 6: A negative trip duration:
    exceptionThrown = intercept[GeoBaseException] {
      geoBase
        .tripDurationFromLocalDates(
          "20160607_0057",
          "JFK",
          "20160606_2327",
          "CDG"
        )
        .get
    }
    val expectedMessage =
      "The trip duration computed is negative (maybe you've inverted " +
        "departure/origin and arrival/destination)"
    assert(exceptionThrown.getMessage === expectedMessage)

    // 7: The trip duration in minutes:
    computedTripDuration = geoBase.tripDurationFromLocalDates(
      "20160606_1627",
      "CDG",
      "20160606_1757",
      "JFK",
      unit = "minutes")
    assert(computedTripDuration === Success(450d))

    // 8: With a specific format:
    computedTripDuration = geoBase.tripDurationFromLocalDates(
      "2016-06-06T16:27",
      "CDG",
      "2016-06-06T17:57",
      "JFK",
      format = "yyyy-MM-dd'T'HH:mm")
    assert(computedTripDuration === Success(7.5d))

    // 9: With a specific format and in minutes:
    computedTripDuration = geoBase.tripDurationFromLocalDates(
      "2016-06-06T16:27",
      "CDG",
      "2016-06-06T17:57",
      "JFK",
      format = "yyyy-MM-dd'T'HH:mm",
      unit = "minutes")
    assert(computedTripDuration === Success(450d))

    // 10: Let's try an invalid unit:
    val invalidExceptionThrown = intercept[IllegalArgumentException] {
      geoBase.tripDurationFromLocalDates(
        "20160606_1627",
        "NCE",
        "20160606_1757",
        "CDG",
        unit = "osef")
    }
    val invalidExpectedMessage =
      "requirement failed: option \"unit\" can only take value " +
        "\"hours\" or \"minutes\" but not \"osef\""
    assert(invalidExceptionThrown.getMessage === invalidExpectedMessage)
  }

  test("Nearby airports") {

    // 1: Invalid radius (negative):
    var exceptionThrown = intercept[IllegalArgumentException] {
      geoBase.nearbyAirports("CDG", -50).get
    }
    val expectedMessage = "requirement failed: radius must be strictly positive"
    assert(exceptionThrown.getMessage === expectedMessage)

    // 2: Invalid radius (zero):
    exceptionThrown = intercept[IllegalArgumentException] {
      geoBase.nearbyAirports("CDG", 0).get
    }
    assert(exceptionThrown.getMessage === expectedMessage)

    // 3: Normal use case:
    val expectedAirports = List("LBG", "CSF", "ORY", "VIY", "POX", "TNF")
    assert(geoBase.nearbyAirports("CDG", 50) === Success(expectedAirports))

    // 4: Normal use case with the detail of distances:
    val computedAirports = geoBase.nearbyAirportsWithDetails("CDG", 50)
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
    assert(geoBase.nearbyAirports("CDG", 10) === Success(List("LBG")))

    // 6: No nearby airports:
    assert(geoBase.nearbyAirports("CDG", 5) === Success(List()))

    // 7: Unknown location:
    val exceptionThrown2 = intercept[GeoBaseException] {
      geoBase.nearbyAirports("...", 20).get
    }
    assert(exceptionThrown2.getMessage === "Unknown location \"...\"")
  }
}
