name := "geobase"

version := "1.1.3"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

wartremoverWarnings in (Compile, compile) ++= Warts.all
wartremoverWarnings in (Compile, compile) --= Seq(
	Wart.DefaultArguments, Wart.Nothing, Wart.Equals
)

assemblyJarName in assembly := name.value + "-" + version.value + ".jar"

assemblyOutputPath in assembly := file("./" + name.value + "-" + version.value + ".jar")

libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
