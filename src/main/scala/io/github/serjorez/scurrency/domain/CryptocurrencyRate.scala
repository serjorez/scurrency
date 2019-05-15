package io.github.serjorez.scurrency.domain

import java.time.OffsetDateTime

case class CryptocurrencyRate(timestamp: OffsetDateTime,
                              rates_data: Cryptocurrency)
