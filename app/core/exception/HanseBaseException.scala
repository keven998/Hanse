package core.exception

/**
 * Hanse的基础异常类
 *
 * Created by pengyt on 2015/10/23.
 */
abstract class HanseBaseException(message: String, cause: Throwable) extends RuntimeException(message, cause)
