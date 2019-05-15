package io.github.serjorez.scurrency.utils

import cats.effect.Sync
import io.circe.Decoder
import io.circe.parser.parse

object JsonUtils {

  def parseF[F[_]: Sync, T](mayBeJson: String)
                           (implicit decoder: Decoder[T]): F[T] = {
    parse(mayBeJson) match {
      case Right(json) =>
        json.as[T] match {
          case Right(listing)        => Sync[F].delay(listing)
          case Left(decodingFailure) => Sync[F].raiseError[T](
            new Exception(s"Failed to decode JSON output: ${decodingFailure.message}"))
        }
      case Left(parsingFailure) => Sync[F].raiseError[T](
        new Exception(s"Failed to parse JSON output: ${parsingFailure.message}", parsingFailure.underlying))
    }
  }
}
