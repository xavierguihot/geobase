package com.geobase

import com.geobase.load.Loader
import com.geobase.error.{GeoBaseException => BGEx}
import com.geobase.model.{Duration, HOURS}
import com.geobase.model.{Airline, AirportOrCity, Country}
import com.geobase.model.{GeoType, DOMESTIC, CONTINENTAL, INTER_CONTINENTAL}

import scala.util.{Try, Success, Failure}

import java.text.SimpleDateFormat

import java.util.concurrent.TimeUnit
import java.util.TimeZone

import math.{asin, cos, pow, round, sin, sqrt}

import cats.implicits._

/** Provides '''geographical mappings''' at airport/city/country level mainly
  * based on <a href="https://github.com/opentraveldata/opentraveldata">
  * opentraveldata</a> as well as other mappings (airlines, currencies, ...).
  * This tool also provides classic time-oriented methods such as the
  * computation of a trip duration.
  *
  * Here are a few examples:
  *
  * {{{
  * import com.geobase.GeoBase
  *
  * GeoBase.city("CDG") // Success("PAR")
  * GeoBase.country("CDG") // Success("FR")
  * GeoBase.continent("JFK") // Success("NA")
  * GeoBase.iataZone("LON") // Success("21")
  * GeoBase.currency("NYC") // Success("USD")
  * GeoBase.countryForAirline("AF") // Success("FR")
  * GeoBase.timeZone("PAR") // Success("Europe/Paris")
  * GeoBase.distanceBetween("PAR", "NCE") // Success(686)
  * GeoBase.localDateToGMT("20160606_2227", "NYC") // Success("20160607_0227")
  * GeoBase.gmtDateToLocal("20160607_0227", "NYC") // Success("20160606_2227")
  * GeoBase.offsetForLocalDate("20171224", "NYC") // Success(-300)
  * GeoBase.tripDurationFromLocalDates("20160606_1627", "CDG", "20160606_1757", "JFK") // Success(7.5d)
  * GeoBase.geoType(List("CDG", "TLS", "DUB", "FRA")) // Success(CONTINENTAL)
  * GeoBase.nearbyAirports("CDG", 50) // Success(List("LBG", "ORY", "VIY", "POX"))
  * GeoBase.nameOfAirline("AF") // Success("Air France")
  * }}}
  *
  * and by pimping String:
  *
  * {{{
  * import com.geobase.GeoBase.StringExtensions
  *
  * "CDG".city // Success("PAR")
  * "PAR".country // Success("FR")
  * "CDG".continent // Success("EU")
  * "CDG".iataZone // Success("21")
  * "JFK".currency // Success("USD")
  * "AF".name // Success("Air France")
  * "CDG".timeZone // Success("Europe/Paris")
  * "LON".distanceWith("NYC") // Success(5568)
  * "CDG".nearbyAirports(50) // Success(List("LBG", "ORY", "VIY", "POX"))
  * }}}
  *
  * The GeoBase object can be used within Spark jobs (in this case, don't forget
  * to broadcast it.
  *
  * Opentraveldata is an accurate and maintained source of various travel
  * mappings. This scala wrapper uses this file:
  * <a href="https://github.com/opentraveldata/opentraveldata/tree/master/opentraveldata/optd_por_public.csv">
  * optd_por_public.csv</a>.
  *
  * Getters all have a return type embedded within the Try monad. Throwing
  * exceptions as is when one might request mappings for non existing locations,
  * isn't really the scala way, and simply embedding the result in the Option
  * monad doesn't give the user the possibility to understand what went wrong.
  * Thus the usage of the Try monad.
  *
  * Source <a href="https://github.com/xavierguihot/geobase/blob/master/src/main/scala/com/geobase/GeoBase.scala">
  * GeoBase</a>
  *
  * @author Xavier Guihot
  * @since 2016-05
  */
object GeoBase extends Serializable {

  private lazy val airportsAndCities: Map[String, AirportOrCity] =
    Loader.loadAirportsAndCities()
  private lazy val countries: Map[String, Country] = Loader.loadCountries()
  private lazy val airlines: Map[String, Airline] = Loader.loadAirlines()

