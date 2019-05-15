package io.github.serjorez.scurrency

import java.io.File

import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import io.github.serjorez.scurrency.config.JobsConfig
import io.github.serjorez.scurrency.domain.CryptocurrencyRates
import io.github.serjorez.scurrency.jobs.{FetchRatesJob, RatesCleanupJob}
import io.github.serjorez.scurrency.utils.FileActionsAlgebraInterpreter
import io.circe.config.parser
import io.circe.generic.auto._

import scala.concurrent.duration._

object Main extends IOApp {

  implicit val sttpBackend: SttpBackend[IO, Nothing] = AsyncHttpClientCatsBackend[IO]()

  override def run(args: List[String]): IO[ExitCode] = {
    Resource.liftF(parser.decodePathF[IO, JobsConfig]("jobs")).use {
      config =>
        val fileActions   = FileActionsAlgebraInterpreter[IO, CryptocurrencyRates](
                              file = new File("rates_latest.json"))

        val fetchRatesJob = FetchRatesJob[IO](
                              fetchRatesConfig = config.fetch_rates,
                              fileActions      = fileActions,
                              uri              = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest",
                              params           = Seq(("start", "1"), ("limit", "3"), ("convert", "USD")),
                              backoffDelay     = 3 seconds,
                              maxRetries       = 1)

        val ratesCleanupJob = RatesCleanupJob[IO](
                                ratesCleanupConfig = config.rates_cleanup,
                                fileActions        = fileActions,
                                threshold          = 60 seconds)

        for {
          _ <- fileActions.createFile
          _ <- fetchRatesJob.run.start
          _ <- ratesCleanupJob.run.start
        } yield ()
    }
  }.map(_ => ExitCode.Success)
}
