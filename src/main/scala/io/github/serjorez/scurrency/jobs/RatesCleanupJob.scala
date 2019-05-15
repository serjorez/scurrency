package io.github.serjorez.scurrency.jobs

import java.util.concurrent.TimeUnit

import cats.syntax.all._
import cats.effect._
import io.github.serjorez.scurrency.config.RatesCleanupConfig
import io.github.serjorez.scurrency.domain.CryptocurrencyRates
import io.github.serjorez.scurrency.utils.FileActionsAlgebraInterpreter

import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration

class RatesCleanupJob[F[_] : Sync](fetchRatesConfig: RatesCleanupConfig,
                                   fileActions: FileActionsAlgebraInterpreter[F, CryptocurrencyRates],
                                   threshold: FiniteDuration)
                                  (implicit timer: Timer[F])
  extends JobAlgebra[F] {

  override def run: F[Unit] = if (fetchRatesConfig.enabled) runInfinitly else Sync[F].unit

  def runInfinitly: F[Unit] = for {
    _ <- cleanCryptocurrencyRates
    _ <- timer.sleep(fetchRatesConfig.schedule.interval)
    _ <- Sync[F].defer(runInfinitly)
  } yield ()

  def cleanCryptocurrencyRates: F[Unit] = for {
    oldRates    <- fileActions.read
    currentTime <- timer.clock.realTime(TimeUnit.MILLISECONDS)
    newRates    =  oldRates.rates.filter(rate => isUnderThreshold(currentTime, rate.timestamp.toInstant.toEpochMilli))
    _           <- fileActions.write(CryptocurrencyRates(newRates))
  } yield ()

  def isUnderThreshold(currentTime: Long, oldTime: Long): Boolean =
    threshold.toSeconds > (currentTime - oldTime) / 1000
}

object RatesCleanupJob {
  def apply[F[_] : Sync](fetchRatesConfig: RatesCleanupConfig,
                         fileActions: FileActionsAlgebraInterpreter[F, CryptocurrencyRates],
                         threshold: FiniteDuration = 60 seconds)
                        (implicit timer: Timer[F]): RatesCleanupJob[F] =
    new RatesCleanupJob(fetchRatesConfig, fileActions, threshold)
}
