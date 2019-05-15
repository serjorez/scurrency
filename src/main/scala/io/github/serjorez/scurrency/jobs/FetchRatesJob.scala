package io.github.serjorez.scurrency.jobs

import cats._
import cats.syntax.all._
import cats.effect._
import com.softwaremill.sttp.{SttpBackend, UriContext, sttp}
import io.circe.parser.parse
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration
import io.github.serjorez.scurrency.config.{FetchRatesConfig, secretKey}
import io.github.serjorez.scurrency.algebra.FileActionsAlgebraInterpreter
import io.github.serjorez.scurrency.domain.CryptocurrencyListing
import io.github.serjorez.scurrency.config.JsonConfig.TimestampFormat

class FetchRatesJob[F[_] : Async](fetchRatesConfig: FetchRatesConfig,
                                  fileActions: FileActionsAlgebraInterpreter[F],
                                  uri: String,
                                  params: Seq[(String, String)],
                                  backoffDelay: FiniteDuration = 3 seconds,
                                  maxRetries: Int = 1)
                                 (implicit backend: SttpBackend[F, Nothing], timer: Timer[F])
  extends JobAlgebra[F] {

  override def run: F[Unit] = if (fetchRatesConfig.enabled) {
    for {
      listing <- getCryptocurrencyListing
      _ <- appendListing(listing)
      _ <- timer.sleep(fetchRatesConfig.schedule.interval)
      _ <- Async[F].defer(run)
    } yield ()
  } else Async[F].unit

  def appendListing(listing: CryptocurrencyListing): F[Unit] = for {
    mayBeJson <- fileActions.read
    listing   <- parseListing(mayBeJson)
    result    <- fileActions.write(listing.asJson.toString)
  } yield result

  def getCryptocurrencyListing: F[CryptocurrencyListing] = {
    val userDataURI = uri"$uri".params(params: _*)
    val request = sttp.get(userDataURI).headers(("Accept", "application/json"), ("X-CMC_PRO_API_KEY", secretKey))

    retryWithBackoff(request.send, delay = backoffDelay, maxRetries).flatMap { response =>
      response.body match {
        case Right(rawBody)     => parseListing(rawBody)
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

  def parseListing(mayBeJson: String): F[CryptocurrencyListing] = {
    parse(mayBeJson) match {
      case Right(json) =>
        json.as[CryptocurrencyListing] match {
          case Right(listing)        => Async[F].delay(listing)
          case Left(decodingFailure) => Async[F].raiseError[CryptocurrencyListing](
            new Exception(s"Failed to decode JSON output: ${decodingFailure.message}"))
        }
      case Left(parsingFailure) => Async[F].raiseError[CryptocurrencyListing](
        new Exception(s"Failed to parse JSON output: ${parsingFailure.message}", parsingFailure.underlying))
    }
  }
}

object FetchRatesJob {
  def apply[F[_] : Async](fetchRatesConfig: FetchRatesConfig,
                          fileActions: FileActionsAlgebraInterpreter[F],
                          uri: String = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest",
                          params: Seq[(String, String)] = Seq(("start", "1"), ("limit", "3"), ("convert", "USD")),
                          backoffDelay: FiniteDuration = 3 seconds,
                          maxRetries: Int = 1)
                         (implicit backend: SttpBackend[F, Nothing], timer: Timer[F]): FetchRatesJob[F] =
    new FetchRatesJob(fetchRatesConfig, fileActions, uri, params, backoffDelay, maxRetries)
}
