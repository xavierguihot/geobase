package com.geobase.model

/** An enumeration which represents the durations provided as parameter of
  * different functions such as the one returning trip duration */
sealed trait Duration

case object MINUTES extends Duration
case object HOURS extends Duration
