package core.misc

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

}
