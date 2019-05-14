package io.github.serjorez.scurrency.domain

case class CryptocurrencyListing(status: CryptocurrencyListingStatus,
                                 data: List[Cryptocurrency])
