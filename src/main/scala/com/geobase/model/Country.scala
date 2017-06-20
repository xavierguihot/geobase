package com.geobase.model

/** A country.
  *
  * @author Xavier Guihot
  * @since 2017-04
  */
private[geobase] case class Country(
	currencyCode: String, continentCode: String, iataZone: String
)