  /** Returns the city associated to the given airport.
    *
    * {{{
    * assert(GeoBase.city("CDG") == Success("PAR"))
    * assert(GeoBase.city("?*#") == Failure(GeoBaseException: Unknown airport "?*#")
    * }}}
    *
    * @param airport the airport IATA code (for instance CDG) for which to get
    * the associated city.
    * @return the city code corresponding to the given airport (for instance
    * PAR).
    */
  def city(airport: String): Try[String] =
    airportsAndCities
      .get(airport)
      .map(_.city)
      .getOrElse(Failure(BGEx("Unknown airport \"" + airport + "\"")))

  /** Returns the cities associated to the given airport.
    *
    * It sometimes happens that an airport is shared between cities. This
    * method, returns this list of cities (usually the list will only contain
    * one city).
    *
    * The method city returns the first city corresponding to the given
    * airport, which is by assumption the biggest corresponding city.
    *
    * {{{
    * assert(GeoBase.cities("CDG") == Success(List("PAR")))
    * assert(GeoBase.cities("AZA") == Success(List("PHX", "MSC")))
    * assert(GeoBase.cities("?*#") == Failure(GeoBaseException: Unknown airport "?*#")
    * }}}
    *
    * @param airport the airport IATA code (for instance AZA) for which to get
    * the associated cities.
    * @return the list of city codes corresponding to the given airport (for
    * instance List("PHX", "MSC")).
    */
  def cities(airport: String): Try[List[String]] =
    airportsAndCities
      .get(airport)
      .map(_.cities)
      .getOrElse(Failure(BGEx("Unknown airport \"" + airport + "\"")))

  /** Returns the country associated to the given location (city or airport).
    *
    * {{{
    * assert(GeoBase.country("PAR") == Success("FR"))
    * assert(GeoBase.country("ORY") == Success("FR"))
    * assert(GeoBase.country("?*#") == Failure(GeoBaseException: Unknown location "?*#"))
    * }}}
    *
    * @param location the location IATA code (city or airport - for instance
    * PAR) for which to get the associated country.
    * @return the country code corresponding to the given city or airport (for
    * instance FR).
    */
  def country(location: String): Try[String] = location.length match {

    // If it's already a country-like code:
    case 2 => Success(location)

    // If it's a city/airport code, we transform it to a country:
    case 3 =>
      airportsAndCities
        .get(location)
        .map(_.country)
        .getOrElse(Failure(BGEx("Unknown location \"" + location + "\"")))

    case _ => Failure(BGEx("Unknown location \"" + location + "\""))
  }

  /** Returns the continent associated to the given airport, city or country.
    *
    * Possible values: EU (Europe) - NA (North America) - SA (South Africa) -
    * AF (Africa) - AS (Asia) - AN (Antarctica) - OC (Oceania).
    *
    * {{{
    * assert(GeoBase.continent("CDG") == Success("EU")) // location is an airport
    * assert(GeoBase.continent("NYC") == Success("NA")) // location is a city
    * assert(GeoBase.continent("CN") == Success("AS")) // location is a country
    * assert(GeoBase.continent("?*#") == Failure(GeoBaseException: Unknown location "?*#"))
    * }}}
    *
    * @param location the country, city or airport IATA code (for instance PAR)
    * for which to get the associated continent.
    * @return the continent code corresponding to the given location (for
    * instance EU).
    */
  def continent(location: String): Try[String] =
    for {

      country <- location.country

      continent <- countries
        .get(country)
        .map(_.continent)
        .getOrElse(Failure(BGEx("Unknown country \"" + country + "\"")))

    } yield continent

  /** Returns the IATA zone associated to the given airport, city or country.
    *
    * Possible values are 11, 12, 13, 21, 22, 23, 31, 32 or 33.
    *
    * {{{
    * assert(GeoBase.iataZone("CDG") == Success("21"))
    * assert(GeoBase.iataZone("NYC") == Success("11"))
    * assert(GeoBase.iataZone("ZA") == Success("23"))
    * assert(GeoBase.iataZone("?*#") == Failure(GeoBaseException: Unknown location "?*#"))
    * }}}
    *
    * @param location the country, city or airport IATA code (for instance PAR)
    * for which to get the associated IATA zone.
    * @return the IATA zone code corresponding to the given location (for
    * instance 21).
    */
  def iataZone(location: String): Try[String] =
    for {

      country <- location.country

      iataZone <- countries
        .get(country)
        .map(_.iataZone)
        .getOrElse(Failure(BGEx("Unknown country \"" + country + "\"")))

    } yield iataZone

