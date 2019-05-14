package io.github.serjorez.scurrency.config

import scala.concurrent.duration.FiniteDuration

final case class JobsConfig(fetch_rates: FetchRatesConfig, rates_cleanup: RatesCleanupConfig)
final case class FetchRatesConfig(enabled: Boolean, schedule: ScheduleConfig)
final case class RatesCleanupConfig(enabled: Boolean, schedule: ScheduleConfig)
final case class ScheduleConfig(interval: FiniteDuration)
