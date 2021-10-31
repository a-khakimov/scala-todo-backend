import scoverage.ScoverageKeys.{coverageEnabled, coverageFailOnMinimum, coverageMinimumStmtTotal}

object Coverage {
  val Settings = Seq(
    coverageEnabled := true,
    coverageFailOnMinimum := true,
    coverageMinimumStmtTotal := 0
  )
}
