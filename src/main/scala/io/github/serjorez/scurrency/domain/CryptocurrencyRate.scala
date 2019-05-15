package io.github.serjorez.scurrency.domain

import java.sql.Timestamp

case class CryptocurrencyRate(timestamp: Timestamp,
                              rates_data: Cryptocurrency)
