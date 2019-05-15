package io.github.serjorez.scurrency.domain

import java.time.OffsetDateTime

case class Cryptocurrency(id: Int,
                          name: String,
                          symbol: String,
                          slug: String,
                          circulating_supply: Double,
                          total_supply: Double,
                          max_supply: Option[Double],
                          date_added: OffsetDateTime,
                          num_market_pairs: Int,
                          tags: List[String],
                          platform: Option[String],
                          cmc_rank: Int,
                          last_updated: OffsetDateTime,
                          quote: Quote)
