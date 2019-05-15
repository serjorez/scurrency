package io.github.serjorez.scurrency.utils

trait FileActionsAlgebra[F[_], T] {

  def read: F[T]

  def write(entity: T): F[Unit]
}
