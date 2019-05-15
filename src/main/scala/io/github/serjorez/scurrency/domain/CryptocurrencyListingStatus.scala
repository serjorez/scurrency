package io.github.serjorez.scurrency.domain

import java.time.OffsetDateTime

case class CryptocurrencyListingStatus(timestamp: OffsetDateTime,
                                       error_code: Int,
                                       error_message: Option[String],
                                       elapsed: Int,
                                       credit_count: Int)