  /** Returns the currency associated to the given location (airport, city or
    * country).
    *
    * {{{
    * assert(GeoBase.currency("JFK") == Success("USD"))
    * assert(GeoBase.currency("FR") == Success("EUR"))
    * assert(GeoBase.currency("?#") == Failure(GeoBaseException: Unknown country "#?"))
    * }}}
    *
    * @param location the country, city or airport IATA code (for instance FR)
    * for which to get the associated currency.
    * @return the currency code corresponding to the given location (for
    * instance EUR).
    */
  def currency(location: String): Try[String] =
    for {

      country <- location.country

      currency <- countries
        .get(country)
        .map(_.currency)
        .getOrElse(Failure(BGEx("Unknown country \"" + country + "\"")))

    } yield currency

  /** Returns the country associated to the given airline.
    *
    * {{{
    * assert(GeoBase.countryForAirline("AF") == Success("FR"))
    * assert(GeoBase.countryForAirline("#?") == Failure(GeoBaseException: Unknown airline "#?"))
    * }}}
    *
    * @param airline the airline IATA code (for instance AF) for which to get
    * the associated country.
    * @return the country code corresponding to the given airline (for instance
    * FR).
    */
  def countryForAirline(airline: String): Try[String] =
    airlines
      .get(airline)
      .map(_.country)
      .getOrElse(Failure(BGEx("Unknown airline \"" + airline + "\"")))

  /** Returns the name associated to the given airline.
    *
    * {{{
    * assert(GeoBase.nameOfAirline("AF") == Success("Air France"))
    * assert(GeoBase.nameOfAirline("#?") == Failure(GeoBaseException: Unknown airline "#?"))
    * }}}
    *
    * @param airline the airline IATA code (for instance AF) for which to get
    * the associated airline name.
    * @return the name corresponding to the given airline (for instance
    * Air France).
    */
  def nameOfAirline(airline: String): Try[String] =
    airlines
      .get(airline)
      .map(_.name)
      .getOrElse(Failure(BGEx("Unknown airline \"" + airline + "\"")))

  /** Returns the time zone associated to the given airport or city.
    *
    * {{{
    * assert(GeoBase.timeZone("CDG") == Success("Europe/Paris"))
    * assert(GeoBase.timeZone("BOS") == Success("America/New_York"))
    * assert(GeoBase.timeZone("?*#") == Failure(GeoBaseException: Unknown location "?*#"))
    * }}}
    *
    * @param location the city or airport IATA code (for instance PAR) for which
    * to get the associated time zone.
    * @return the time zone corresponding to the given location (for instance
    * Europe/Paris).
    */
  def timeZone(location: String): Try[String] =
    airportsAndCities
      .get(location)
      .map(_.timeZone)
      .getOrElse(Failure(BGEx("Unknown location \"" + location + "\"")))

  /** Returns the distance between two locations (airports/cities).
    *
    * {{{
    * assert(GeoBase.distanceBetween("ORY", "NCE") == Success(674))
    * assert(GeoBase.distanceBetween("PAR", "NCE") == Success(686))
    * assert(GeoBase.distanceBetween("PAR", "~#?") == Failure(GeoBaseException: Unknown location "~#?"))
    * }}}
    *
    * @param locationA an airport or city IATA code (for instance ORY) for which
    * to get the distance with locationB.
    * @param locationB an airport or city IATA code (for instance NCE) for which
    * to get the distance with locationA.
    * @return the distance rounded in km between locationA and locationB (for
    * instance 674 km).
    */
  def distanceBetween(locationA: String, locationB: String): Try[Int] =
    for {

      locationDetailsA <- airportsAndCities
        .get(locationA)
        .map(Success(_))
        .getOrElse(Failure(BGEx("Unknown location \"" + locationA + "\"")))
      locationDetailsB <- airportsAndCities
        .get(locationB)
        .map(Success(_))
        .getOrElse(Failure(BGEx("Unknown location \"" + locationB + "\"")))

      latA <- locationDetailsA.latitude
      lngA <- locationDetailsA.longitude
      latB <- locationDetailsB.latitude
      lngB <- locationDetailsB.longitude

    } yield
      round(
        2 * 6371 * asin(
          sqrt(
            pow(sin(0.5 * (latA - latB)), 2) +
              pow(sin(0.5 * (lngA - lngB)), 2) * cos(latA) * cos(latB)
          ))
      ).toInt

