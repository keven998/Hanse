package core.exception

/**
 * Created by pengyt on 2015/10/22.
 */
class ErrorCode {
  var value: Int = 0
}
object ErrorCode extends Enumeration {

  /**
   * 正常
   */
  val NORMAL = Value(0)
  /**
   * 未知错误
   */
  val UNKOWN_ERROR = Value(900)
  /** K2 系统性 */
  /**
   * Http请求中输入参数错误
   */
  val INVALID_ARGUMENT = Value(100)
  /**
   * Http请求中缺少参数
   */
  val LACK_OF_ARGUMENT = Value(101)
  /**
   * 缺少相应的权限
   */
  val LACK_OF_AUTH = Value(102)
  /**
   * 通用IO错误
   */
  val IO_ERROR = Value(110)
  /**
   * 数据库错误
   */
  val DATABASE_ERROR = Value(111)
  /**
   * 搜索引擎通用错误
   */
  val SEARCH_ENGINE_ERROR = Value(103)

  /**
   * 数据不存在
   */
  val DATA_NOT_FOUND = Value(104)
}