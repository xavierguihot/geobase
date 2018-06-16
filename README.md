
# GeoBase [![Build Status](https://travis-ci.org/xavierguihot/geobase.svg?branch=master)](https://travis-ci.org/xavierguihot/geobase) [![Coverage Status](https://coveralls.io/repos/github/xavierguihot/geobase/badge.svg?branch=master)](https://coveralls.io/github/xavierguihot/geobase?branch=master) [![Release](https://jitpack.io/v/xavierguihot/geobase.svg)](https://jitpack.io/#xavierguihot/geobase)


## Overview


API Scaladoc: [GeoBase](http://xavierguihot.com/geobase/#com.geobase.GeoBase$)

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
[GeoBase doc](http://xavierguihot.com/geobase/#com.geobase.GeoBase$)

Here is a non-exhaustive list of available methods:

```scala
import com.geobase.GeoBase

GeoBase.city("CDG") // Success("PAR")
GeoBase.country("CDG") // Success("FR")
GeoBase.continent("JFK") // Success("NA")
GeoBase.iataZone("LON") // Success("21")
GeoBase.currency("NYC") // Success("USD")
GeoBase.countryForAirline("AF") // Success("FR")
GeoBase.timeZone("PAR") // Success("Europe/Paris")
GeoBase.distanceBetween("PAR", "NCE") // Success(686)
GeoBase.localDateToGMT("20160606_2227", "NYC") // Success("20160607_0227")
GeoBase.gmtDateToLocal("20160607_0227", "NYC") // Success("20160606_2227")
GeoBase.offsetForLocalDate("20171224", "NYC") // Success(-300)
GeoBase.tripDurationFromLocalDates("20160606_1627", "CDG", "20160606_1757", "JFK") // Success(7.5d)
GeoBase.geoType(List("CDG", "TLS", "DUB", "FRA")) // Success(CONTINENTAL)
GeoBase.nearbyAirports("CDG", 50) // Success(List("LBG", "ORY", "VIY", "POX"))
GeoBase.nameOfAirline("AF") // Success("Air France")
```

These functions can also be called as attachments to Strings:

```scala
import com.geobase.GeoBase.StringExtensions

"CDG".city // Success("PAR")
"PAR".country // Success("FR")
"CDG".continent // Success("EU")
"CDG".iataZone // Success("21")
"JFK".currency // Success("USD")
"AF".name // Success("Air France")
"CDG".timeZone // Success("Europe/Paris")
"LON".distanceWith("NYC") // Success(5568)
"CDG".nearbyAirports(50) // Success(List("LBG", "ORY", "VIY", "POX"))
```

Getters all have a return type embedded within the Try monad. Throwing
exceptions when one might request mappings for non existing locations, isn't
really the idiomatic scala way, and simply embedding the result in the Option
monad doesn't give the user the possibility to understand what went wrong.
Thus the usage of the Try monad.


## Including geobase to your dependencies:


With sbt:

```scala
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.github.xavierguihot" % "geobase" % "2.0.0"
```

With maven:

```xml
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>

<dependency>
	<groupId>com.github.xavierguihot</groupId>
	<artifactId>geobase</artifactId>
	<version>2.0.0</version>
</dependency>
```

With gradle:

```groovy
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}

dependencies {
	compile 'com.github.xavierguihot:geobase:2.0.0'
}
```

For versions anterior to `2.0.0`, use prefix `v` in the version tag; for
instance `v1.0.0`


## Building the project:


First import data from opentraveldata with the update_data.sh script. You can
then build the project with sbt:

	./update_data.sh
	sbt assembly
