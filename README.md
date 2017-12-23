
# GeoBase [![Build Status](https://travis-ci.org/XavierGuihot/geobase.svg?branch=master)](https://travis-ci.org/XavierGuihot/geobase)


## Overview


Version: 1.0.4

API Scaladoc: [GeoBase](http://xavierguihot.com/geobase/#com.geobase.GeoBase)

Scala wrapper around opentraveldata (geo/travel data).

Provides geographical mappings at airport/city/country level mainly based on
[opentraveldata](https://github.com/opentraveldata/opentraveldata) as well as
other mappings (airlines, currencies, ...). This tool also provides classic
time-oriented methods such as the computation of a trip duration.

Geo/Travel facilities:

* Airport to city converter
* Airport/city to country converter
* Airport/city/country to continent converter
* Airport/city/country to IATA zone converter
* City/country to currency code converter
* Airline to country converter
* Distance getter between two airports/cities
* Trip type getter (domestic, continental or intercontinental)
* Conversion from local time to GMT time (without needing the GMT offset of a location) (and vice et versa)
* A trip duration (elapsed flying time) getter from local dates (without needing the locations' GMT offsets)
* A way to retrieve nearby airports within a defined radius

Inspired by [neobase](https://github.com/alexprengere/neobase) for python users.


## Using geobase:


The full list of methods is available at
[GeoBase doc](http://xavierguihot.com/geobase/#com.geobase.GeoBase)

Here is a non-exhaustive list of examples:

	import com.geobase.GeoBase

	val geoBase = new GeoBase()

	assert(geoBase.getCityForAirport("CDG") == "PAR")
	assert(geoBase.getCountryForAirline("AF") == "FR")
	assert(geoBase.getCountryForAirport("CDG") == "FR")
	assert(geoBase.getCurrencyForCity("NYC") == "USD")
	assert(geoBase.getDistanceBetween("PAR", "NCE") == 686)
	assert(geoBase.getTripDurationFromLocalDates("20160606_1627", "CDG", "20160606_1757", "JFK") == 7.5d)
	assert(geoBase.getNearbyAirportsWithDetails("CDG", 50) == List("LBG", "ORY", "VIY", "POX"))


## Including geobase to your dependencies:


With sbt, just add this one line to your build.sbt:

	libraryDependencies += "geobase" % "geobase" % "1.0.4" from "https://github.com/xavierguihot/geobase/releases/download/v1.0.4/geobase-1.0.4.jar"


## Building the project:


First import data from opentraveldata with the update_data.sh script. You can
then build the project with sbt:

	./update_data.sh
	sbt assembly
