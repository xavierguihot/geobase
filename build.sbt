name := "geobase"

version := "1.1.3"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Ywarn-unused-import"
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
