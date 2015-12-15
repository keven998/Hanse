package core.misc

import com.lvxingpai.model.misc.PhoneNumber
import com.twitter.{ util => twitter }
import org.mongodb.morphia.annotations.Entity
import play.api.libs.json.Json

import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.language.implicitConversions
import scala.util.{ Failure, Success, Try }
import scala.xml.{ Elem, Node, NodeSeq }

/**
 * Created by zephyre on 7/10/15.
 */
object Implicits {

  implicit def long2String(v: Long): String = v.toString

  implicit def int2String(v: Int): String = v.toString

  implicit def float2String(v: Float): String = v.toString

  implicit def Node2String(body: Node): String = {
    body match {
      case NodeSeq.Empty => ""
      case _ => body.toString()
    }
  }

  @Entity(noClassnameStored = true)
  case class PhoneNumberTemp(dialCode: Int, number: Long) {
    def toPhoneNumber = {
      val ret = new PhoneNumber
      ret.dialCode = dialCode
      ret.number = number
      ret
    }
  }

  implicit val phoneNumberReads = Json.reads[PhoneNumberTemp]

  implicit def phoneNumberTemp2Model(pt: PhoneNumberTemp): PhoneNumber = {
    val ret: PhoneNumber = new PhoneNumber()
    ret.dialCode = pt.dialCode
    ret.number = pt.number
    ret
  }

  implicit def NodeSeq2String(body: NodeSeq): String = {
    body match {
      case NodeSeq.Empty => ""
      case _ => body.toString()
    }
  }

  implicit def NodeSeq2Int(body: NodeSeq): Int = {
    body match {
      case NodeSeq.Empty => 0
      case _ => body.toString().toInt
    }
  }

  implicit class ElemChild(ns: NodeSeq) {
    def \* = ns flatMap {
      case e: Elem => e.child
      case _ => NodeSeq.Empty
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
