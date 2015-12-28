package core.misc

import java.security.MessageDigest

import org.apache.commons.codec.binary.Hex

import scala.util.Random
import scala.xml._

/**
 * Created by topy on 2015/10/23.
 */
object Utils {

  def addChildrenToXML(n: Node, m: Map[String, String]) = n match {
    case Elem(prefix, label, attributes, scope, child @ _*) =>
      Elem(prefix, label, attributes, scope, true, child ++ m.map {
        entry =>
          val (key, value) = entry
          Elem(null, key, Null, TopScope, true, Text(value))
      }: _*)
    case _ => n
  }

  /**
   * 存放在数据库中的价格信息, 都是以分为单位的. 发送给前端的时候, 需要转换成浮点数(元为单位)
   */
  def getActualPrice(data: Int): Double = data / 100.0

  def nonceStr() = {
    val str = Random.nextInt().toString + System.currentTimeMillis / 1000
    val msg = MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8"))
    new String(Hex.encodeHex(msg))
  }

  def MD5(str: String) = {
    val bytes = MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8"))
    new String(Hex.encodeHex(bytes))
  }
}
