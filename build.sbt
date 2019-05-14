organization    := "io.github.serjorez"
name            := "hcurrency"
version         := "0.0.1-SNAPSHOT"
scalaVersion    := "2.12.8"

val CatsVersion            = "1.6.0"
val CatsEffectVersion      = "1.3.0"
val CirceVersion           = "0.11.1"
val CirceConfigVersion     = "0.6.1"

libraryDependencies ++= Seq(
"org.typelevel" %% "cats-core"     % CatsVersion,
"org.typelevel" %% "cats-effect"   % CatsEffectVersion,
"io.circe"      %% "circe-core"    % CirceVersion,
"io.circe"      %% "circe-generic" % CirceVersion,
"io.circe"      %% "circe-parser"  % CirceVersion,
"io.circe"      %% "circe-config"  % CirceConfigVersion
)

scalacOptions += "-Ypartial-unification"
