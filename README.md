
# GeoBase [![Build Status](https://travis-ci.org/XavierGuihot/geobase.svg?branch=master)](https://travis-ci.org/XavierGuihot/geobase)


## Overview


Version: 1.0.2

API Scaladoc: [GeoBase](http://xavierguihot.github.io/geobase)

Scala wrapper around opentraveldata (geo/travel data).

Provides geographical mappings at airport/city/country level mainly based on
[opentraveldata](https://github.com/opentraveldata/opentraveldata) as well as
other mappings such as airlines or currencies. This facility also provides
classic time-oriented methods such as trip duration computation.

Geo/Travel facilities:

* Airport to City converter
* Airport/City to Country converter
* Airport/City/Country to Continent converter
* Airport/City/Country to IATA zone converter
* City/Country to Currency code converter
* Airline to Country converter
* Distance getter between two airports/cities
* Trip Type getter (domestic, continental or intercontinental)
* Conversion from local time to GMT time (without needing to know the GMT offset) (and vice et versa)
* A Segment EFT getter for local dates (without needing to know the GMT offset)
* A way to retrieve nearby airports within a defined radius

Inspired by [neobase](https://github.com/alexprengere/neobase) for python users.


## Using geobase:


The full list of methods is available at
[GeoBase doc](http://xavierguihot.github.io/geobase)

	import com.geobase.GeoBase

	val geoBase = new GeoBase()

	assert(geoBase.getCityForAirport("CDG") == "PAR")


## Including geobase to your dependencies:


With sbt, just add this one line to your build.sbt:

	libraryDependencies += "geobase" % "geobase" % "1.0.2" from "https://github.com/xavierguihot/geobase/releases/download/v1.0.2/geobase-1.0.2.jar"


## Building the project:


First import data from opentraveldata with the update_data.sh script. You can
then build the project with sbt:

	./update_data.sh
	sbt assembly
