package com.geobase

import com.geobase.load.Loader
import com.geobase.error.GeoBaseException
import com.geobase.model.{Airline, AirportOrCity, Country}

import scala.util.{Try, Success, Failure}

import java.text.SimpleDateFormat

import java.util.concurrent.TimeUnit
import java.util.TimeZone

import java.security.InvalidParameterException

import math.{asin, cos, pow, round, sin, sqrt}

/** A facility to '''deal with travel/geographical data'''.
  *
  * Provides '''geographical mappings''' at airport/city/country level mainly
  * based on <a href="https://github.com/opentraveldata/opentraveldata">
  * opentraveldata</a> as well as other mappings (airlines, currencies, ...).
  * This tool also provides classic time-oriented methods such as the
  * computation of a trip duration.
  *
  * Here are a few exemples:
  *
  * {{{
  * import com.geobase.GeoBase
  *
  * val geoBase = new GeoBase()
  *
  * assert(geoBase.getCityFor("CDG").get == "PAR")
  * assert(geoBase.getCountry("CDG").get == "FR")
  * assert(geoBase.getCurrencyFor("NYC").get == "USD")
  * assert(geoBase.getCountryForAirline("AF").get == "FR")
  * assert(geoBase.getDistanceBetween("PAR", "NCE").get == 686)
  * assert(geoBase.getTripDurationFromLocalDates("20160606_1627", "CDG", "20160606_1757", "JFK").get == 7.5d)
  * assert(geoBase.getNearbyAirports("CDG", 50).get == List("LBG", "ORY", "VIY", "POX"))
  * }}}
  *
  * The GeoBase object can be used within Spark jobs (in this case, don't forget
  * the possibility to '''broadcast GeoBase''').
  *
  * Opentraveldata is an accurate and maintained source of various travel
  * mappings. This scala wrapper around opentraveldata mostly uses this
  * file: <a href="https://github.com/opentraveldata/opentraveldata/tree/master/opentraveldata/optd_por_public.csv">
  * optd_por_public.csv</a>.
  *
  * Getters all have a return type embedded within the Try monade. Throwing
  * exceptions as is when one might request mappings for non existing locations,
  * isn't realy the scala way, and simply embedding the result in the Option
  * monad doesn't give the user the possibility to understand what went wrong.
  * Thus the usage of the Try monade.
  *
  * Source <a href="https://github.com/XavierGuihot/geobase/blob/master/src/main/scala/com/geobase/GeoBase.scala">
  * GeoBase</a>
  *
  * @author Xavier Guihot
  * @since 2016-05
  *
  * @constructor Creates a GeoBase object.
  */
class GeoBase() extends Serializable {

	private lazy val airportsAndCities: Map[String, AirportOrCity] = Loader.loadAirportsAndCities()
	private lazy val countries: Map[String, Country] = Loader.loadCountries()
	private lazy val airlines: Map[String, Airline] = Loader.loadAirlines()

	import GeoType._

	/** Returns the city associated to the given airport.
	  *
	  * {{{
	  * assert(geoBase.getCityFor("CDG") == Success("PAR"))
	  * assert(geoBase.getCityFor("CDG").get == "PAR")
	  * assert(geoBase.getCityFor("?*#") == Failure(GeoBaseException: Unknown airport "?*#")
	  * assert(geoBase.getCityFor("?*#").getOrElse("") == "")
	  * }}}
	  *
	  * @param airport the airport IATA code (for instance CDG) for which to get
	  * the associated city.
	  * @return the city code corresponding to the given airport (for instance
	  * PAR).
	  */
	def getCityFor(airport: String): Try[String] = {

		airportsAndCities.get(airport) match {
			case Some(airportInfo) => airportInfo.getCity()
			case None => Failure(GeoBaseException("Unknown airport \"" + airport + "\""))
		}
	}

