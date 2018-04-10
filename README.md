
# GeoBase [![Build Status](https://travis-ci.org/xavierguihot/geobase.svg?branch=master)](https://travis-ci.org/xavierguihot/geobase) [![Coverage Status](https://coveralls.io/repos/github/xavierguihot/geobase/badge.svg?branch=master)](https://coveralls.io/github/xavierguihot/geobase?branch=master) [![Release](https://jitpack.io/v/xavierguihot/geobase.svg)](https://jitpack.io/#xavierguihot/geobase)


## Overview


Version: 1.2.2

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

Here is a non-exhaustive list of available methods:

```scala
import com.geobase.GeoBase

val geoBase = new GeoBase()

assert(geoBase.city("CDG") == Success("PAR"))
assert(geoBase.country("CDG") == Success("FR"))
assert(geoBase.continent("JFK") == Success("NA"))
assert(geoBase.iataZone("LON") == Success("21"))
assert(geoBase.currency("NYC") == Success("USD"))
assert(geoBase.countryForAirline("AF") == Success("FR"))
assert(geoBase.timeZone("PAR") == Success("Europe/Paris"))
assert(geoBase.distanceBetween("PAR", "NCE") == Success(686))
assert(geoBase.localDateToGMT("20160606_2227", "NYC") == Success("20160607_0227"))
assert(geoBase.gmtDateToLocal("20160607_0227", "NYC") == Success("20160606_2227"))
assert(geoBase.offsetForLocalDate("20171224", "NYC") == Success(-300))
assert(geoBase.tripDurationFromLocalDates("20160606_1627", "CDG", "20160606_1757", "JFK") == Success(7.5d))
assert(geoBase.geoType(List("CDG", "TLS", "DUB", "FRA")) == Success(CONTINENTAL))
assert(geoBase.nearbyAirports("CDG", 50) == Success(List("LBG", "ORY", "VIY", "POX")))
assert(geoBase.nameOfAirline("AF") == Success("Air France"))
```

Getters all have a return type embedded within the Try monade. Throwing
exceptions when one might request mappings for non existing locations, isn't
realy the idiomatic scala way, and simply embedding the result in the Option
monade doesn't give the user the possibility to understand what went wrong.
Thus the usage of the Try monade.


## Including geobase to your dependencies:


With sbt, add these lines to your build.sbt:

```scala
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.github.xavierguihot" % "geobase" % "v1.2.1"
```

With maven, add these lines to your pom.xml:

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
	<version>v1.2.1</version>
</dependency>
```

With gradle, add these lines to your build.gradle:

```groovy
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}

dependencies {
	compile 'com.github.xavierguihot:geobase:v1.2.1'
}
```


## Building the project:


First import data from opentraveldata with the update_data.sh script. You can
then build the project with sbt:

	./update_data.sh
	sbt assembly
