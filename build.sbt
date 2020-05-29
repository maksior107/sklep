name := "sklep"

version := "1.0"

lazy val `sklep` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "Atlassian Releases" at "https://maven.atlassian.com/public/"
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "4.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.0",
  "org.xerial" % "sqlite-jdbc" % "3.30.1",
  "com.mohiva" %% "play-silhouette" % "7.0.0",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "7.0.0",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "7.0.0",
  "com.mohiva" %% "play-silhouette-persistence" % "7.0.0",
  "com.mohiva" %% "play-silhouette-testkit" % "7.0.0" % "test",
  "org.webjars" %% "webjars-play" % "2.7.0",
  "org.webjars" % "bootstrap" % "3.3.7-1" exclude("org.webjars", "jquery"),
  "org.webjars" % "jquery" % "3.2.1",
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.8.4-akka-2.6.x",
  "net.codingwell" %% "scala-guice" % "4.1.0",
  "com.iheart" %% "ficus" % "1.4.3",
  "com.adrianhurt" %% "play-bootstrap" % "1.6.1-P28-B4",
  "com.typesafe.play" %% "play-mailer" % "7.0.0",
  "com.typesafe.play" %% "play-mailer-guice" % "7.0.0",
  //  jdbc,
  ehcache, ws, specs2 % Test, guice)

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

      