  /** Transforms a local date (at a given location) into a GMT date.
    *
    * Here we bring more than just converting the local time to GMT. The
    * additional value is the knowledge of the time zone thanks to
    * opentraveldata. You don't need to know the time zone, just enter the
    * airport or the city as a parameter.
    *
    * {{{
    * assert(GeoBase.localDateToGMT("20160606_2227", "NYC") == Success("20160607_0227"))
    * assert(GeoBase.localDateToGMT("2016-06-06T22:27", "NYC", "yyyy-MM-dd'T'HH:mm") == Success("2016-06-07T02:27"))
    * assert(GeoBase.localDateToGMT("20160606_2227", "~#?") == Failure(GeoBaseException: Unknown location "~#?"))
    * }}}
    *
    * @param localDate the local date at the given location under the given
    * format.
    * @param location the airport or city where this local date applies
    * @param format (default = "yyyyMMdd_HHmm") the format under which localDate
    * is provided and the GMT date is returned.
    * @return the GMT date associated to the local date under the requested
    * format.
    */
  def localDateToGMT(
      localDate: String,
      location: String,
      format: String = "yyyyMMdd_HHmm"
  ): Try[String] = {

    timeZone(location).map(timeZone => {

      val inputLocalDateParser = new SimpleDateFormat(format)
      inputLocalDateParser.setTimeZone(TimeZone.getTimeZone(timeZone))

      val outputGMTDateFormatter = new SimpleDateFormat(format)
      outputGMTDateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"))

      outputGMTDateFormatter.format(inputLocalDateParser.parse(localDate))
    })
  }

  /** Returns the offset in minutes for the given date at the given city/airport.
    *
    * {{{
    * assert(GeoBase.offsetForLocalDate("20170712", "NCE") == Success(120))
    * assert(GeoBase.offsetForLocalDate("2017-07-12", "NCE", "yyyy-MM-dd") == Success(120))
    * assert(GeoBase.offsetForLocalDate("20171224", "NCE") == Success(60))
    * assert(GeoBase.offsetForLocalDate("20171224", "NYC") == Success(-300))
    * assert(GeoBase.offsetForLocalDate("20171224", "~#?") == Failure(GeoBaseException: Unknown location "~#?"))
    * }}}
    *
    * @param localDate the local date
    * @param location the airport or the city where this local date applies
    * @param format (default = "yyyyMMdd") the format under which localDate is
    * provided.
    * @return the the offset in minutes for the given date at the given
    * city/airport (can be negative).
    */
  def offsetForLocalDate(
      localDate: String,
      location: String,
      format: String = "yyyyMMdd"
  ): Try[Int] =
    timeZone(location).map { timeZone =>
      val dateTime = new SimpleDateFormat(format).parse(localDate).getTime
      TimeZone.getTimeZone(timeZone).getOffset(dateTime) / 60000
    }

  /** Transforms a GMT date into a local date for the given airport or city.
    *
    * Here we bring more than just converting the GMT time to local. The
    * additional value is the knowledge of the time zone thanks to
    * opentraveldata. You don't need to know the time zone, just enter the
    * airport or the city as a parameter.
    *
    * {{{
    * assert(GeoBase.gmtDateToLocal("20160606_1427", "NCE") == Success("20160606_1627"))
    * assert(GeoBase.gmtDateToLocal("2016-06-07T02:27", "NYC", "yyyy-MM-dd'T'HH:mm") == Success("2016-06-06T22:27"))
    * assert(GeoBase.gmtDateToLocal("20160606_2227", "~#?") == Failure(GeoBaseException: Unknown location "~#?"))
    * }}}
    *
    * @param gmtDate the GMT date
    * @param location the airport or the city where this GMT date is to be
    * localized.
    * @param format (default = "yyyyMMdd_HHmm") the format under which gmtDate
    * is provided and the local date is returned.
    * @return the local date associated to the GMT date under the requested
    * format.
    */
  def gmtDateToLocal(
      gmtDate: String,
      location: String,
      format: String = "yyyyMMdd_HHmm"
  ): Try[String] = {

    timeZone(location).map(timeZone => {

      val inputGMTDateParser = new SimpleDateFormat(format)
      inputGMTDateParser.setTimeZone(TimeZone.getTimeZone("GMT"))

      val outputLocalDateFormatter = new SimpleDateFormat(format)
      outputLocalDateFormatter.setTimeZone(TimeZone.getTimeZone(timeZone))

      outputLocalDateFormatter.format(inputGMTDateParser.parse(gmtDate))
    })
  }

