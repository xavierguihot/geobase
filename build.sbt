name := "geobase"

version := "1.0.0"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

assemblyJarName in assembly := name.value + "-" + version.value + ".jar"

assemblyOutputPath in assembly := file("./" + name.value + "-" + version.value + ".jar")

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"
