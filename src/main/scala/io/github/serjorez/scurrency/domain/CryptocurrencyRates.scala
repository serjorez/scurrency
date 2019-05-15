package io.github.serjorez.scurrency.domain

import cats.kernel.Semigroup

case class CryptocurrencyRates(rates: List[CryptocurrencyRate])

object CryptocurrencyRates {
  def apply(listing: CryptocurrencyListing): CryptocurrencyRates = {
    val timestamp = listing.status.timestamp
    CryptocurrencyRates(listing.data.map(data => CryptocurrencyRate(timestamp, data)))
  }

  implicit val cryptocurrencyRatesSemigroup: Semigroup[CryptocurrencyRates] =
    cats.Semigroup.instance[CryptocurrencyRates]((f, s) => CryptocurrencyRates(f.rates ++ s.rates))
}