  /** Returns the trip duration between two locations (airport or city).
    *
    * In the travel industry, the trip duration is synonym with elapsed flying
    * time (EFT).
    *
    * This is meant to be used to compute the trip duration for a segment/bound
    * for which we know the origin/destination airports/cities and the local
    * time. i.e. when we don't have gmt times.
    *
    * {{{
    * assert(GeoBase.tripDurationFromLocalDates(
    *   "20160606_1627", "CDG", "20160606_1757", "JFK") == Success(7.5d))
    *
    * val computedTripDuration = GeoBase.tripDurationFromLocalDates(
    *   "2016-06-06T16:27", "CDG", "2016-06-06T17:57", "JFK",
    *   format = "yyyy-MM-dd'T'HH:mm", unit = MINUTES
    * )
    * assert(computedTripDuration == Success(450d))
    *
    * assert(geoBase
    *   .tripDurationFromLocalDates("20160606_1627", "~#?", "20160606_1757", "JFK")
    *     == Failure(GeoBaseException: Unknown location "~#?"))
    * }}}
    *
    * @param localDepartureDate the departure local date
    * @param originLocation the origin airport or city
    * @param localArrivalDate the arrival local date
    * @param destinationLocation the destination airport or city
    * @param unit (default = com.geobase.model.HOURS) either HOURS or MINUTES
    * @param format (default = "yyyyMMdd_HHmm") the format under which local
    * departure and arrival dates are provided.
    * @return the trip duration in the chosen unit (in hours by default) and
    * format.
    */
  def tripDurationFromLocalDates(
      localDepartureDate: String,
      originLocation: String,
      localArrivalDate: String,
      destinationLocation: String,
      unit: Duration = HOURS,
      format: String = "yyyyMMdd_HHmm"
  ): Try[Double] = {

    for {

      gmtDepDate <- localDateToGMT(localDepartureDate, originLocation, format)
      gmtArrDate <- localDateToGMT(
        localArrivalDate,
        destinationLocation,
        format
      )

      tripDuration <- {

        val formatter = new SimpleDateFormat(format)

        val depDate = formatter.parse(gmtDepDate)
        val arrDate = formatter.parse(gmtArrDate)

        val tripDurationMillis = arrDate.getTime - depDate.getTime

        if (tripDurationMillis < 0)
          Failure(
            BGEx(
              "The trip duration computed is negative (maybe you've " +
                "inverted departure/origin and arrival/destination)"))
        else
          Success(
            TimeUnit.MINUTES.convert(tripDurationMillis, TimeUnit.MILLISECONDS))
      }

    } yield if (unit == HOURS) tripDuration / 60d else tripDuration
  }

  /** Returns the geo type of a trip (domestic, continental or inter continental).
    *
    * Possible returned values: DOMESTIC, CONTINENTAL or INTER_CONTINENTAL.
    *
    * The distinction between continental and intercontinental is made based on
    * iata zones.
    *
    * {{{
    * assert(GeoBase.geoType(List("CDG", "ORY")) == Success(DOMESTIC))
    * assert(GeoBase.geoType(List("FR", "FR")) == Success(DOMESTIC))
    * assert(GeoBase.geoType(List("FR", "PAR", "DUB")) == Success(CONTINENTAL))
    * assert(GeoBase.geoType(List("CDG", "TLS", "JFK", "MEX")) == Success(INTER_CONTINENTAL))
    * assert(GeoBase.geoType(List("US", "bbb", "NCE", "aaa")) == Failure(GeoBaseException: Unknown locations \"bbb\", \"aaa\"))
    * }}}
    *
    * @param locations a list of cities/airports/countries representing the trip
    * @return the type of the trip (a GeoType "enum" value, such as DOMESTIC)
    */
  def geoType(locations: List[String]): Try[GeoType] = {

    require(
      locations.length >= 2,
      "at least 2 locations are needed to compute a geography type"
    )

    for {

      countries <- locations.traverse(country)

      distCountries = countries.distinct

      geo <- distCountries.length match {

        case 1 => Success(DOMESTIC)

        case _ =>
          for {

            iataZones <- distCountries.traverse(iataZone)

            distIataZones = iataZones.distinct

            geo = distIataZones.length match {
              case 1 => CONTINENTAL
              case _ => INTER_CONTINENTAL
            }

          } yield geo
      }

    } yield geo
  }

