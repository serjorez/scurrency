package io.github.serjorez.scurrency.domain

import java.sql.Timestamp

case class CryptocurrencyListingStatus(timestamp: Timestamp,
                                       errorCode: Int,
                                       errorMessage: Option[String],
                                       elapsed: Int,
                                       creditCount: Int)
