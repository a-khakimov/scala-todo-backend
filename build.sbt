version := "0.1"

inThisBuild(
  List(
    organization := "org.github.ainr",
    developers := List(
      Developer(
        "ainr",
        "Ainur Khakimov",
        "hak.ain@yandex.ru",
        url("https://github.com/a-khakimov")
      )
    ),
    scalaVersion := "2.13.6"
  )
)

lazy val root = (project in file("."))
  .settings(
    name := "todo_backend",
    libraryDependencies ++= Dependencies.App,
    Compile / scalacOptions := Options.scalacOptions(scalaVersion.value, isSnapshot.value)
  )

sonarProperties := Sonar.properties
