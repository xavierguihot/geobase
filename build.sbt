name := "geobase"

version := "1.2.0"

scalaVersion := "2.11.12"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Ywarn-unused-import",
  "-Ywarn-unused"
)

wartremoverWarnings in (Compile, compile) ++= Warts.all
wartremoverWarnings in (Compile, compile) --= Seq(
  Wart.DefaultArguments,
  Wart.Nothing,
  Wart.Equals
)

assemblyJarName in assembly := name.value + "-" + version.value + ".jar"

assemblyOutputPath in assembly := file(
  "./" + name.value + "-" + version.value + ".jar")

scalafmtOnCompile := true

val catsVersion = "1.0.1"
val scalatestVersion = "3.0.1"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion % "test"
)
