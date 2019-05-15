package io.github.serjorez.scurrency.jobs

import cats._
import cats.syntax.all._
import cats.effect._
import com.softwaremill.sttp.{SttpBackend, UriContext, sttp}
import io.github.serjorez.scurrency.config.{FetchRatesConfig, secretKey}
import io.github.serjorez.scurrency.utils.FileActionsAlgebraInterpreter
import io.github.serjorez.scurrency.domain.{CryptocurrencyListing, CryptocurrencyRates}
import io.github.serjorez.scurrency.domain.CryptocurrencyRates.cryptocurrencyRatesSemigroup
import io.github.serjorez.scurrency.utils.JsonUtils.parseF
import io.circe.generic.auto._
import io.github.serjorez.scurrency.config.JsonConfig.TimestampFormat

import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration

class FetchRatesJob[F[_] : Async](fetchRatesConfig: FetchRatesConfig,
                                  fileActions: FileActionsAlgebraInterpreter[F, CryptocurrencyRates],
                                  uri: String,
                                  params: Seq[(String, String)],
                                  backoffDelay: FiniteDuration,
                                  maxRetries: Int)
                                 (implicit backend: SttpBackend[F, Nothing], timer: Timer[F])
  extends JobAlgebra[F] {

  override def run: F[Unit] = if (fetchRatesConfig.enabled) runInfinitly() else Async[F].unit

  def runInfinitly(coldStart: Boolean = true): F[Unit] = for {
    listing <- getCryptocurrencyListing
    _       <- if(coldStart) writeCryptocurrencyRates(listing) else appendCryptocurrencyRates(listing)
    _       <- timer.sleep(fetchRatesConfig.schedule.interval)
  } yield Async[F].defer(runInfinitly(false))

  def writeCryptocurrencyRates(listing: CryptocurrencyListing): F[Unit] =
    fileActions.write(CryptocurrencyRates(listing))

  def appendCryptocurrencyRates(newListing: CryptocurrencyListing): F[Unit] =
    fileActions.read.flatMap(oldRates => fileActions.write(oldRates |+| CryptocurrencyRates(newListing)))

  def getCryptocurrencyListing: F[CryptocurrencyListing] = {
    val userDataURI = uri"$uri".params(params: _*)
    val request = sttp.get(userDataURI).headers(("Accept", "application/json"), ("X-CMC_PRO_API_KEY", secretKey))

    retryWithBackoff(request.send, delay = backoffDelay, maxRetries).flatMap { response =>
      response.body match {
        case Right(rawBody)     => parseF(rawBody)
        case Left(errorMessage) =>
          Async[F].raiseError[CryptocurrencyListing](
            new Exception(s"Failed to get cryptocurrency listing. Invalid response: (${response.code}) $errorMessage"))
      }
    }
  }

  def retryWithBackoff[A, F[_] : Sync](fa: F[A],
                                       delay: FiniteDuration,
                                       maxRetries: Int)
                                      (implicit timer: Timer[F]): F[A] =
    fa.handleErrorWith { error =>
      if (maxRetries > 0) timer.sleep(delay) *> retryWithBackoff(fa, delay * 2, maxRetries - 1)
      else ApplicativeError[F, Throwable].raiseError(error)
    }
}

object FetchRatesJob {
  def apply[F[_] : Async](fetchRatesConfig: FetchRatesConfig,
                          fileActions: FileActionsAlgebraInterpreter[F, CryptocurrencyRates],
                          uri: String = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest",
                          params: Seq[(String, String)] = Seq(("start", "1"), ("limit", "3"), ("convert", "USD")),
                          backoffDelay: FiniteDuration = 3 seconds,
                          maxRetries: Int = 1)
                         (implicit backend: SttpBackend[F, Nothing], timer: Timer[F]): FetchRatesJob[F] =
    new FetchRatesJob(fetchRatesConfig, fileActions, uri, params, backoffDelay, maxRetries)
}
