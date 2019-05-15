package io.github.serjorez.scurrency

import java.io.File

import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import io.circe.config.parser
import io.github.serjorez.scurrency.config.JobsConfig
import io.github.serjorez.scurrency.domain.CryptocurrencyRates
import io.github.serjorez.scurrency.jobs.{FetchRatesJob, RatesCleanupJob}
import io.github.serjorez.scurrency.utils.FileActionsAlgebraInterpreter
import io.circe.generic.auto._

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    Resource.liftF(parser.decodePathF[IO, JobsConfig]("jobs")).use {
      config =>

        implicit val sttpBackend: SttpBackend[IO, Nothing] = AsyncHttpClientCatsBackend[IO]()
        val fileActions = FileActionsAlgebraInterpreter[IO, CryptocurrencyRates](new File("rates_latest.json"))
        val fetchRatesJob = FetchRatesJob[IO](config.fetch_rates, fileActions)
        val ratesCleanupJob = RatesCleanupJob[IO](config.rates_cleanup, fileActions)

        for {
          _ <- fileActions.createFile
          _ <- fetchRatesJob.run.start
          _ <- ratesCleanupJob.run.start
        } yield ()
    }
  }.map(_ => ExitCode.Success)
}
