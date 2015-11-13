package core.api

import core.db.MorphiaFactory
import core.model.misc.Column

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/13.
 */
object MiscAPI {

  val ds = MorphiaFactory.datastore

  /**
   * 取得运营位列表
   * @param columnType 运营位类型
   * @return 运营位列表
   */
  def getColumns(columnType: String): Future[Seq[Column]] = {
    val query = ds.createQuery(classOf[Column]).field(columnType).equal(columnType)
    Future {
      if(query != null || query.isEmpty)
        query.asList().toSeq
      else
        Seq()
    }
  }
}
