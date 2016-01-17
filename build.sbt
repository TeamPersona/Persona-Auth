name         := """Persona-Auth"""
organization := "Project Persona"
version      := "0.1-SNAPSHOT"
scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaHttpV = "2.0.1"
  val scalaTestV  = "3.0.0-M15"
  val scalaMockV = "3.2.2"
  val nimbusJwtV = "4.9"
  val nscalaTimeV = "2.6.0"

  Seq(
    "com.typesafe.akka" %% "akka-stream-experimental"          % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-core-experimental"       % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-experimental"            % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-testkit-experimental"    % akkaHttpV,
    "org.scalatest" %% "scalatest" % scalaTestV % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % scalaMockV % "test",
    "com.nimbusds" % "nimbus-jose-jwt" % nimbusJwtV,
    "com.github.nscala-time" %% "nscala-time" % nscalaTimeV
  )
}

// Coveralls settings
ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 70
ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := false
ScoverageSbtPlugin.ScoverageKeys.coverageHighlighting := true

