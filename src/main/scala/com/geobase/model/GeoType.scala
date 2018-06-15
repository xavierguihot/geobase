package com.geobase.model

/** An enumeration which represents the geographic types of a trip */
sealed trait GeoType

case object DOMESTIC extends GeoType
case object CONTINENTAL extends GeoType
case object INTER_CONTINENTAL extends GeoType
