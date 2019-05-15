package io.github.serjorez.scurrency.algebra

trait FileActionsAlgebra[F[_]] {

  def read: F[String]

  def write(content: String): F[Unit]
}
