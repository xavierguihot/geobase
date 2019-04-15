package com.geobase.error

/** Exception thrown anytime requested data can't be found by GeoBase */
final case class GeoBaseException private[geobase] (message: String) extends Exception(message)
