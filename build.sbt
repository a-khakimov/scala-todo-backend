version := "0.1"

lazy val root = (project in file("."))
  .settings(Coverage.Settings)
  .settings(
    inThisBuild(
      List(
        organization := "org.github.ainr",
        scalaVersion := "2.13.6",
        developers := List(
          Developer(
            "ainr",
            "Ainur Khakimov",
            "hak.ain@yandex.ru",
            url("https://github.com/a-khakimov")
          )
        )
      )
    ),
    name := "TodoBackend",
    libraryDependencies ++= Dependencies.App,
    Compile / scalacOptions := Options.scalacOptions(scalaVersion.value, isSnapshot.value)
  )

sonarProperties := Sonar.properties
