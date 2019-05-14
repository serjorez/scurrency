package io.github.serjorez.scurrency

import io.circe.Decoder
import io.circe.generic.semiauto._

package object config {
  implicit val jobsDec: Decoder[JobsConfig] = deriveDecoder
  implicit val frDec: Decoder[FetchRatesConfig] = deriveDecoder
  implicit val rcDec: Decoder[RatesCleanupConfig] = deriveDecoder
  implicit val scDec: Decoder[ScheduleConfig] = deriveDecoder
}
