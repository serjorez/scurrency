package io.github.serjorez.scurrency.jobs

trait JobAlgebra[F[_]] {
  def run: F[Unit]
}
