package io.github.serjorez.scurrency.utils

trait FileActionsAlgebra[F[_]] {

  def read: F[String]

  def write(content: String): F[Unit]
}
