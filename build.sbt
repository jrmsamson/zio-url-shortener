ThisBuild / scalaVersion := "2.13.4"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.jerome"
ThisBuild / organizationName := "jerome"

val ZioVersion           = "2.0.0-RC1"
val ZioCatsVersion       = "3.3.0-RC1"
val Http4sVersion        = "1.0.0-M30"
val DoobieVersion        = "1.0.0-RC1"
val PureConfigVersion    = "0.14.0"
val CirceVersion         = "0.14.1"
val Slf4jVersion         = "1.7.30"
val ScalaTestPlusVersion = "3.2.3.0"
val KindProjectorVersion = "0.13.2"

lazy val root = (project in file("."))
  .settings(
    name := "url-shortener-service",
    libraryDependencies ++= Seq(
      // ZIO
      "dev.zio" %% "zio"               % ZioVersion,
      "dev.zio" %% "zio-interop-cats"  % ZioCatsVersion,
      "dev.zio" %% "zio-test"          % ZioVersion % "test",
      "dev.zio" %% "zio-test-sbt"      % ZioVersion % "test",
      "dev.zio" %% "zio-test-magnolia" % ZioVersion % "test",
      // PureConfig
      "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
      // Slf4j
      "org.slf4j" % "slf4j-log4j12" % Slf4jVersion,
      // http4s
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe"        % Http4sVersion,
      "org.http4s" %% "http4s-dsl"          % Http4sVersion,
      // doobie
      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-h2"   % DoobieVersion,
      // circe
      "io.circe"                     %% "circe-core"     % CirceVersion,
      "io.circe"                     %% "circe-generic"  % CirceVersion,
      "io.circe"                     %% "circe-parser"   % CirceVersion,
      compilerPlugin(("org.typelevel" % "kind-projector" % KindProjectorVersion).cross(CrossVersion.full))
    ),
    testFrameworks ++= Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    scalacOptions ++= Seq(
      "-deprecation",                  // Emit warning and location for usages of deprecated APIs.
      "-explaintypes",                 // Explain type errors in more detail.
      "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros", // Allow macro definition (besides implementation and application)
      "-language:higherKinds",         // Allow higher-kinded types
      "-language:implicitConversions", // Allow definition of implicit functions called views
      "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
      "-Xcheckinit",                   // Wrap field accessors to throw an exception on uninitialized access.
      "-Xfatal-warnings",              // Fail the compilation if there are any warnings.
      "-Xlint:adapted-args",           // Warn if an argument list is modified to match the receiver.
      "-Xlint:constant",               // Evaluation of a constant arithmetic expression results in an error.
      "-Xlint:delayedinit-select",     // Selecting member of DelayedInit.
      "-Xlint:doc-detached",           // A Scaladoc comment appears to be detached from its element.
      "-Xlint:inaccessible",           // Warn about inaccessible types in method signatures.
      "-Xlint:infer-any",              // Warn when a type argument is inferred to be `Any`.
      "-Xlint:missing-interpolator",   // A string literal appears to be missing an interpolator id.
      "-Xlint:nullary-unit",           // Warn when nullary methods return Unit.
      "-Xlint:option-implicit",        // Option.apply used implicit view.
      "-Xlint:package-object-classes", // Class or object defined in package object.
      "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:private-shadow",         // A private field (or class parameter) shadows a superclass field.
      "-Xlint:stars-align",            // Pattern sequence wildcard must align with sequence component.
      "-Xlint:type-parameter-shadow",  // A local type parameter shadows a type already in scope.
      "-Ywarn-dead-code",              // Warn when dead code is identified.
      "-Ywarn-extra-implicit",         // Warn when more than one implicit parameter section is defined.
      "-Ywarn-numeric-widen",          // Warn when numerics are widened.
      "-Ywarn-unused:implicits",       // Warn if an implicit parameter is unused.
//      "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
      //"-Ywarn-unused:locals", // Warn if a local definition is unused.
      "-Ywarn-unused:params",   // Warn if a value parameter is unused.
      "-Ywarn-unused:patvars",  // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:privates", // Warn if a private member is unused.
      "-Ywarn-value-discard",   // Warn when non-Unit expression results are unused.
      "-Ybackend-parallelism",
      "8",                                         // Enable paralellisation — change to desired number!
      "-Ycache-plugin-class-loader:last-modified", // Enables caching of classloaders for compiler plugins
      "-Ycache-macro-class-loader:last-modified"   // and macro definitions. This can lead to performance improvements.
    )
  )

inThisBuild(
  List(
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)