	/** Returns the cities associated to the given airport.
	  *
	  * It sometimes happens that an airport is shared between cities. This
	  * method, returns this list of cities (usually the list will only contain
	  * one city).
	  *
	  * The method getCityFor returns the first city corresponding to the
	  * given airport, which is by assumption the biggest corresponding city.
	  *
	  * {{{
	  * assert(geoBase.getCitiesFor("CDG") == Success(List("PAR")))
	  * assert(geoBase.getCitiesFor("CDG").get == List("PAR"))
	  * assert(geoBase.getCitiesFor("AZA") == Success(List("PHX", "MSC")))
	  * assert(geoBase.getCitiesFor("?*#") == Failure(GeoBaseException: Unknown airport "?*#")
	  * assert(geoBase.getCitiesFor("?*#").getOrElse(Nil) == List())
	  * }}}
	  *
	  * @param airport the airport IATA code (for instance AZA) for which to get
	  * the associated cities.
	  * @return the list of city codes corresponding to the given airport (for
	  * instance List("PHX", "MSC")).
	  */
	def getCitiesFor(airport: String): Try[List[String]] = {

		airportsAndCities.get(airport) match {
			case Some(airportInfo) => airportInfo.getCities()
			case None => Failure(GeoBaseException("Unknown airport \"" + airport + "\""))
		}
	}

	/** Returns the country associated to the given location (city or airport).
	  *
	  * {{{
	  * assert(geoBase.getCountryFor("PAR") == Success("FR"))
	  * assert(geoBase.getCountryFor("PAR").get == "FR")
	  * assert(geoBase.getCountryFor("ORY").get == "FR")
	  * assert(geoBase.getCountryFor("?*#") == Failure(GeoBaseException: Unknown location "?*#"))
	  * assert(geoBase.getCountryFor("?*#").getOrElse("") == "")
	  * }}}
	  *
	  * @param location the location IATA code (city or airport - for instance
	  * PAR) for which to get the associated country.
	  * @return the country code corresponding to the given city or airport (for
	  * instance FR).
	  */
	def getCountryFor(location: String): Try[String] = {

		location.length match {

			// If it's already a country-like code:
			case 2 => Success(location)

			// If it's a city/airport code, we transform it to a country:
			case 3 => airportsAndCities.get(location) match {
				case Some(locationInfo) => locationInfo.getCountry()
				case None => Failure(GeoBaseException("Unknown location \"" + location + "\""))
			}

			case _ => Failure(GeoBaseException("Unknown location \"" + location + "\""))
		}
	}

	/** Returns the continent associated to the given airport, city or country.
	  *
	  * Possible values: EU (Eurrope) - NA (North America) - SA (South Africa) -
	  * AF (Africa) - AS (Asia) - AN (Antarctica) - OC (Oceania).
	  *
	  * {{{
	  * assert(geoBase.getContinentFor("CDG") == Success("EU")) // location is an airport
	  * assert(geoBase.getContinentFor("NYC").get == "NA") // location is a city
	  * assert(geoBase.getContinentFor("CN").get == "AS") // location is a country
	  * assert(geoBase.getContinentFor("?*#") == Failure(GeoBaseException: Unknown location "?*#"))
	  * assert(geoBase.getContinentFor("?*#").getOrElse("") == "")
	  * }}}
	  *
	  * @param location the country, city or airport IATA code (for instance
	  * PAR) for which to get the associated continent.
	  * @return the continent code corresponding to the given location (for
	  * instance EU).
	  */
	def getContinentFor(location: String): Try[String] = {

		// We transform the location (city, airport or country) into a country:
		getCountryFor(location) match {

			case Success(country) =>
				countries.get(country) match {
					case Some(countryDetails) => countryDetails.getContinent()
					case None => Failure(GeoBaseException("Unknown country \"" + country + "\""))
				}

			case Failure(exception) => Failure(exception)
		}
	}

