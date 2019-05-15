package io.github.serjorez.scurrency

import io.circe.Decoder
import io.circe.generic.semiauto._
import io.circe.config.syntax._

package object config {

  val secretKey: String = sys.env("CMC_SECRET_KEY")

  implicit val jobsDec: Decoder[JobsConfig]       = deriveDecoder
  implicit val frDec: Decoder[FetchRatesConfig]   = deriveDecoder
  implicit val rcDec: Decoder[RatesCleanupConfig] = deriveDecoder
  implicit val scDec: Decoder[ScheduleConfig]     = deriveDecoder
}
