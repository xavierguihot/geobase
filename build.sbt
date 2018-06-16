name := "geobase"

version := "2.0.0"

scalaVersion := "2.11.12"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Ywarn-unused-import",
  "-Ywarn-unused"
)

assemblyJarName in assembly := name.value + "-" + version.value + ".jar"

assemblyOutputPath in assembly := file(
  "./" + name.value + "-" + version.value + ".jar")

testOptions in Test += Tests.Argument("-oD")
parallelExecution in Test := false

wartremoverWarnings in (Compile, compile) ++= Warts.all
wartremoverWarnings in (Compile, compile) --= Seq(
  Wart.DefaultArguments,
  Wart.Nothing,
  Wart.Equals
)

scalafmtOnCompile := true

val catsVersion = "1.0.1"
val scalatestVersion = "3.0.4"
val sparkVersion = "2.1.0"
val sparkTestVersion = "2.1.0_0.8.0"

libraryDependencies ++= Seq(
  "org.typelevel"    %% "cats-core"          % catsVersion,
  "org.scalatest"    %% "scalatest"          % scalatestVersion % "test",
  "org.apache.spark" %% "spark-core"         % sparkVersion % "test",
  "com.holdenkarau"  %% "spark-testing-base" % sparkTestVersion % "test"
)