	/** Returns the IATA zone associated to the given airport, city or country.
	  *
	  * Possible values are 11, 12, 13, 21, 22, 23, 31, 32 or 33.
	  *
	  * {{{
	  * assert(geoBase.getIataZoneFor("CDG") == Success("21"))
	  * assert(geoBase.getIataZoneFor("NYC").get == "11")
	  * assert(geoBase.getIataZoneFor("ZA").get == "23")
	  * assert(geoBase.getIataZoneFor("?*#") == Failure(GeoBaseException: Unknown location "?*#"))
	  * assert(geoBase.getIataZoneFor("?*#").getOrElse("") == "")
	  * }}}
	  *
	  * @param location the country, city or airport IATA code (for instance
	  * PAR) for which to get the associated IATA zone.
	  * @return the IATA zone code corresponding to the given location (for
	  * instance 21).
	  */
	def getIataZoneFor(location: String): Try[String] = {

		// We transform the location (city, airport or country) into a country:
		getCountryFor(location) match {

			case Success(country) =>
				countries.get(country) match {
					case Some(countryDetails) => countryDetails.getIataZone()
					case None => Failure(GeoBaseException("Unknown country \"" + country + "\""))
				}

			case Failure(exception) => Failure(exception)
		}
	}

	/** Returns the currency associated to the given location (airport, city or country).
	  *
	  * {{{
	  * assert(geoBase.getCurrencyFor("JFK") == Success("USD"))
	  * assert(geoBase.getCurrencyFor("FR").get == "EUR")
	  * assert(geoBase.getCurrencyFor("?#").get == Failure(GeoBaseException: Unknown country "#?"))
	  * assert(geoBase.getCurrencyFor("?#").getOrElse("USD") == "USD")
	  * }}}
	  *
	  * @param location the country, city or airport IATA code (for instance FR)
	  * for which to get the associated currency.
	  * @return the currency code corresponding to the given location (for
	  * instance EUR).
	  */
	def getCurrencyFor(location: String): Try[String] = {

		// We transform the location (city, airport or country) into a country:
		getCountryFor(location) match {

			case Success(country) =>
				countries.get(country) match {
					case Some(countryDetails) => countryDetails.getCurrency()
					case None => Failure(GeoBaseException("Unknown country \"" + country + "\""))
				}

			case Failure(exception) => Failure(exception)
		}
	}

	/** Returns the country associated to the given airline.
	  *
	  * {{{
	  * assert(geoBase.getCountryForAirline("AF") == Success("FR"))
	  * assert(geoBase.getCountryForAirline("AF").get == "FR")
	  * assert(geoBase.getCountryForAirline("#?") == Failure(GeoBaseException: Unknown airline "#?"))
	  * assert(geoBase.getCountryForAirline("#?").getOrElse("") == "")
	  * }}}
	  *
	  * @param airline the airline IATA code (for instance AF) for which to get
	  * the associated country.
	  * @return the country code corresponding to the given airline (for
	  * instance FR).
	  */
	def getCountryForAirline(airline: String): Try[String] = {

		airlines.get(airline) match {
			case Some(airline) => airline.getCountry()
			case None => Failure(GeoBaseException("Unknown airline \"" + airline + "\""))
		}
	}

	/** Returns the distance between two locations (airports/cities).
	  *
	  * {{{
	  * assert(geoBase.getDistanceBetween("ORY", "NCE") == Success(674))
	  * assert(geoBase.getDistanceBetween("PAR", "NCE").get == 686)
	  * assert(geoBase.getDistanceBetween("PAR", "~#?").get == Failure(GeoBaseException: Unknown location "~#?"))
	  * assert(geoBase.getDistanceBetween("PAR", "~#?").getOrElse(-1) == -1)
	  * }}}
	  *
	  * @param locationA an airport or city IATA code (for instance ORY) for
	  * which to get the distance with locationB.
	  * @param locationB an airport or city IATA code (for instance NCE) for
	  * which to get the distance with locationA.
	  * @return the distance rounded in km between locationA and locationB (for
	  * instance 674 km).
	  */
	def getDistanceBetween(locationA: String, locationB: String): Try[Int] = {

		val locationDetailsA = airportsAndCities.get(locationA)
		val locationDetailsB = airportsAndCities.get(locationB)

		(locationDetailsA, locationDetailsB) match {

			case (None, _) =>
				Failure(GeoBaseException("Unknown location \"" + locationA + "\""))

			case (_, None) =>
				Failure(GeoBaseException("Unknown location \"" + locationB + "\""))

			case (Some(locationDetailsA), Some(locationDetailsB)) => {

				val latA = locationDetailsA.getLatitude()
				val lngA = locationDetailsA.getLongitude()
				val latB = locationDetailsB.getLatitude()
				val lngB = locationDetailsB.getLongitude()

				(latA, lngA, latB, lngB) match {

					case (Success(latA), Success(lngA), Success(latB), Success(lngB)) =>
						Success(round(
							2 * 6371 * asin(sqrt(
								pow(sin(0.5 * (latA - latB)), 2) +
								pow(sin(0.5 * (lngA - lngB)), 2) * cos(latA) * cos(latB)
							))
						).toInt)

					case _ => {
						if (latA.isFailure || lngA.isFailure)
							Failure(GeoBaseException("No coordinates available for location \"" + locationA + "\""))
						else
							Failure(GeoBaseException("No coordinates available for location \"" + locationB + "\""))
					}
				}
			}
		}
	}

