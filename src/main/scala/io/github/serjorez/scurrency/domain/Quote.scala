package io.github.serjorez.scurrency.domain

import java.sql.Timestamp

case class Quote(USD: QuoteUSD)

case class QuoteUSD(price: Double,
                    volume_24h: Double,
                    percent_change_1h: Double,
                    percent_change_24h: Double,
                    percent_change_7d: Double,
                    market_cap: Double,
                    last_updated: Timestamp)
