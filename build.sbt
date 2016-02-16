name         := """Persona-Auth"""
organization := "Project Persona"
version      := "0.1-SNAPSHOT"
scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaHttpV = "2.0.1"
  val scalaTestV  = "3.0.0-M15"
  val scalaMockV = "3.2.2"
  val nimbusOAuth = "5.1"
  val nscalaTimeV = "2.6.0"
  val slickV = "3.1.1"
  val postgresV = "9.4.1207"

  Seq(
    "com.typesafe.akka" %% "akka-stream-experimental"          % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-core-experimental"       % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-experimental"            % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-testkit-experimental"    % akkaHttpV,
    "com.typesafe.slick" %% "slick" % slickV,
    "org.postgresql" % "postgresql" % postgresV,
    "org.scalatest" %% "scalatest" % scalaTestV % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % scalaMockV % "test",
    "com.nimbusds" % "oauth2-oidc-sdk" % nimbusOAuth,
    "com.github.nscala-time" %% "nscala-time" % nscalaTimeV
  )
}

// Coveralls settings
ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 70
ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := false
ScoverageSbtPlugin.ScoverageKeys.coverageHighlighting := true