	/** Transforms a local date (at a given location) into a GMT date.
	  *
	  * Here we bring more than just converting the local time to GMT. The
	  * additional value is the knowledge of the time zone thanks to
	  * opentraveldata. You don't need to know the time zone, just enter the
	  * airport or the city as a parameter.
	  *
	  * {{{
	  * assert(geoBase.localDateToGMT("20160606_2227", "NYC") == Success("20160607_0227"))
	  * assert(geoBase.localDateToGMT("20160606_2227", "NYC").get == "20160607_0227")
	  * assert(geoBase.localDateToGMT("2016-06-06T22:27", "NYC", "yyyy-MM-dd'T'HH:mm").get == "2016-06-07T02:27")
	  * assert(geoBase.localDateToGMT("20160606_2227", "~#?") == Failure(GeoBaseException: Unknown location "~#?"))
	  * assert(geoBase.localDateToGMT("20160606_2227", "~#?").getOrElse("20000101_1200") == "20000101_1200")
	  * }}}
	  *
	  * @param localDate the local date at the given location under the given
	  * format.
	  * @param location the airport or city where this local date applies
	  * @param format (default = "yyyyMMdd_HHmm") the format under which
	  * localDate is provided and the GMT date is returned.
	  * @return the GMT date associated to the local date under the requested
	  * format.
	  */
	def localDateToGMT(
		localDate: String, location: String, format: String = "yyyyMMdd_HHmm"
	): Try[String] = {

		getTimeZone(location) match {

			case Success(timeZone) => {

				val inputLocalDateParser = new SimpleDateFormat(format)
				inputLocalDateParser.setTimeZone(TimeZone.getTimeZone(timeZone))

				val outputGMTDateFormatter = new SimpleDateFormat(format)
				outputGMTDateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"))

				Success(outputGMTDateFormatter.format(
					inputLocalDateParser.parse(localDate)
				))
			}

			case Failure(exception) => Failure(exception)
		}
	}

	/** Returns the offset in minutes for the given date at the given city/airport.
	  *
	  * {{{
	  * assert(geoBase.getOffsetForLocalDate("20170712", "NCE") == Success(120))
	  * assert(geoBase.getOffsetForLocalDate("20170712", "NCE").get == 120)
	  * assert(geoBase.getOffsetForLocalDate("2017-07-12", "NCE", "yyyy-MM-dd").get == 120)
	  * assert(geoBase.getOffsetForLocalDate("20171224", "NCE").get == 60)
	  * assert(geoBase.getOffsetForLocalDate("20171224", "NYC").get == -300)
	  * assert(geoBase.getOffsetForLocalDate("20171224", "~#?") == Failure(GeoBaseException: Unknown location "~#?"))
	  * assert(geoBase.getOffsetForLocalDate("20171224", "~#?").getOrElse("0") == 0)
	  * }}}
	  *
	  * @param localDate the local date
	  * @param location the airport or the city where this local date applies
	  * @param format (default = "yyyyMMdd") the format under which localDate is
	  * provided.
	  * @return the the offset in minutes for the given date at the given
	  * city/airport (can be negative).
	  */
	def getOffsetForLocalDate(
		localDate: String, location: String, format: String = "yyyyMMdd"
	): Try[Int] = {

		getTimeZone(location) match {

			case Success(timeZone) =>
				Success(
					TimeZone.getTimeZone(timeZone).getOffset(
						new SimpleDateFormat(format).parse(localDate).getTime()
					) / 60000
				)

			case Failure(exception) => Failure(exception)
		}
	}

