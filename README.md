
# GeoBase [![Build Status](https://travis-ci.org/xavierguihot/geobase.svg?branch=master)](https://travis-ci.org/xavierguihot/geobase) [![Coverage Status](https://coveralls.io/repos/github/xavierguihot/geobase/badge.svg?branch=master)](https://coveralls.io/github/xavierguihot/geobase?branch=master) [![Release](https://jitpack.io/v/xavierguihot/geobase.svg)](https://jitpack.io/#xavierguihot/geobase)


## Overview


Version: 1.1.2

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

```scala
import com.geobase.GeoBase

val geoBase = new GeoBase()

assert(geoBase.getCityFor("CDG").get == "PAR")
assert(geoBase.getCountryFor("CDG").get == "FR")
assert(geoBase.getCountryForAirline("AF").get == "FR")
assert(geoBase.getCurrencyFor("NYC").get == "USD")
assert(geoBase.getDistanceBetween("PAR", "NCE").get == 686)
assert(geoBase.getTripDurationFromLocalDates("20160606_1627", "CDG", "20160606_1757", "JFK").get == 7.5d)
assert(geoBase.getNearbyAirports("CDG", 50).get == List("LBG", "ORY", "VIY", "POX"))
```

Getters all have a return type embedded within the Try monade. Throwing
exceptions when one might request mappings for non existing locations, isn't
realy the idiomatic of scala, and simply embedding the result in the Option
monad doesn't give the user the possibility to understand what went wrong.
Thus the usage of the Try monade.


## Including geobase to your dependencies:


With sbt, add these lines to your build.sbt:

```
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.github.xavierguihot" % "geobase" % "v1.1.2"
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
	<version>v1.1.2</version>
</dependency>
```

With gradle, add these lines to your build.gradle:

```
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}

dependencies {
	compile 'com.github.xavierguihot:geobase:v1.1.2'
}
```


## Building the project:


First import data from opentraveldata with the update_data.sh script. You can
then build the project with sbt:

	./update_data.sh
	sbt assembly
