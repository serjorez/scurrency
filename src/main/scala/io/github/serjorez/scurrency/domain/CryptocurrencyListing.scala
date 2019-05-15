package io.github.serjorez.scurrency.domain

import cats.effect.Async
import io.circe.parser.parse
import io.circe.generic.auto._
import io.github.serjorez.scurrency.config.JsonConfig.TimestampFormat

case class CryptocurrencyListing(status: CryptocurrencyListingStatus,
                                 data: List[Cryptocurrency])

object CryptocurrencyListing {

  def parseListing[F[_]: Async](mayBeJson: String): F[CryptocurrencyListing] = {
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