	/** Transforms a GMT date into a local date for the given airport or city.
	  *
	  * Here we bring more than just converting the GMT time to local. The
	  * additional value is the knowledge of the time zone thanks to
	  * opentraveldata. You don't need to know the time zone, just enter the
	  * airport or the city as a parameter.
	  *
	  * {{{
	  * assert(geoBase.gmtDateToLocal("20160606_1427", "NCE") == Success("20160606_1627"))
	  * assert(geoBase.gmtDateToLocal("20160606_1427", "NCE").get == "20160606_1627")
	  * assert(geoBase.gmtDateToLocal("2016-06-07T02:27", "NYC", "yyyy-MM-dd'T'HH:mm").get == "2016-06-06T22:27")
	  * assert(geoBase.gmtDateToLocal("20160606_2227", "~#?") == Failure(GeoBaseException: Unknown location "~#?"))
	  * assert(geoBase.gmtDateToLocal("20160606_2227", "~#?").getOrElse("20000101_1200") == "20000101_1200")
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
		gmtDate: String, location: String, format: String = "yyyyMMdd_HHmm"
	): Try[String] = {

		getTimeZone(location) match {

			case Success(timeZone) => {

				val inputGMTDateParser = new SimpleDateFormat(format)
				inputGMTDateParser.setTimeZone(TimeZone.getTimeZone("GMT"))

				val outputLocalDateFormatter = new SimpleDateFormat(format)
				outputLocalDateFormatter.setTimeZone(TimeZone.getTimeZone(timeZone))

				Success(outputLocalDateFormatter.format(
					inputGMTDateParser.parse(gmtDate)
				))
			}

			case Failure(exception) => Failure(exception)
		}
	}

	/** Returns the trip duration between two locations (airport or city).
	  *
	  * In the travel indeuxtry, the trip duration is synonym with elapsed
	  * flying time (EFT).
	  *
	  * This is meant to be used to compute the trip duration for a segment/bound
	  * for which we know the origin/destination airports/cities and the local
	  * time. i.e. when we don't have gmt times.
	  *
	  * {{{
	  * assert(geoBase.getTripDurationFromLocalDates("20160606_1627", "CDG", "20160606_1757", "JFK") == Success(7.5d))
	  * assert(geoBase.getTripDurationFromLocalDates("20160606_1627", "CDG", "20160606_1757", "JFK").get == 7.5d)
	  *
	  * val computedTripDuration = geoBase.getTripDurationFromLocalDates(
	  * 	"2016-06-06T16:27", "CDG", "2016-06-06T17:57", "JFK",
	  * 	format = "yyyy-MM-dd'T'HH:mm", unit = "minutes"
	  * )
	  * assert(computedTripDuration.get == 450d)
	  *
	  * assert(geoBase.getTripDurationFromLocalDates("20160606_1627", "~#?", "20160606_1757", "JFK").get == Failure(GeoBaseException: Unknown location "~#?"))
	  * assert(geoBase.getTripDurationFromLocalDates("20160606_1627", "~#?", "20160606_1757", "JFK").getOrElse(-1d) == -1d)
	  * }}}
	  *
	  * @param localDepartureDate the departure local date
	  * @param originLocation the origin airport or city
	  * @param localArrivalDate the arrival local date
	  * @param destinationLocation the destination airport or city
	  * @param unit (default = "hours") either "hours" or "minutes"
	  * @param format (default = "yyyyMMdd_HHmm") the format under which local
	  * departure and arrival dates are provided.
	  * @return the trip duration in the chosen unit (in hours by default) and
	  * format.
	  */
	def getTripDurationFromLocalDates(
		localDepartureDate: String, originLocation: String,
		localArrivalDate: String, destinationLocation: String,
		unit: String = "hours", format: String = "yyyyMMdd_HHmm"
	): Try[Double] = {

		require(
			unit == "hours" || unit == "minutes",
			"option \"unit\" can only take value \"hours\" or \"minutes\" " +
			"but not \"" + unit + "\""
		)

		// We retrieve GMT dates in order to be able to do a real duration
		// computation:
		val gmtDepartureDate = localDateToGMT(
			localDepartureDate, originLocation, format
		)
		val gmtArrivalDate = localDateToGMT(
			localArrivalDate, destinationLocation, format
		)

		(gmtDepartureDate, gmtArrivalDate) match {

			case (Failure(exception), _) => Failure(exception)

			case (_, Failure(exception)) => Failure(exception)

			case (Success(gmtDepartureDate), Success(gmtArrivalDate)) => {

				val formatter = new SimpleDateFormat(format)

				val departureDate = formatter.parse(gmtDepartureDate)
				val arrivalDate = formatter.parse(gmtArrivalDate)

				val tripDurationinMilliseconds =
						arrivalDate.getTime() - departureDate.getTime()

				if (tripDurationinMilliseconds < 0)
					Failure(GeoBaseException(
						"The trip duration computed is negative (maybe you've " +
						"inverted departure/origin and arrival/destination)"
					))

				else {

					val tripDurationInMinutes = TimeUnit.MINUTES.convert(
						tripDurationinMilliseconds, TimeUnit.MILLISECONDS
					)

					if (unit == "minutes")
						Success(tripDurationInMinutes)
					else
						Success(tripDurationInMinutes / 60d)
				}
			}
		}
	}

