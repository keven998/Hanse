package core.misc

import com.twitter.{ util => twitter }
import core.db.MorphiaFactory

import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.language.implicitConversions
import scala.util.{ Failure, Success, Try }
import scala.xml.{ Elem, NodeSeq }

/**
 * Created by zephyre on 7/10/15.
 */
object Implicits {

  implicit lazy val ds = MorphiaFactory.datastore

  implicit def long2String(v: Long) = {
    v.toString
  }

  implicit def int2String(v: Int) = {
    v.toString
  }

  implicit def float2String(v: Float) = {
    v.toString
  }

  implicit class ChildSelectable(ns: NodeSeq) {
    def \* = ns flatMap {
      _ match {
        case e: Elem => e.child
        case _ => NodeSeq.Empty
      }
    }
  }

  object TwitterConverter {
    implicit def scalaToTwitterTry[T](t: Try[T]): twitter.Try[T] = t match {
      case Success(r) => twitter.Return(r)
      case Failure(ex) => twitter.Throw(ex)
    }

    implicit def twitterToScalaTry[T](t: twitter.Try[T]): Try[T] = t match {
      case twitter.Return(r) => Success(r)
      case twitter.Throw(ex) => Failure(ex)
    }

    implicit def scalaToTwitterFuture[T](f: Future[T])(implicit ec: ExecutionContext): twitter.Future[T] = {
      val promise = twitter.Promise[T]()
      f.onComplete(promise update _)
      promise
    }

    implicit def twitterToScalaFuture[T](f: twitter.Future[T]): Future[T] = {
      val promise = Promise[T]()
      f.respond(promise complete _)
      promise.future
    }
  }

}
