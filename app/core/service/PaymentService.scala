package core.service

/**
 * Created by topy on 2015/10/22.
 */
object PaymentService {

  def genXML(content: Map[String, String]) = {
    val body = <xml></xml>
    //    body.map {
    //      entry =>
    //        val (key, value) = entry
    //    }
  }

  def main(args: Array[String]) {
    val nb = new xml.NodeBuffer
    val str = "str"
    val nb2 = nb &+ <li>apple</li> &+ <li>banana</li> &+ <li>cherry</li>
    System.out.print(nb2)

  }

}