	/** Returns the geo type of a trip (domestic, continental or inter continental).
	  *
	  * Return an enum value: GeoType.DOMESTIC, GeoType.CONTINENTAL or
	  * GeoType.INTER_CONTINENTAL.
	  *
	  * The distinction between continental and intercontinental is made based
	  * on iata zones.
	  *
	  * {{{
	  * assert(geoBase.getGeoType(List("CDG", "ORY")) == Success(GeoType.DOMESTIC))
	  * assert(geoBase.getGeoType(List("CDG", "ORY")).get == GeoType.DOMESTIC)
	  * assert(geoBase.getGeoType(List("FR", "FR")).get == GeoType.DOMESTIC)
	  * assert(geoBase.getGeoType(List("FR", "PAR", "DUB")).get == GeoType.CONTINENTAL)
	  * assert(geoBase.getGeoType(List("CDG", "TLS", "JFK", "MEX")).get == GeoType.INTER_CONTINENTAL)
	  * assert(geoBase.getGeoType(List("US", "bbb", "NCE", "aaa")).get == Failure(GeoBaseException: Unknown locations \"bbb\", \"aaa\"))
	  * assert(geoBase.getGeoType(List("US", "bbb", "NCE", "aaa")).toOption == None)
	  * }}}
	  *
	  * @param locations a list of cities/ariports/countries representing the
	  * trip.
	  * @return the type of the trip (a GeoType enum value, such as
	  * GeoType.DOMESTIC).
	  */
	def getGeoType(locations: List[String]): Try[GeoType] = {

		require(
			locations.length >= 2,
			"at least 2 locations are needed to compute a geography type"
		)

		// We transform all locations in countries:
		val distinctCountries = locations.map(
			item => getCountryFor(item)
		).distinct

		// If at least one mapping airport/city to country is failing:
		if (distinctCountries.exists(_.isFailure)) {

			val invalidLocations = locations.filter(
				item => getCountryFor(item).isFailure
			).map(
				item => "\"" + item + "\""
			)

			val plural = invalidLocations.size match { case 1 => "" case _ => "s" }
			Failure(GeoBaseException("Unknown location" + plural + " " + invalidLocations.mkString(", ")))
		}

		else {

			distinctCountries.length match {

				case 1 => Success(GeoType.DOMESTIC)

				case _ => {

					// If it's not domestic, we transform all countries in iata
					// zones:
					val distinctIataZones = distinctCountries.flatMap(_.toOption).map(
						country => getIataZoneFor(country)
					).distinct

					// If at least one mapping country to iata zone is failing:
					if (distinctIataZones.exists(_.isFailure)) {

						val invalidCountries =
							distinctCountries.flatMap(_.toOption).filter(
								country => getIataZoneFor(country).isFailure
							).map(
								country => "\"" + country + "\""
							)

						val plural = invalidCountries.size match { case 1 => "y" case _ => "ies" }
						Failure(GeoBaseException("Unknown countr" + plural + " " + invalidCountries.mkString(", ")))
					}

					else
						distinctIataZones.length match {
							case 1 => Success(GeoType.CONTINENTAL)
							case _ => Success(GeoType.INTER_CONTINENTAL)
						}
				}
			}
		}
	}

