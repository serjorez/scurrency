package io.github.serjorez.scurrency.domain

import java.sql.Timestamp

case class Cryptocurrency(id: Int,
                          name: String,
                          symbol: String,
                          slug: String,
                          circulating_supply: Long,
                          total_supply: Long,
                          max_supply: Long,
                          date_added: Timestamp,
                          num_market_pairs: Int,
                          tags: List[String],
                          platform: Option[String],
                          cmc_rank: Int,
                          last_updated: Timestamp,
                          quote: Quote)
