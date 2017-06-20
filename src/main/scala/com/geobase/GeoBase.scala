package com.geobase

import com.geobase.load.Loader
import com.geobase.error.GeoBaseException
import com.geobase.model.{Airline, AirportOrCity, Country}

import java.text.SimpleDateFormat

import java.util.concurrent.TimeUnit
import java.util.TimeZone

import java.security.InvalidParameterException

import math.{asin, cos, pow, round, sin, sqrt}

/** A facility to '''deal with travel/geographical data'''.
  *
  * Provides '''geographical mappings''' at airport/city/country level mainly
  * based on <a href="https://github.com/opentraveldata/opentraveldata">
  * opentraveldata</a> as well as other mappings such as airlines or currencies.
  * This facility also provides classic time-oriented methods such as trip
  * duration computation.
  *
  * Here are a few exemples:
  *
  * {{{
  * val geoBase = new GeoBase()
  * assert(geoBase.getCityForAirport("CDG") == "PAR")
  * assert(geoBase.getTripDurationFromLocalDates("20160606_1627", "CDG", "20160606_1757", "JFK") == 7.5f)
  * assert(geoBase.getCurrencyForCountry("FR") == "EUR")
  * }}}
  *
  * The GeoBase object can be used within Spark jobs (in this case, don't forget
  * to '''broadcast GeoBase''').
  *
  * Opentraveldata is an accurate and maintained source of various travel
  * mappings. This scala wrapper around opentraveldata mostly uses this very
  * file: <a href="https://github.com/opentraveldata/opentraveldata/tree/master/opentraveldata/optd_por_public.csv">
  * optd_por_public.csv</a>.
  *
  * Source <a href="https://github.com/opentraveldata/opentraveldata/tree/master/src/main/scala/com/geobase/GeoBase.scala">
  * GeoBase</a>
  *
  * @todo Switch to JodaTime
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
	  * assert(geoBase.getCityForAirportOrElse("CDG", "") == "PAR")
	  * assert(geoBase.getCityForAirportOrElse("?*#", "") == "")
	  * }}}
	  *
	  * @param airport the airport IATA code (for instance CDG) for which to get
	  * the associated city.
	  * @param orElse the value to return if no city could be associated to the
	  * requested airport.
	  * @return the city code corresponding to the given airport (for instance
	  * PAR).
	  */
	def getCityForAirportOrElse(airport: String, orElse: String = ""): String = {

		if (!airportsAndCities.contains(airport))
			orElse

		else {
			val cityCode = airportsAndCities(airport).cityCode
			if (cityCode.length >= 3) cityCode.substring(0, 3) else orElse
		}
	}

	/** Returns the city associated to the given airport.
	  *
	  * {{{ assert(geoBase.getCityForAirport("CDG") == "PAR") }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find a corresponding city.
	  *
	  * @param airport the airport IATA code (for instance CDG) for which to get
	  * the associated city.
	  * @return the city code corresponding to the given airport (for instance
	  * PAR).
	  * @throws classOf[GeoBaseException]
	  */
	def getCityForAirport(airport: String): String = {

		val cityCode = getCityForAirportOrElse(airport, "")

		if (cityCode != "")
			cityCode
		else
			throw GeoBaseException("No entry in GeoBase for \"" + airport + "\"")
	}

	/** Returns the cities associated to the given airport.
	  *
	  * It sometimes happens that an airport is shared between cities. This
	  * method, returns this list of cities (usually the list wil only contains
	  * one city).
	  *
	  * The method getCityForAirport returns the first city corresponding to the
	  * given airport, which is by assumption the biggest corresponding city.
	  *
	  * {{{
	  * assert(geoBase.getCitiesForAirportOrElse("CDG", List()) == List("PAR"))
	  * assert(geoBase.getCitiesForAirportOrElse("AZA", List()) == List("PHX", "MSC"))
	  * assert(geoBase.getCitiesForAirportOrElse("?*#", List()) == List())
	  * }}}
	  *
	  * @param airport the airport IATA code (for instance AZA) for which to get
	  * the associated cities.
	  * @param orElse the value to return if no city could be associated to the
	  * requested airport (for instance List()).
	  * @return the list of city codes corresponding to the given airport (for
	  * instance List("PHX", "MSC")).
	  */
	def getCitiesForAirportOrElse(
		airport: String, orElse: List[String] = List()
	): List[String] = {

		if (!airportsAndCities.contains(airport))
			orElse

		else {
			val cityCodes = airportsAndCities(airport).cityCode.split("\\,")
			if (!cityCodes.isEmpty) cityCodes.toList else orElse
		}
	}

	/** Returns the cities associated to the given airport.
	  *
	  * It sometimes happens that an airport is shared between cities. This
	  * method, returns this list of cities (usually the list wil only contains
	  * one city).
	  *
	  * The method getCityForAirport returns the first city corresponding to the
	  * given airport, which is by assumption the biggest corresponding city.
	  *
	  * {{{
	  * assert(geoBase.getCitiesForAirport("CDG") == List("PAR"))
	  * assert(geoBase.getCitiesForAirport("AZA") == List("PHX", "MSC"))
	  * }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find corresponding cities.
	  *
	  * @param airport the airport IATA code (for instance AZA) for which to get
	  * the associated cities.
	  * @return the list of city codes corresponding to the given airport (for
	  * instance List("PHX", "MSC")).
	  * @throws classOf[GeoBaseException]
	  */
	def getCitiesForAirport(airport: String): List[String] = {

		val cityCodes = getCitiesForAirportOrElse(airport, List())

		if (!cityCodes.isEmpty)
			cityCodes
		else
			throw GeoBaseException("No entry in GeoBase for \"" + airport + "\"")
	}

	/** Returns the country associated to the given city.
	  *
	  * {{{
	  * assert(geoBase.getCountryForCityOrElse("PAR") == "FR")
	  * assert(geoBase.getCountryForCityOrElse("?*#", "") == "")
	  * }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find a corresponding country.
	  *
	  * @param city the city IATA code (for instance PAR) for which to get
	  * the associated country.
	  * @param orElse the value to return if no country could be associated to
	  * the requested city.
	  * @return the country code corresponding to the given city (for instance
	  * FR).
	  */
	def getCountryForCityOrElse(city: String, orElse: String = ""): String = {
		if (!airportsAndCities.contains(city))
			orElse
		else
			airportsAndCities(city).countryCode
	}

	/** Returns the country associated to the given city.
	  *
	  * {{{ assert(geoBase.getCountryForCity("PAR") == "FR") }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find a corresponding country.
	  *
	  * @param city the city IATA code (for instance PAR) for which to get
	  * the associated country.
	  * @return the country code corresponding to the given city (for instance
	  * FR).
	  * @throws classOf[GeoBaseException]
	  */
	def getCountryForCity(city: String): String = {

		val countryCode = getCountryForCityOrElse(city, "")

		if (countryCode != "")
			countryCode
		else
			throw GeoBaseException("No entry in GeoBase for \"" + city + "\"")
	}

	/** Returns the continent associated to the given airport, city or country.
	  *
	  * Possible values: EU (Eurrope) - NA (North America) - SA (South Africa) -
	  * AF (Africa) - AS (Asia) - AN (Antarctica) - OC (Oceania).
	  *
	  * {{{
	  * assert(geoBase.getContinentForLocationOrElse("CDG") == "EU")
	  * assert(geoBase.getContinentForLocationOrElse("NYC") == "NA")
	  * assert(geoBase.getContinentForLocationOrElse("CN") == "AS")
	  * assert(geoBase.getContinentForLocationOrElse("?*#", "") == "")
	  * }}}
	  *
	  * @param location the country, city or airport IATA code (for instance
	  * PAR) for which to get the associated continent.
	  * @param orElse the value to return if no continent could be associated to
	  * the requested location.
	  * @return the continent code corresponding to the given location (for
	  * instance EU).
	  */
	def getContinentForLocationOrElse(location: String, orElse: String = ""): String = {

		// Let's first try as if location is a country:
		if (countries.contains(location)) {
			val continentCode = countries(location).continentCode
			if (continentCode != "") continentCode else orElse
		}

		// Otherwise, let's then try as if location was a city or an airport:
		else {

			val countryCode = getCountryForCityOrElse(location, "")

			if (countries.contains(countryCode)) {
				val continentCode = countries(countryCode).continentCode
				if (continentCode != "") continentCode else orElse
			}

			else
				orElse
		}
	}

	/** Returns the continent associated to the given airport, city or country.
	  *
	  * Possible values: EU (Eurrope) - NA (North America) - SA (South Africa) -
	  * AF (Africa) - AS (Asia) - AN (Antarctica) - OC (Oceania).
	  *
	  * {{{
	  * assert(geoBase.getContinentForLocation("CDG") == "EU")
	  * assert(geoBase.getContinentForLocation("NYC") == "NA")
	  * assert(geoBase.getContinentForLocation("CN") == "AS")
	  * }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find a corresponding
	  * continent.
	  *
	  * @param location the country, city or airport IATA code (for instance
	  * PAR) for which to get the associated continent.
	  * @return the continent code corresponding to the given location (for
	  * instance EU).
	  * @throws classOf[GeoBaseException]
	  */
	def getContinentForLocation(location: String): String = {

		val continentCode = getContinentForLocationOrElse(location, "")

		if (continentCode != "")
			continentCode
		else
			throw GeoBaseException("No entry in GeoBase for \"" + location + "\"")
	}

	/** Returns the IATA zone associated to the given airport, city or country.
	  *
	  * Possible values are 11, 12, 13, 21, 22, 23, 31, 32 or 33.
	  *
	  * {{{
	  * assert(geoBase.getIataZoneForLocationOrElse("CDG", "") == "21")
	  * assert(geoBase.getIataZoneForLocationOrElse("NYC", "") == "11")
	  * assert(geoBase.getIataZoneForLocationOrElse("ZA", "") == "23")
	  * assert(geoBase.getIataZoneForLocationOrElse("?*#", "") == "")
	  * }}}
	  *
	  * @param location the country, city or airport IATA code (for instance
	  * PAR) for which to get the associated IATA zone.
	  * @param orElse the value to return if no continent could be associated to
	  * the requested location.
	  * @return the IATA zone code corresponding to the given location (for
	  * instance 21).
	  */
	def getIataZoneForLocationOrElse(location: String, orElse: String = ""): String = {

		// Let's first try as if location was a country:
		if (countries.contains(location)) {
			val iataZone = countries(location).iataZone
			if (iataZone != "") iataZone else orElse
		}

		// Otherwise, let's then try as if location is a city or an airport:
		else {

			val countryCode = getCountryForCityOrElse(location, "")

			if (countries.contains(countryCode)) {
				val iataZone = countries(countryCode).iataZone
				if (iataZone != "") iataZone else orElse
			}

			else
				orElse
		}
	}

	/** Returns the IATA zone associated to the given airport, city or country.
	  *
	  * Possible values are 11, 12, 13, 21, 22, 23, 31, 32 or 33.
	  *
	  * {{{
	  * assert(geoBase.getIataZoneForLocation("CDG") == "21")
	  * assert(geoBase.getIataZoneForLocation("NYC") == "11")
	  * assert(geoBase.getIataZoneForLocation("ZA") == "23")
	  * }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find a corresponding IATA
	  * zone.
	  *
	  * @param location the country, city or airport IATA code (for instance
	  * PAR) for which to get the associated IATA zone.
	  * @return the IATA zone code corresponding to the given location (for
	  * instance 21).
	  * @throws classOf[GeoBaseException]
	  */
	def getIataZoneForLocation(location: String): String = {

		val iataZone = getIataZoneForLocationOrElse(location, "")

		if (iataZone != "")
			iataZone
		else
			throw GeoBaseException("No entry in GeoBase for \"" + location + "\"")
	}

	/** Returns the currency associated to the given country.
	  *
	  * {{{
	  * assert(geoBase.getCurrencyForCountryOrElse("FR", "") == "EUR")
	  * assert(geoBase.getCurrencyForCountryOrElse("?#", "") == "")
	  * assert(geoBase.getCurrencyForCountryOrElse("?#", "USD") == "USD")
	  * }}}
	  *
	  * @param country the country IATA code (for instance FR) for which to get
	  * the associated currency.
	  * @param orElse the value to return if no currency could be associated to
	  * the requested country.
	  * @return the currency code corresponding to the given country (for
	  * instance EUR).
	  */
	def getCurrencyForCountryOrElse(country: String, orElse: String = ""): String = {

		// Let's rename the parameter:
		val location = country

		// Let's first try as if location was a country:
		if (countries.contains(location)) {
			val currencyCode = countries(location).currencyCode
			if (currencyCode != "") currencyCode else orElse
		}

		// Otherwise, let's then try as if location is a city or an airport:
		else {

			val countryCode = getCountryForCityOrElse(location, "")

			if (countries.contains(countryCode)) {
				val currencyCode = countries(countryCode).currencyCode
				if (currencyCode != "") currencyCode else orElse
			}

			else
				orElse
		}
	}

	/** Returns the currency associated to the given country.
	  *
	  * {{{ assert(geoBase.getCurrencyForCountry("FR") == "EUR") }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find a corresponding currency.
	  *
	  * @param country the country IATA code (for instance FR) for which to get
	  * the associated currency.
	  * @return the currency code corresponding to the given country (for
	  * instance EUR).
	  * @throws classOf[GeoBaseException]
	  */
	def getCurrencyForCountry(country: String): String = {

		val currencyCode = getCurrencyForCountryOrElse(country, "")

		if (currencyCode != "")
			currencyCode
		else
			throw GeoBaseException("No entry in GeoBase for \"" + country + "\"")
	}

	/** Returns the country associated to the given airline.
	  *
	  * {{{
	  * assert(geoBase.getCountryForAirlineOrElse("AF", "") == "FR")
	  * assert(geoBase.getCountryForAirlineOrElse("#?", "") == "")
	  * }}}
	  *
	  * @param airline the airline IATA code (for instance AF) for which to get
	  * the associated country.
	  * @param orElse the value to return if no country could be associated to
	  * the requested airline.
	  * @return the country code corresponding to the given airline (for
	  * instance FR).
	  */
	def getCountryForAirlineOrElse(airline: String, orElse: String = ""): String = {

		if (!airlines.contains(airline))
			orElse

		else {
			val countryCode = airlines(airline).countryCode
			if (countryCode != "") countryCode else orElse
		}
	}

	/** Returns the country associated to the given airline.
	  *
	  * {{{ assert(geoBase.getCountryForAirline("AF") == "FR") }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find a corresponding country.
	  *
	  * @param airline the airline IATA code (for instance AF) for which to get
	  * the associated country.
	  * @return the country code corresponding to the given airline (for
	  * instance FR).
	  * @throws classOf[GeoBaseException]
	  */
	def getCountryForAirline(airline: String): String = {

		val countryCode = getCountryForAirlineOrElse(airline, "")

		if (countryCode != "")
			countryCode
		else
			throw GeoBaseException("No entry in GeoBase for \"" + airline + "\"")
	}

	/** Returns the distance between two locations (airports/cities).
	  *
	  * {{{
	  * assert(geoBase.getDistanceBetweenOrElse("ORY", "NCE", -1) == 674)
	  * assert(geoBase.getDistanceBetweenOrElse("PAR", "NCE", -1) == 686)
	  * assert(geoBase.getDistanceBetweenOrElse("PAR", "~#?", -1) == -1)
	  * }}}
	  *
	  * @param airportOrCityA an airport or city IATA code (for instance ORY)
	  * for which to get the distance with airportOrCityB.
	  * @param airportOrCityB an airport or city IATA code (for instance NCE)
	  * for which to get the distance with airportOrCityA.
	  * @param orElse the value to return if GeoBase doesn't have data for
	  * airportOrCityA or airportOrCityB.
	  * @return the distance rounded in km between airportOrCityA and
	  * airportOrCityB (for instance 674).
	  */
	def getDistanceBetweenOrElse(
		airportOrCityA: String, airportOrCityB: String, orElse: Int = -1
	): Int = {

		if (
			!airportsAndCities.contains(airportOrCityA) ||
			!airportsAndCities.contains(airportOrCityB)
		)
			orElse

		else {

			val latA = airportsAndCities(airportOrCityA).getLatitude()
			val lngA = airportsAndCities(airportOrCityA).getLongitude()
			val latB = airportsAndCities(airportOrCityB).getLatitude()
			val lngB = airportsAndCities(airportOrCityB).getLongitude()

			if (latA == None || lngA == None || latB == None || lngB == None)
				orElse

			// The Haversine formula (6371 is Earth radius):
			else
				round(
					2 * 6371 * asin(sqrt(
						pow(sin(0.5 * (latA.get - latB.get)), 2) +
						pow(sin(0.5 * (lngA.get - lngB.get)), 2) *
						cos(latA.get) * cos(latB.get)
					))
				).toInt
		}
	}

	/** Returns the distance between two locations (airports/cities).
	  *
	  * {{{
	  * assert(geoBase.getDistanceBetween("ORY", "NCE") == 674)
	  * assert(geoBase.getDistanceBetween("PAR", "NCE") == 686)
	  * }}}
	  *
	  * Throws a GeoBaseException if GeoBase doesn't have an entry for the
	  * requested airports/cities.
	  *
	  * @param airportOrCityA an airport or city IATA code (for instance ORY)
	  * for which to get the distance with airportOrCityB.
	  * @param airportOrCityB an airport or city IATA code (for instance NCE)
	  * for which to get the distance with airportOrCityA.
	  * @return the distance rounded in km between airportOrCityA and
	  * airportOrCityB (for instance 674).
	  * @throws classOf[GeoBaseException]
	  */
	def getDistanceBetween(airportOrCityA: String, airportOrCityB: String): Int = {

		val distance = getDistanceBetweenOrElse(airportOrCityA, airportOrCityB, -1)

		if (distance != -1)
			distance
		else
			throw GeoBaseException(
				"One of airports/cities " + airportOrCityA + " or " +
				airportOrCityB + " is either not entry in GeoBase or doesn't " +
				"have a valid latitude or longitude."
			)
	}

	/** Transforms a local date in a GMT date.
	  *
	  * Here we bring more than just converting the local time to GMT. The
	  * additional value is on the knowledge of the time zone thanks to
	  * opentraveldata. You don't need to know the time zone, just enter the
	  * airport or the city as a parameter.
	  *
	  * {{{
	  * assert(geoBase.localDateToGMT("20160606_2227", "NYC") == "20160607_0227")
	  * assert(geoBase.localDateToGMT("2016-06-06T22:27", "NYC", "yyyy-MM-dd'T'HH:mm") == "2016-06-07T02:27")
	  * }}}
	  *
	  * Throws a GeoBaseException if GeoBase doesn't have an entry for the
	  * requested airports/cities.
	  *
	  * @param localDate the local date
	  * @param localAirportOrCity the airport or the city where this local date
	  * applies.
	  * @param format (default = "yyyyMMdd_HHmm") the format under which
	  * localDate is provided and the GMT date is returned.
	  * @return the GMT date associated to the local date under the requested
	  * format.
	  * @throws classOf[GeoBaseException]
	  */
	def localDateToGMT(
		localDate: String, localAirportOrCity: String,
		format: String = "yyyyMMdd_HHmm"
	): String = {

		checkExistence(localAirportOrCity, airportsAndCities)

		// We retrieve the time zone associated to this geo. point:
		val timeZone = airportsAndCities(localAirportOrCity).timeZone

		val inputLocalDateParser = new SimpleDateFormat(format)
		inputLocalDateParser.setTimeZone(TimeZone.getTimeZone(timeZone))

		// The formatter to GMT (we check if we already have an instance of one
		// since it's faster not to create objects over and over):
		val outputGMTDateFormatter = new SimpleDateFormat(format)
		outputGMTDateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"))

		outputGMTDateFormatter.format(
			inputLocalDateParser.parse(localDate)
		)
	}

	/** Returns the offset in minutes for the given date at the given city/airport.
	  *
	  * {{{
	  * assert(geoBase.getOffsetForLocalDate("20170712", "NCE") == 120)
	  * assert(geoBase.getOffsetForLocalDate("20170712", "NCE", "yyyyMMdd") == 120)
	  * assert(geoBase.getOffsetForLocalDate("20171224", "NCE") == 60)
	  * assert(geoBase.getOffsetForLocalDate("20171224", "NYC") == -300)
	  * }}}
	  *
	  * Throws a GeoBaseException if GeoBase doesn't have an entry for the
	  * requested airports/cities.
	  *
	  * @param localDate the local date
	  * @param localAirportOrCity the airport or the city where this local date
	  * applies.
	  * @param format (default = "yyyyMMdd") the format under which
	  * localDate is provided.
	  * @return the the offset in minutes for the given date at the given
	  * city/airport (can be negative).
	  * @throws classOf[GeoBaseException]
	  */
	def getOffsetForLocalDate(
		localDate: String, localAirportOrCity: String, format: String = "yyyyMMdd"
	): Int = {

		checkExistence(localAirportOrCity, airportsAndCities)

		val parser = new SimpleDateFormat(format)

		// We retrieve the time zone associated to this geo. point:
		val timeZone = airportsAndCities(localAirportOrCity).timeZone

		TimeZone.getTimeZone(timeZone).getOffset(
			parser.parse(localDate).getTime()
		) / 60000
	}

	/** Transforms a GMT date in a local date for the given airport or city.
	  *
	  * Here we bring more than just converting the GMT time to local. The
	  * additional value is on the knowledge of the time zone thanks to
	  * opentraveldata. You don't need to know the time zone, just enter the
	  * airport or the city as a parameter.
	  *
	  * {{{
	  * assert(geoBase.localDateToGMT("20160606_2227", "NYC") == "20160607_0227")
	  * assert(geoBase.localDateToGMT("2016-06-06T22:27", "NYC", "yyyy-MM-dd'T'HH:mm") == "2016-06-07T02:27")
	  * }}}
	  *
	  * Throws a GeoBaseException if GeoBase doesn't have an entry for the
	  * requested airports/cities.
	  *
	  * @param gmtDate the GMT date
	  * @param localAirportOrCity the airport or the city where this GMT date
	  * is to be localized.
	  * @param format (default = "yyyyMMdd_HHmm") the format under which gmtDate
	  * is provided and the local date is returned.
	  * @return the local date associated to the GMT date under the requested
	  * format.
	  * @throws classOf[GeoBaseException]
	  */
	def gmtDateToLocal(
		gmtDate: String, localAirportOrCity: String,
		format: String = "yyyyMMdd_HHmm"
	): String = {

		checkExistence(localAirportOrCity, airportsAndCities)

		// We retrieve the time zone associated to this geo. point:
		val timeZone = airportsAndCities(localAirportOrCity).timeZone

		// The formatter to GMT (we check if we already have an instance of one
		// since it's faster not to create objects over and over):
		val inputGMTDateParser = new SimpleDateFormat(format)
		inputGMTDateParser.setTimeZone(TimeZone.getTimeZone("GMT"))

		val outputLocalDateFormatter = new SimpleDateFormat(format)
		outputLocalDateFormatter.setTimeZone(TimeZone.getTimeZone(timeZone))

		outputLocalDateFormatter.format(
			inputGMTDateParser.parse(gmtDate)
		)
	}

	/** Returns the trip duration between two locations (airport or city).
	  *
	  * Trip duration is a synonym of elapsed flying time (EFT).
	  *
	  * This is meant to be used to compute the trip duration for a
	  * segment/bound for which we know the origin/destination airports/cities
	  * and the local time. i.e. when we don't have gmt times.
	  *
	  * {{{
	  * assert(geoBase.getTripDurationFromLocalDates("20160606_1627", "CDG", "20160606_1757", "JFK") == 7.5f)
	  *
	  * val computedTripDuration = geoBase.getTripDurationFromLocalDates(
	  * 	"2016-06-06T16:27", "CDG", "2016-06-06T17:57", "JFK",
	  * 	format = "yyyy-MM-dd'T'HH:mm", unit = "minutes"
	  * )
	  * assert(computedTripDuration == 450f)
	  * }}}
	  *
	  * Throws a GeoBaseException if GeoBase doesn't have an entry for the
	  * requested airports/cities.
	  *
	  * @param localDepartureDate the departure local date
	  * @param originAirportOrCity the origin airport or city
	  * @param localArrivalDate the arrival local date
	  * @param destinationAirportOrCity the destination airport or city
	  * @param unit (default = "hours") either "hours" or "minutes"
	  * @param format (default = "yyyyMMdd_HHmm") the format under which local
	  * departure and arrival dates are provided.
	  * @return the trip duration in the chosen unit (in hours by default) and
	  * format.
	  * @throws classOf[GeoBaseException]
	  */
	def getTripDurationFromLocalDates(
		localDepartureDate: String, originAirportOrCity: String,
		localArrivalDate: String, destinationAirportOrCity: String,
		unit: String = "hours", format: String = "yyyyMMdd_HHmm"
	): Float = {

		if (!List("hours", "minutes").contains(unit))
			throw new InvalidParameterException(
				"Option \"unit\" can only take value \"hours\" or " +
				"\"minutes\" but not \"" + unit + "\""
			)

		// We retrieve GMT dates in order to be able to do a real duration
		// computation:
		val gmtDepartureDate = localDateToGMT(
			localDepartureDate, originAirportOrCity, format
		)
		val gmtArrivalDate = localDateToGMT(
			localArrivalDate, destinationAirportOrCity, format
		)

		val formatter = new SimpleDateFormat(format)

		val departureDate = formatter.parse(gmtDepartureDate)
		val arrivalDate = formatter.parse(gmtArrivalDate)

		val tripDurationinMilliseconds = arrivalDate.getTime() - departureDate.getTime()

		// We throw an exception if the trip duration is negative:
		if (tripDurationinMilliseconds < 0)
			throw GeoBaseException(
				"The trip duration computed is negative (maybe you've " +
				"inverted departure/origin and arrival/destination)"
			)

		val tripDurationinMinutes = TimeUnit.MINUTES.convert(
			tripDurationinMilliseconds, TimeUnit.MILLISECONDS
		)

		if (unit == "minutes")
			tripDurationinMinutes
		else
			tripDurationinMinutes / 60f
	}

	/** Returns the day of week for a date under the given format.
	  *
	  * A Monday is "1" and a Sunday is "7".
	  *
	  * {{{ assert(geoBase.getDayOfWeek("20160614") == "2") }}}
	  *
	  * @param date the date for which to get the day of week
	  * @param format (default = "yyyyMMdd") the format under which the date is
	  * provided.
	  * @return the associated day of week, such as 2 for Tuesday
	  */
	def getDayOfWeek(date: String, format: String = "yyyyMMdd"): String = {
		val parser = new SimpleDateFormat(format)
		val formatter = new SimpleDateFormat("u")
		formatter.format(parser.parse(date))
	}

	/** Returns the geo type of a trip (domestic, continental or inter continental).
	  *
	  * Return an enum value: GeoType.DOMESTIC, GeoType.CONTINENTAL or
	  * GeoType.INTER_CONTINENTAL.
	  *
	  * {{{
	  * assert(geoBase.getGeoType(List("CDG", "ORY")) == GeoType.DOMESTIC)
	  * assert(geoBase.getGeoType(List("FR", "FR")) == GeoType.DOMESTIC)
	  * assert(geoBase.getGeoType(List("FR", "PAR", "DUB")) == GeoType.CONTINENTAL)
	  * assert(geoBase.getGeoType(List("CDG", "TLS", "JFK", "MEX")) == GeoType.INTER_CONTINENTAL)
	  * }}}
	  *
	  * Throws a GeoBaseException if GeoBase doesn't have an entry for one of
	  * the requested airports/cities.
	  *
	  * @param locations a list of cities/ariports/countries representing the
	  * trip.
	  * @return the type of the trip (a GeoType enum value, such as
	  * GeoType.DOMESTIC).
	  * @throws classOf[GeoBaseException]
	  */
	def getGeoType(locations: List[String]): GeoType = {

		// What would it mean to return a geo type if we have 0 or 1 locations?:
		if (locations.isEmpty)
			throw new InvalidParameterException(
				"GeoBase can't provide a Geography Type for an empty list of " +
				"airports/cities"
			)
		if (locations.length == 1)
			throw new InvalidParameterException(
				"GeoBase can't provide a Geography Type if only one " +
				"airport or city is provided"
			)

		// We transform all locations in countries:
		val distinctCountries = locations.map(
			item => {
				if (item.length == 2)
					item
				else
					getCountryForCity(item)
			}
		).distinct

		// And all countries in iata zones:
		val distinctIataZones = distinctCountries.map(
			country => {
				checkExistence(country, countries)
				countries(country).iataZone
			}
		).distinct

		// If the list of distinct countries is reduced to one element, then
		// it's a domestic trip:
		if (distinctCountries.length == 1)
			GeoType.DOMESTIC

		// If we only have one iata zone, then it's a continental trip:
		else if (distinctIataZones.length == 1)
			GeoType.CONTINENTAL

		// Otherwise, it's an intercontinental trip:
		else
			GeoType.INTER_CONTINENTAL
	}

	/** Returns the list of nearby airports (within the radius) for the given airport or city.
	  *
	  * Find the list of nearby airports, within the requested radius. The list
	  * is sorted starting from the closest airport.
	  *
	  * {{{
	  * assert(geoBase.getNearbyAirportsWithDetails("CDG", 50) == List("LBG", "ORY", "VIY", "POX"))
	  * assert(geoBase.getNearbyAirportsWithDetails("CDG", 36) == List("LBG", "ORY"))
	  * }}}
	  *
	  * Throws a GeoBaseException if GeoBase doesn't have an entry for the
	  * requested location.
	  *
	  * @todo Include an internal cache in order to make it faster to do twice
	  * the same query?
	  *
	  * @param airportOrCity the airport or city for which to find nearby
	  * airports.
	  * @param radius the maximum distance (in km) for which an airport is
	  * considered close.
	  * @return the sorted per incresaing distance list of tuples (airport,
	  * distance).
	  * @throws classOf[GeoBaseException]
	  */
	def getNearbyAirports(airportOrCity: String, radius: Int): List[String] = {
		getNearbyAirportsWithDetails(airportOrCity, radius).map(_._1)
	}

	/** Returns the list of nearby airports (within the radius) for the given airport or city.
	  *
	  * Find the list of nearby airports, within the requested radius. The list
	  * is sorted starting from the closest airport. This list is a tuple of
	  * (airport/distance).
	  *
	  * {{{
	  * assert(geoBase.getNearbyAirportsWithDetails("CDG", 50) == List(("LBG", 9), ("ORY", 35), ("VIY", 37), ("POX", 38)))
	  * assert(geoBase.getNearbyAirportsWithDetails("CDG", 36) == List(("LBG", 9), ("ORY", 35)))
	  * }}}
	  *
	  * Throws a GeoBaseException if GeoBase doesn't have an entry for the
	  * requested location.
	  *
	  * @todo Include an internal cache in order to make it faster to do twice
	  * the same query?
	  *
	  * @param airportOrCity the airport or city for which to find nearby
	  * airports.
	  * @param radius the maximum distance (in km) for which an airport is
	  * considered close.
	  * @return the sorted per incresaing distance list of tuples (airport,
	  * distance).
	  * @throws classOf[GeoBaseException]
	  */
	def getNearbyAirportsWithDetails(
		airportOrCity: String, radius: Int
	): List[(String, Int)] = {

		// No negative radius allowed:
		if (radius <= 0)
			throw new InvalidParameterException("No negative radius allowed")

		airportsAndCities.keys.toList.filter(
			// We only keep airport locations:
			randomLocation =>
				airportsAndCities(randomLocation).isAirport()
		).flatMap(
			// We compute the distance between all airports to the given airport
			// and we only keep those for which the distance is within the given
			// radius:
			randomAirport => {

				val distance = getDistanceBetweenOrElse(airportOrCity, randomAirport, -1)

				if (distance > 0 && distance <= radius)
					Some((randomAirport, distance))
				else
					None
			}
		).sortWith(
			// And we sort per increasing radius:
			_._2 < _._2
		)
	}

	// Aliases:

	/** Returns the country associated to the given airport.
	  *
	  * {{{ assert(geoBase.getCountryForAirport("CDG") == "FR") }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find a corresponding country.
	  *
	  * @param airport the airport IATA code (for instance CDG) for which to get
	  * the associated country.
	  * @return the country code corresponding to the given airport (for
	  * instance FR).
	  * @throws classOf[GeoBaseException]
	  */
	def getCountryForAirport(airport: String): String = getCountryForCity(airport)

	/** Returns the country associated to the given airport.
	  *
	  * {{{
	  * assert(geoBase.getCountryForAirportOrElse("CDG") == "FR")
	  * assert(geoBase.getCountryForAirportOrElse("?*#", "") == "")
	  * }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find a corresponding country.
	  *
	  * @param airport the airport IATA code (for instance CDG) for which to get
	  * the associated country.
	  * @param orElse the value to return if no country could be associated to
	  * the requested airport.
	  * @return the country code corresponding to the given airport (for
	  * instance FR).
	  */
	def getCountryForAirportOrElse(airport: String, orElse: String = ""): String = {
		getCountryForCityOrElse(airport, orElse)
	}

	/** Returns the continent associated to the given airport.
	  *
	  * Possible values: EU (Eurrope) - NA (North America) - SA (South Africa) -
	  * AF (Africa) - AS (Asia) - AN (Antarctica) - OC (Oceania).
	  *
	  * {{{
	  * assert(geoBase.getContinentForAirportOrElse("CDG") == "EU")
	  * assert(geoBase.getContinentForAirportOrElse("?*#", "") == "")
	  * }}}
	  *
	  * @param airport the airport IATA code (for instance PAR) for which to get
	  * the associated continent.
	  * @param orElse the value to return if no continent could be associated to
	  * the requested airport.
	  * @return the continent code corresponding to the given airport (for
	  * instance EU).
	  */
	def getContinentForAirportOrElse(airport: String, orElse: String = ""): String = {
		getContinentForLocationOrElse(airport, orElse)
	}

	/** Returns the continent associated to the given city.
	  *
	  * Possible values: EU (Eurrope) - NA (North America) - SA (South Africa) -
	  * AF (Africa) - AS (Asia) - AN (Antarctica) - OC (Oceania).
	  *
	  * {{{
	  * assert(geoBase.getContinentForCityOrElse("NYC") == "NA")
	  * assert(geoBase.getContinentForCityOrElse("?*#", "") == "")
	  * }}}
	  *
	  * @param city the city IATA code (for instance NYC) for which to get the
	  * associated continent.
	  * @param orElse the value to return if no continent could be associated to
	  * the requested city.
	  * @return the continent code corresponding to the given city (for
	  * instance EU).
	  */
	def getContinentForCityOrElse(city: String, orElse: String = ""): String = {
		getContinentForLocationOrElse(city, orElse)
	}

	/** Returns the continent associated to the given country.
	  *
	  * Possible values: EU (Eurrope) - NA (North America) - SA (South Africa) -
	  * AF (Africa) - AS (Asia) - AN (Antarctica) - OC (Oceania).
	  *
	  * {{{
	  * assert(geoBase.getContinentForCountryOrElse("CN") == "AS")
	  * assert(geoBase.getContinentForCountryOrElse("?*#", "") == "")
	  * }}}
	  *
	  * @param country the country IATA code (for instance US) for which to get
	  * the associated continent.
	  * @param orElse the value to return if no continent could be associated to
	  * the requested country.
	  * @return the continent code corresponding to the given country (for
	  * instance NA).
	  */
	def getContinentForCountryOrElse(country: String, orElse: String = ""): String = {
		getContinentForLocationOrElse(country, orElse)
	}

	/** Returns the continent associated to the given airport.
	  *
	  * Possible values: EU (Eurrope) - NA (North America) - SA (South Africa) -
	  * AF (Africa) - AS (Asia) - AN (Antarctica) - OC (Oceania).
	  *
	  * {{{ assert(geoBase.getContinentForAirport("CDG") == "EU") }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find a corresponding
	  * continent.
	  *
	  * @param airport the airport IATA code (for instance CDG) for which to get
	  * the associated continent.
	  * @return the continent code corresponding to the given airport (for
	  * instance EU).
	  * @throws classOf[GeoBaseException]
	  */
	def getContinentForAirport(airport: String): String = {
		getContinentForLocation(airport)
	}

	/** Returns the continent associated to the given city.
	  *
	  * Possible values: EU (Eurrope) - NA (North America) - SA (South Africa) -
	  * AF (Africa) - AS (Asia) - AN (Antarctica) - OC (Oceania).
	  *
	  * {{{ assert(geoBase.getContinentForCity("PAR") == "EU") }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find a corresponding
	  * continent.
	  *
	  * @param city the city IATA code (for instance CDG) for which to get
	  * the associated continent.
	  * @return the continent code corresponding to the given city (for
	  * instance EU).
	  * @throws classOf[GeoBaseException]
	  */
	def getContinentForCity(city: String): String = {
		getContinentForLocation(city)
	}

	/** Returns the continent associated to the given country.
	  *
	  * Possible values: EU (Eurrope) - NA (North America) - SA (South Africa) -
	  * AF (Africa) - AS (Asia) - AN (Antarctica) - OC (Oceania).
	  *
	  * {{{ assert(geoBase.getContinentForCountry("FR") == "EU") }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find a corresponding
	  * continent.
	  *
	  * @param country the country IATA code (for instance FR) for which to get
	  * the associated continent.
	  * @return the continent code corresponding to the given country (for
	  * instance EU).
	  * @throws classOf[GeoBaseException]
	  */
	def getContinentForCountry(country: String): String = {
		getContinentForLocation(country)
	}

	/** Returns the IATA zone associated to the given airport.
	  *
	  * Possible values are 11, 12, 13, 21, 22, 23, 31, 32 or 33.
	  *
	  * {{{
	  * assert(geoBase.getIataZoneForAirportOrElse("CDG", "") == "21")
	  * assert(geoBase.getIataZoneForAirportOrElse("?*#", "") == "")
	  * }}}
	  *
	  * @param airport the airport IATA code (for instance CDG) for which to get
	  * the associated IATA zone.
	  * @param orElse the value to return if no continent could be associated to
	  * the requested airport.
	  * @return the IATA zone code corresponding to the given airport (for
	  * instance 21).
	  */
	def getIataZoneForAirportOrElse(airport: String, orElse: String = ""): String = {
		getIataZoneForLocationOrElse(airport, orElse)
	}

	/** Returns the IATA zone associated to the given city.
	  *
	  * Possible values are 11, 12, 13, 21, 22, 23, 31, 32 or 33.
	  *
	  * {{{
	  * assert(geoBase.getIataZoneForCityOrElse("NYC", "") == "11")
	  * assert(geoBase.getIataZoneForCityOrElse("?*#", "") == "")
	  * }}}
	  *
	  * @param city the city IATA code (for instance NYC) for which to get the
	  * associated IATA zone.
	  * @param orElse the value to return if no continent could be associated to
	  * the requested city.
	  * @return the IATA zone code corresponding to the given city (for
	  * instance 11).
	  */
	def getIataZoneForCityOrElse(city: String, orElse: String = ""): String = {
		getIataZoneForLocationOrElse(city, orElse)
	}

	/** Returns the IATA zone associated to the given country.
	  *
	  * Possible values are 11, 12, 13, 21, 22, 23, 31, 32 or 33.
	  *
	  * {{{
	  * assert(geoBase.getIataZoneForCountryOrElse("ZA", "") == "23")
	  * assert(geoBase.getIataZoneForCountryOrElse("?*#", "") == "")
	  * }}}
	  *
	  * @param country the country IATA code (for instance ZA) for which to get
	  * the associated IATA zone.
	  * @param orElse the value to return if no continent could be associated to
	  * the requested country.
	  * @return the IATA zone code corresponding to the given country (for
	  * instance 23).
	  */
	def getIataZoneForCountryOrElse(country: String, orElse: String = ""): String = {
		getIataZoneForLocationOrElse(country, orElse)
	}

	/** Returns the IATA zone associated to the given airport.
	  *
	  * Possible values are 11, 12, 13, 21, 22, 23, 31, 32 or 33.
	  *
	  * {{{ assert(geoBase.getIataZoneForAirport("CDG") == "21") }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find a corresponding IATA
	  * zone.
	  *
	  * @param airport the airport IATA code (for instance CDG) for which to get
	  * the associated IATA zone.
	  * @return the IATA zone code corresponding to the given airport (for
	  * instance 21).
	  * @throws classOf[GeoBaseException]
	  */
	def getIataZoneForAirport(airport: String): String = {
		getIataZoneForLocation(airport)
	}

	/** Returns the IATA zone associated to the given city.
	  *
	  * Possible values are 11, 12, 13, 21, 22, 23, 31, 32 or 33.
	  *
	  * {{{ assert(geoBase.getIataZoneForCity("NYC") == "11") }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find a corresponding IATA
	  * zone.
	  *
	  * @param city the city IATA code (for instance NYC) for which to get the
	  * associated IATA zone.
	  * @return the IATA zone code corresponding to the given city (for
	  * instance 11).
	  * @throws classOf[GeoBaseException]
	  */
	def getIataZoneForCity(city: String): String = getIataZoneForLocation(city)

	/** Returns the IATA zone associated to the given country.
	  *
	  * Possible values are 11, 12, 13, 21, 22, 23, 31, 32 or 33.
	  *
	  * {{{ assert(geoBase.getIataZoneForCountry("ZA") == "23") }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find a corresponding IATA
	  * zone.
	  *
	  * @param country the country IATA code (for instance ZA) for which to get
	  * the associated IATA zone.
	  * @return the IATA zone code corresponding to the given country (for
	  * instance 23).
	  * @throws classOf[GeoBaseException]
	  */
	def getIataZoneForCountry(country: String): String = {
		getIataZoneForLocation(country)
	}

	/** Returns the currency associated to the given city.
	  *
	  * {{{ assert(geoBase.getCurrencyForCity("NYC") == "USD") }}}
	  *
	  * Throws a GeoBaseException if GeoBase can't find a corresponding currency.
	  *
	  * @param city the city IATA code (for instance NYC) for which to get
	  * the associated currency.
	  * @return the currency code corresponding to the given city (for instance
	  * USD).
	  * @throws classOf[GeoBaseException]
	  */
	def getCurrencyForCity(city: String): String = getCurrencyForCountry(city)

	/** Returns the currency associated to the given city.
	  *
	  * {{{
	  * assert(geoBase.getCurrencyForCityOrElse("PAR", "") == "EUR")
	  * assert(geoBase.getCurrencyForCityOrElse("?#", "") == "")
	  * assert(geoBase.getCurrencyForCityOrElse("?#", "USD") == "USD")
	  * }}}
	  *
	  * @param city the city IATA code (for instance PAR) for which to get
	  * the associated currency.
	  * @param orElse the value to return if no currency could be associated to
	  * the requested city.
	  * @return the currency code corresponding to the given city (for
	  * instance EUR).
	  */
	def getCurrencyForCityOrElse(city: String, orElse: String = ""): String = {
		getCurrencyForCountryOrElse(city, orElse)
	}

	/** Returns the elapsed flying time between two locations (airport or city).
	  *
	  * Trip duration is a synonym of elapsed flying time (EFT).
	  *
	  * This is meant to be used to compute the trip duration for a
	  * segment/bound for which we know the origin/destination airports/cities
	  * and the local time. i.e. when we don't have gmt times.
	  *
	  * {{{
	  * assert(geoBase.getEFTfromLocalDates("20160606_1627", "CDG", "20160606_1757", "JFK") == 7.5f)
	  *
	  * val computedTripDuration = geoBase.getEFTfromLocalDates(
	  * 	"2016-06-06T16:27", "CDG", "2016-06-06T17:57", "JFK",
	  * 	format = "yyyy-MM-dd'T'HH:mm", unit = "minutes"
	  * )
	  * assert(computedTripDuration == 450f)
	  * }}}
	  *
	  * Throws a GeoBaseException if GeoBase doesn't have an entry for the
	  * requested airports/cities.
	  *
	  * @param localDepartureDate the departure local date
	  * @param originAirportOrCity the origin airport or city
	  * @param localArrivalDate the arrival local date
	  * @param destinationAirportOrCity the destination airport or city
	  * @param unit (default = "hours") either "hours" or "minutes"
	  * @param format (default = "yyyyMMdd_HHmm") the format under which local
	  * departure and arrival dates are provided.
	  * @return the trip duration in the chosen unit (in hours by default) and
	  * format.
	  * @throws classOf[GeoBaseException]
	  */
	def getEFTfromLocalDates(
		localDepartureDate: String, originAirportOrCity: String,
		localArrivalDate: String, destinationAirportOrCity: String,
		unit: String = "hours", format: String = "yyyyMMdd_HHmm"
	): Float = {
		getTripDurationFromLocalDates(
			localDepartureDate, originAirportOrCity,
			localArrivalDate, destinationAirportOrCity, unit, format
		)
	}

	/** Throws an exception when the item requested is not part of the mapping.
	  *
	  * Checks wheter the requested airport or country or any item is a key of
	  * the mapping in question. If yes, this method does nothing; otherwise, we
	  * throw an exception.
	  *
	  * @param itemCode the airline or the country or any item
	  * @param mapping the mapping for which we check the itemCode key existence
	  * @throws (classOf[GeoBaseException])
	  */
	private def checkExistence(itemCode: String, mapping: Map[String, Any]) = {
		if (!mapping.contains(itemCode))
			throw GeoBaseException("No entry in GeoBase for \"" + itemCode + "\"")
	}
}

/** An enumeration which represents the possible geograpic types of a trip */
object GeoType extends Enumeration {
	type GeoType = Value
	val DOMESTIC, CONTINENTAL, INTER_CONTINENTAL = Value
}
