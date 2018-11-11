name := "root"

version := "1.0-SNAPSHOT"


lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.12.6"

crossScalaVersions := Seq("2.11.12", "2.12.4")


// Test Database
libraryDependencies += "com.h2database" % "h2" % "1.4.197"
libraryDependencies += "mysql" % "mysql-connector-java" % "6.0.4"

// Testing libraries for dealing with CompletionStage...
libraryDependencies += "org.assertj" % "assertj-core" % "3.6.2" % Test
libraryDependencies += "org.awaitility" % "awaitility" % "2.0.0" % Test
libraryDependencies += "com.typesafe.play" %% "play-ebean" % "4.0.1"
libraryDependencies += guice



// Make verbose tests
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))
