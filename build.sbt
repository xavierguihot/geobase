name := "geobase"

version := "1.1.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xfatal-warnings")

assemblyJarName in assembly := name.value + "-" + version.value + ".jar"

assemblyOutputPath in assembly := file("./" + name.value + "-" + version.value + ".jar")

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
