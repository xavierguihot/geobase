package com.geobase

import com.geobase.error.GeoBaseException

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

  test("Airport to City Getter") {

    // Basic cases:
    assert(geoBase.getCityFor("ORY") === Success("PAR"))
    assert(geoBase.getCityFor("CDG") === Success("PAR"))
    assert(geoBase.getCityFor("JFK") === Success("NYC"))
    assert(geoBase.getCityFor("NCE") === Success("NCE"))

    // Case where several cities are given for one airport:
    assert(geoBase.getCityFor("AZA") === Success("PHX")) // PHX,MSC

    // Unknown airport:
    val exceptionThrown = intercept[GeoBaseException] {
      geoBase.getCityFor("...").get
    }
    assert(exceptionThrown.getMessage === "Unknown airport \"...\"")

    // A city as input will return the city itself:
    assert(geoBase.getCityFor("PAR") === Success("PAR"))
  }

  test("Airport to Cities Getter") {

    // Basic cases:
    assert(geoBase.getCitiesFor("ORY") === Success(List("PAR")))
    assert(geoBase.getCitiesFor("CDG") === Success(List("PAR")))
    assert(geoBase.getCitiesFor("JFK") === Success(List("NYC")))
    assert(geoBase.getCitiesFor("NCE") === Success(List("NCE")))

    // Case where several cities are given for one airport:
    assert(geoBase.getCitiesFor("AZA") === Success(List("PHX", "MSC")))

    // Unknown airport:
    val exceptionThrown = intercept[GeoBaseException] {
      geoBase.getCitiesFor("...").get
    }
    assert(exceptionThrown.getMessage === "Unknown airport \"...\"")

    // A city as input will return the city itself:
    assert(geoBase.getCitiesFor("PAR") === Success(List("PAR")))
  }

  test("Airport/City to Country Getter") {

    assert(geoBase.getCountryFor("ORY") === Success("FR"))
    assert(geoBase.getCountryFor("CDG") === Success("FR"))
    assert(geoBase.getCountryFor("JFK") === Success("US"))
    assert(geoBase.getCountryFor("NCE") === Success("FR"))
    assert(geoBase.getCountryFor("PAR") === Success("FR"))
    assert(geoBase.getCountryFor("FR") === Success("FR"))

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
    assert(geoBase.getContinentFor("ORY") === Success("EU"))
    assert(geoBase.getContinentFor("LON") === Success("EU"))
    assert(geoBase.getContinentFor("FR") === Success("EU"))
    assert(geoBase.getContinentFor("JFK") === Success("NA"))
    assert(geoBase.getContinentFor("AU") === Success("OC"))
    assert(geoBase.getContinentFor("HK") === Success("AS"))
    assert(geoBase.getContinentFor("BUE") === Success("SA"))
    assert(geoBase.getContinentFor("AN") === Success("NA"))
    assert(geoBase.getContinentFor("ZA") === Success("AF"))

    // Unknown location:
    val exceptionThrown = intercept[GeoBaseException] {
      geoBase.getContinentFor("..").get
    }
    assert(exceptionThrown.getMessage === "Unknown country \"..\"")
  }

  test("Location to IATA Zone Getter") {

    // Various input location types (airport, city, country):
    assert(geoBase.getIataZoneFor("ORY") === Success("21"))
    assert(geoBase.getIataZoneFor("LON") === Success("21"))
    assert(geoBase.getIataZoneFor("FR") === Success("21"))
    assert(geoBase.getIataZoneFor("JFK") === Success("11"))
    assert(geoBase.getIataZoneFor("AU") === Success("32"))
    assert(geoBase.getIataZoneFor("HK") === Success("31"))
    assert(geoBase.getIataZoneFor("BUE") === Success("13"))
    assert(geoBase.getIataZoneFor("ZA") === Success("23"))

    // Unknown location:
    val exceptionThrown = intercept[GeoBaseException] {
      geoBase.getIataZoneFor("..").get
    }
    assert(exceptionThrown.getMessage === "Unknown country \"..\"")
  }

  test("Location to Currency Getter") {

    // Various input location types (airport, city, country):
    assert(geoBase.getCurrencyFor("PAR") === Success("EUR"))
    assert(geoBase.getCurrencyFor("JFK") === Success("USD"))
    assert(geoBase.getCurrencyFor("FR") === Success("EUR"))
    assert(geoBase.getCurrencyFor("AU") === Success("AUD"))

    // Unknown location:
    val exceptionThrown = intercept[GeoBaseException] {
      geoBase.getCurrencyFor("..").get
    }
    assert(exceptionThrown.getMessage === "Unknown country \"..\"")
  }

  test("Airline to Country Getter") {

    assert(geoBase.getCountryForAirline("BA") === Success("GB"))
    assert(geoBase.getCountryForAirline("AF") === Success("FR"))
    assert(geoBase.getCountryForAirline("AA") === Success("US"))
    assert(geoBase.getCountryForAirline("LH") === Success("DE"))

    // Unknown airline:
    val exceptionThrown = intercept[GeoBaseException] {
      geoBase.getCountryForAirline("..").get
    }
    assert(exceptionThrown.getMessage === "Unknown airline \"..\"")
  }

  test("Distance Getter") {

    assert(geoBase.getDistanceBetween("ORY", "NCE") === Success(676))
    assert(geoBase.getDistanceBetween("NCE", "ORY") === Success(676))

    assert(geoBase.getDistanceBetween("ORY", "CDG") === Success(35))

    assert(geoBase.getDistanceBetween("ORY", "ORY") === Success(0))

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

    var computedGeoType = geoBase.getGeoType(List("CDG", "ORY"))
    assert(computedGeoType === Success(GeoType.DOMESTIC))
    computedGeoType = geoBase.getGeoType(List("CDG", "PAR"))
    assert(computedGeoType === Success(GeoType.DOMESTIC))

    computedGeoType = geoBase.getGeoType(List("CDG", "NCE", "NCE", "TLS"))
    assert(computedGeoType === Success(GeoType.DOMESTIC))
    computedGeoType = geoBase.getGeoType(List("CDG", "NCE", "NCE", "CDG"))
    assert(computedGeoType === Success(GeoType.DOMESTIC))

    computedGeoType = geoBase.getGeoType(List("CDG", "FRA"))
    assert(computedGeoType === Success(GeoType.CONTINENTAL))
    computedGeoType = geoBase.getGeoType(List("CDG", "TLS", "DUB", "FRA"))
    assert(computedGeoType === Success(GeoType.CONTINENTAL))
    computedGeoType =
      geoBase.getGeoType(List("CDG", "TLS", "DUB", "FRA", "CDG"))
    assert(computedGeoType === Success(GeoType.CONTINENTAL))

    computedGeoType = geoBase.getGeoType(List("CDG", "JFK"))
    assert(computedGeoType === Success(GeoType.INTER_CONTINENTAL))
    computedGeoType = geoBase.getGeoType(List("PAR", "JFK"))
    assert(computedGeoType === Success(GeoType.INTER_CONTINENTAL))
    computedGeoType = geoBase.getGeoType(List("PAR", "NYC"))
    assert(computedGeoType === Success(GeoType.INTER_CONTINENTAL))
    computedGeoType = geoBase.getGeoType(List("CDG", "TLS", "JFK", "MEX"))
    assert(computedGeoType === Success(GeoType.INTER_CONTINENTAL))

    computedGeoType = geoBase.getGeoType(List("FR", "FR"))
    assert(computedGeoType === Success(GeoType.DOMESTIC))
    computedGeoType = geoBase.getGeoType(List("FR", "FR", "DE"))
    assert(computedGeoType === Success(GeoType.CONTINENTAL))
    computedGeoType = geoBase.getGeoType(List("FR", "GB", "DE"))
    assert(computedGeoType === Success(GeoType.CONTINENTAL))
    computedGeoType = geoBase.getGeoType(List("FR", "PAR", "DUB"))
    assert(computedGeoType === Success(GeoType.CONTINENTAL))
    computedGeoType = geoBase.getGeoType(List("US", "PAR", "DUB"))
    assert(computedGeoType === Success(GeoType.INTER_CONTINENTAL))

    // Empty list of airports/cities:
    val invalidExceptionThrown = intercept[IllegalArgumentException] {
      geoBase.getGeoType(List("CDG")).get
    }
    val invalidExpectedMessage =
      "requirement failed: at least 2 locations are needed to compute a " +
        "geography type"
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
    assert(exceptionThrown.getMessage === "Unknown location \"bbb\"")

    // Unknown IATA zone for a country:
    exceptionThrown = intercept[GeoBaseException] {
      geoBase.getGeoType(List("FR", "XX")).get
    }
    assert(exceptionThrown.getMessage === "Unknown country \"XX\"")
    exceptionThrown = intercept[GeoBaseException] {
      geoBase.getGeoType(List("FR", "XX", "..")).get
    }
    assert(exceptionThrown.getMessage === "Unknown country \"XX\"")
  }

  test("Local Date to GMT Date") {

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

  test("Offset Getter from Local Date") {

    var offset = geoBase.getOffsetForLocalDate("20170712", "NCE")
    assert(offset === Success(120))
    offset = geoBase.getOffsetForLocalDate("20170712", "NCE", "yyyyMMdd")
    assert(offset === Success(120))
    offset = geoBase.getOffsetForLocalDate("2017-07-12", "NCE", "yyyy-MM-dd")
    assert(offset === Success(120))

    offset = geoBase.getOffsetForLocalDate("20171224", "NCE")
    assert(offset === Success(60))

    offset = geoBase.getOffsetForLocalDate("20171224", "NYC")
    assert(offset === Success(-300))
  }

  test("GMT Date to Local Date") {

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

  test("Trip Duration Getter Between two Local Dates") {

    // 1: Origin = destination and departure time = arrival date:
    var computedTripDuration = geoBase.getTripDurationFromLocalDates(
      "20160606_1627",
      "NCE",
      "20160606_1627",
      "NCE")
    assert(computedTripDuration === Success(0d))

    // 2: Within same time zone:
    computedTripDuration = geoBase.getTripDurationFromLocalDates(
      "20160606_1627",
      "NCE",
      "20160606_1757",
      "CDG")
    assert(computedTripDuration === Success(1.5d))

    // 3: With a different time zone:
    computedTripDuration = geoBase.getTripDurationFromLocalDates(
      "20160606_1627",
      "CDG",
      "20160606_1757",
      "JFK")
    assert(computedTripDuration === Success(7.5d))

    // 4: With a different time zone and a change of date:
    computedTripDuration = geoBase.getTripDurationFromLocalDates(
      "20160606_2327",
      "CDG",
      "20160607_0057",
      "JFK")
    assert(computedTripDuration === Success(7.5d))

    // 5: With an invalid origin city/ariport:
    var exceptionThrown = intercept[GeoBaseException] {
      geoBase
        .getTripDurationFromLocalDates(
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
        .getTripDurationFromLocalDates(
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
    computedTripDuration = geoBase.getTripDurationFromLocalDates(
      "20160606_1627",
      "CDG",
      "20160606_1757",
      "JFK",
      unit = "minutes")
    assert(computedTripDuration === Success(450d))

    // 8: With a specific format:
    computedTripDuration = geoBase.getTripDurationFromLocalDates(
      "2016-06-06T16:27",
      "CDG",
      "2016-06-06T17:57",
      "JFK",
      format = "yyyy-MM-dd'T'HH:mm")
    assert(computedTripDuration === Success(7.5d))

    // 9: With a specific format and in minutes:
    computedTripDuration = geoBase.getTripDurationFromLocalDates(
      "2016-06-06T16:27",
      "CDG",
      "2016-06-06T17:57",
      "JFK",
      format = "yyyy-MM-dd'T'HH:mm",
      unit = "minutes")
    assert(computedTripDuration === Success(450d))

    // 10: Let's try an invalid unit:
    val invalidExceptionThrown = intercept[IllegalArgumentException] {
      geoBase.getTripDurationFromLocalDates(
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

  test("Get Nearby Airports") {

    // 1: Invalid radius (negative):
    var exceptionThrown = intercept[IllegalArgumentException] {
      geoBase.getNearbyAirports("CDG", -50).get
    }
    val expectedMessage = "requirement failed: radius must be strictly positive"
    assert(exceptionThrown.getMessage === expectedMessage)

    // 2: Invalid radius (zero):
    exceptionThrown = intercept[IllegalArgumentException] {
      geoBase.getNearbyAirports("CDG", 0).get
    }
    assert(exceptionThrown.getMessage === expectedMessage)

    // 3: Normal use case:
    val expectedAirports = List("LBG", "CSF", "ORY", "VIY", "POX", "TNF")
    assert(geoBase.getNearbyAirports("CDG", 50) === Success(expectedAirports))

    // 4: Normal use case with the detail of distances:
    val computedAirports = geoBase.getNearbyAirportsWithDetails("CDG", 50)
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
    assert(geoBase.getNearbyAirports("CDG", 10) === Success(List("LBG")))

    // 6: No nearby airports:
    assert(geoBase.getNearbyAirports("CDG", 5) === Success(List()))

    // 7: Unknown location:
    val exceptionThrown2 = intercept[GeoBaseException] {
      geoBase.getNearbyAirports("...", 20).get
    }
    assert(exceptionThrown2.getMessage === "Unknown location \"...\"")
  }
}