  /** Returns the list of nearby airports (within the radius) for the given
    * airport or city.
    *
    * Find the list of nearby airports, within the requested radius. The list is
    * sorted starting from the closest airport.
    *
    * {{{
    * assert(GeoBase.nearbyAirports("CDG", 50) == Success(List("LBG", "ORY", "VIY", "POX")))
    * assert(GeoBase.nearbyAirports("CDG", 36) == Success(List("LBG", "ORY")))
    * assert(GeoBase.nearbyAirports("~#?", 36)) == Failure(GeoBaseException: Unknown location \"~#?\""))
    * }}}
    *
    * @param location the airport or city for which to find nearby airports
    * @param radius the maximum distance (in km) for which an airport is
    * considered close.
    * @return the sorted per increasing distance list nearby airports
    */
  def nearbyAirports(location: String, radius: Int): Try[List[String]] =
    for {
      nearbyAirports <- nearbyAirportsWithDetails(location, radius)
    } yield nearbyAirports.map(_._1)

  /** Returns the list of nearby airports (within the radius) for the given
    * airport or city.
    *
    * Find the list of nearby airports, within the requested radius. The list is
    * sorted starting from the closest airport. This list is a tuple of
    * (airport/distance).
    *
    * {{{
    * assert(GeoBase.nearbyAirportsWithDetails("CDG", 50) == Success(List(("LBG", 9), ("ORY", 35), ("VIY", 37), ("POX", 38))))
    * assert(GeoBase.nearbyAirportsWithDetails("CDG", 36) == Success(List(("LBG", 9), ("ORY", 35))))
    * assert(GeoBase.nearbyAirportsWithDetails("~#?", 36)) == Failure(GeoBaseException: Unknown location \"~#?\""))
    * }}}
    *
    * @param location the airport or city for which to find nearby airports.
    * @param radius the maximum distance (in km) for which an airport is
    * considered close.
    * @return the sorted per increasing distance list of tuples (airport,
    * distance).
    */
  def nearbyAirportsWithDetails(
      location: String,
      radius: Int
  ): Try[List[(String, Int)]] = {

    require(radius > 0, "radius must be strictly positive")

    // We check whether the given location is known:
    if (airportsAndCities.contains(location)) {

      val nearbyAirports = for {

        randomLocation <- airportsAndCities.keys.toList
        if airportsAndCities(randomLocation).isAirport

        distance = distanceBetween(location, randomLocation).getOrElse(-1)
        if distance > 0
        if distance <= radius

      } yield (randomLocation, distance)

      Success(nearbyAirports.sortWith(_._2 < _._2)) // Sorted per increasing radius
    }
    else
      Failure(BGEx("Unknown location \"" + location + "\""))
  }

