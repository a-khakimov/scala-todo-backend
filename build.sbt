
lazy val root = (project in file("."))
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

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.properties") =>
    MergeStrategy.singleOrError
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
