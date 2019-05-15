package io.github.serjorez.scurrency.algebra

import java.io.{File, PrintWriter}

import cats.implicits._
import cats.effect.{Resource, Sync}

import scala.io.{BufferedSource, Source}

class FileActionsAlgebraInterpreter[F[_]: Sync](file: File) extends FileActionsAlgebra[F] {

  type Reader = BufferedSource
  type Writer = PrintWriter

  override def read: F[String] = reader(file).use(_.mkString.pure[F])

  override def write(content: String): F[Unit] = writer(file).use(_.write(content).pure[F])

  def reader(file: File): Resource[F, Reader] =
    Resource.make {
      Sync[F].delay(Source.fromFile(file))
    } { reader =>
      Sync[F].delay(reader.close())
    }

  def writer(file: File): Resource[F, Writer] =
    Resource.make {
      Sync[F].delay(new Writer(file))
    } { writer =>
      Sync[F].delay(writer.close())
    }
}