  implicit class StringExtensions(val string: String) extends AnyVal {

    /** Returns the city associated to the given airport.
      *
      * {{{
      * assert("CDG".city == Success("PAR"))
      * assert("?*#".city == Failure(GeoBaseException: Unknown airport "?*#")
      * }}}
      *
      * @return the city code corresponding to the given airport (for instance
      * PAR).
      */
    def city: Try[String] = GeoBase.city(string)

    /** Returns the cities associated to the given airport.
      *
      * It sometimes happens that an airport is shared between cities. This
      * method, returns this list of cities (usually the list will only contain
      * one city).
      *
      * The city method returns the first city corresponding to the given
      * airport, which is by assumption the biggest corresponding city.
      *
      * {{{
      * assert("CDG".cities == Success(List("PAR")))
      * assert("AZA".cities == Success(List("PHX", "MSC")))
      * assert("?*#".cities == Failure(GeoBaseException: Unknown airport "?*#")
      * }}}
      *
      * @return the list of city codes corresponding to the given airport (for
      * instance List("PHX", "MSC")).
      */
    def cities: Try[List[String]] = GeoBase.cities(string)

    /** Returns the country associated to the given location (city or airport).
      *
      * {{{
      * assert("PAR".country == Success("FR"))
      * assert("ORY".country == Success("FR"))
      * assert("?*#".country == Failure(GeoBaseException: Unknown location "?*#"))
      * }}}
      *
      * @return the country code corresponding to the given city or airport (for
      * instance FR).
      */
    def country: Try[String] = GeoBase.country(string)

    /** Returns the continent associated to the given airport, city or country.
      *
      * Possible values: EU (Europe) - NA (North America) - SA (South Africa) -
      * AF (Africa) - AS (Asia) - AN (Antarctica) - OC (Oceania).
      *
      * {{{
      * assert("CDG".continent == Success("EU")) // location is an airport
      * assert("NYC".continent == Success("NA")) // location is a city
      * assert("CN".continent == Success("AS")) // location is a country
      * assert("?*#".continent == Failure(GeoBaseException: Unknown location "?*#"))
      * }}}
      *
      * @return the continent code corresponding to the given location (for
      * instance EU).
      */
    def continent: Try[String] = GeoBase.continent(string)

    /** Returns the IATA zone associated to the given airport, city or country.
      *
      * Possible values are 11, 12, 13, 21, 22, 23, 31, 32 or 33.
      *
      * {{{
      * assert("CDG".iataZone == Success("21"))
      * assert("NYC".iataZone == Success("11"))
      * assert("ZA".iataZone == Success("23"))
      * assert("?*#".iataZone == Failure(GeoBaseException: Unknown location "?*#"))
      * }}}
      *
      * @return the IATA zone code corresponding to the given location (for
      * instance 21).
      */
    def iataZone: Try[String] = GeoBase.iataZone(string)

    /** Returns the currency associated to the given location (airport, city or
      * country).
      *
      * {{{
      * assert("JFK".currency == Success("USD"))
      * assert("FR".currency == Success("EUR"))
      * assert("?#".currency == Failure(GeoBaseException: Unknown country "#?"))
      * }}}
      *
      * @return the currency code corresponding to the given location (for
      * instance EUR).
      */
    def currency: Try[String] = GeoBase.currency(string)

    /** Returns the name associated to the given airline.
      *
      * {{{
      * assert("AF".name == Success("Air France"))
      * assert("#?".name == Failure(GeoBaseException: Unknown airline "#?"))
      * }}}
      *
      * @return the name corresponding to the given airline (for instance
      * Air France).
      */
    def name: Try[String] = GeoBase.nameOfAirline(string)

    /** Returns the time zone associated to the given airport or city.
      *
      * {{{
      * assert("CDG".timeZone == Success("Europe/Paris"))
      * assert("BOS".timeZone == Success("America/New_York"))
      * assert("?*#".timeZone == Failure(GeoBaseException: Unknown location "?*#"))
      * }}}
      *
      * @return the time zone corresponding to the given location (for instance
      * Europe/Paris).
      */
    def timeZone: Try[String] = GeoBase.timeZone(string)

    /** Returns the distance between two locations (airports/cities).
      *
      * {{{
      * assert("ORY".distanceWith("NCE") == Success(674))
      * assert("PAR".distanceWith("NCE") == Success(686))
      * assert("PAR".distanceWith("~#?") == Failure(GeoBaseException: Unknown location "~#?"))
      * }}}
      *
      * @param location an airport or city IATA code (for instance NCE) for
      * which to get the distance with the location this is called for.
      * @return the distance rounded in km between locationA and locationB (for
      * instance 674 km).
      */
    def distanceWith(location: String): Try[Int] =
      GeoBase.distanceBetween(string, location)

    /** Returns the list of nearby airports (within the radius) for the given
      * airport or city.
      *
      * Find the list of nearby airports, within the requested radius. The list
      * is sorted starting from the closest airport.
      *
      * {{{
      * assert("CDG".nearbyAirports(50) == Success(List("LBG", "ORY", "VIY", "POX")))
      * assert("CDG".nearbyAirports(36) == Success(List("LBG", "ORY")))
      * assert("~#?".nearbyAirports(36)) == Failure(GeoBaseException: Unknown location \"~#?\""))
      * }}}
      *
      * @param radius the maximum distance (in km) for which an airport is
      * considered close.
      * @return the sorted per increasing distance list nearby airports
      */
    def nearbyAirports(radius: Int): Try[List[String]] =
      GeoBase.nearbyAirports(string, radius)
  }
}