	/** Returns the list of nearby airports (within the radius) for the given airport or city.
	  *
	  * Find the list of nearby airports, within the requested radius. The list
	  * is sorted starting from the closest airport.
	  *
	  * {{{
	  * assert(geoBase.getNearbyAirportsWithDetails("CDG", 50) == Success(List("LBG", "ORY", "VIY", "POX")))
	  * assert(geoBase.getNearbyAirportsWithDetails("CDG", 36).get == List("LBG", "ORY"))
	  * assert(geoBase.getNearbyAirportsWithDetails("~#?", 36)).get == Failure(GeoBaseException: Unknown location \"~#?\""))
	  * assert(geoBase.getNearbyAirportsWithDetails("~#?", 36)).getOrElse(Nil) == List())
	  * }}}
	  *
	  * @param location the airport or city for which to find nearby airports.
	  * @param radius the maximum distance (in km) for which an airport is
	  * considered close.
	  * @return the sorted per incresaing distance list nearby airports
	  */
	def getNearbyAirports(location: String, radius: Int): Try[List[String]] = {

		getNearbyAirportsWithDetails(location, radius) match {
			case Success(nearbyAirports) => Success(nearbyAirports.map(_._1))
			case Failure(exception) => Failure(exception)
		}
	}

	/** Returns the list of nearby airports (within the radius) for the given airport or city.
	  *
	  * Find the list of nearby airports, within the requested radius. The list
	  * is sorted starting from the closest airport. This list is a tuple of
	  * (airport/distance).
	  *
	  * {{{
	  * assert(geoBase.getNearbyAirportsWithDetails("CDG", 50) == Success(List(("LBG", 9), ("ORY", 35), ("VIY", 37), ("POX", 38))))
	  * assert(geoBase.getNearbyAirportsWithDetails("CDG", 36).get == List(("LBG", 9), ("ORY", 35)))
	  * assert(geoBase.getNearbyAirportsWithDetails("~#?", 36)).get == Failure(GeoBaseException: Unknown location \"~#?\""))
	  * assert(geoBase.getNearbyAirportsWithDetails("~#?", 36)).getOrElse(Nil) == List())
	  * }}}
	  *
	  * @param location the airport or city for which to find nearby
	  * airports.
	  * @param radius the maximum distance (in km) for which an airport is
	  * considered close.
	  * @return the sorted per incresaing distance list of tuples (airport,
	  * distance).
	  */
	def getNearbyAirportsWithDetails(location: String, radius: Int): Try[List[(String, Int)]] = {

		require(radius > 0, "radius must be strictly positive")

		// We check whether the given location is known:
		if (airportsAndCities.contains(location)) {

			val nearbyAirports = airportsAndCities.keys.toList.filter(
				// We only keep airport locations:
				randomLocation => airportsAndCities(randomLocation).isAirport()
			).flatMap(
				// We compute the distance between all airports to the given airport
				// and we only keep those for which the distance is within the given
				// radius:
				randomAirport => {

					val distance = getDistanceBetween(location, randomAirport).getOrElse(-1)

					if (distance > 0 && distance <= radius)
						Some((randomAirport, distance))
					else
						None
				}
			).sortWith(
				// And we sort per increasing radius:
				_._2 < _._2
			)

			Success(nearbyAirports)
		}

		else
			Failure(GeoBaseException("Unknown location \"" + location + "\""))
	}

	private def getTimeZone(location: String): Try[String] = {
		airportsAndCities.get(location) match {
			case Some(locationInfo) => locationInfo.getTimeZone()
			case None => Failure(GeoBaseException("Unknown location \"" + location + "\""))
		}
	}
}

/** An enumeration which represents the possible geograpic types of a trip */
object GeoType extends Enumeration {
	type GeoType = Value
	val DOMESTIC, CONTINENTAL, INTER_CONTINENTAL = Value
}
