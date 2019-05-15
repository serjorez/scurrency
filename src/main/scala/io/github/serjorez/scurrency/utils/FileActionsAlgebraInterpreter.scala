package io.github.serjorez.scurrency.utils

import java.io.{File, PrintWriter}

import cats.implicits._
import cats.effect.{Resource, Sync}
import io.circe.{Decoder, Encoder}
import io.circe.syntax._
import io.github.serjorez.scurrency.utils.JsonUtils.String2JsonConversions

import scala.io.{BufferedSource, Source}

class FileActionsAlgebraInterpreter[F[_]: Sync, T](file: File)
                                                  (implicit encoder: Encoder[T],
                                                            decoder: Decoder[T])
  extends FileActionsAlgebra[F, T] {

  type Reader = BufferedSource
  type Writer = PrintWriter

  override def read: F[T] = reader(file).use(lines => lines.mkString.parseF)

  override def write(content: T): F[Unit] = writer(file).use(_.write(content.asJson.toString()).pure[F])

  def createFile: F[Boolean] =
    Sync[F].defer(
      if(file.exists && file.isDirectory) Sync[F].raiseError(new Exception(s"${file.getName} is a directory!"))
      else Sync[F].delay(file.createNewFile))

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

object FileActionsAlgebraInterpreter {
  def apply[F[_]: Sync, T](file: File)
                          (implicit encoder: Encoder[T], decoder: Decoder[T]): FileActionsAlgebraInterpreter[F, T] =
    new FileActionsAlgebraInterpreter(file)
}