package com.gu.tip.assertion

import java.util.concurrent.Executors

import com.typesafe.scalalogging.{LazyLogging, Logger}
import retry.Defaults

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Random, Success, Try}

object ExecutionContext {
  private val threadPool = Executors.newFixedThreadPool(
    2,
    (r: Runnable) =>
      new Thread(r, s"tip-assertion-pool-thread-${Random.nextInt(2)}")
  )
  val assertionExecutionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.fromExecutor(threadPool)
}

sealed trait AssertionResult
case object TipAssertPass extends AssertionResult
case object TipAssertFail extends AssertionResult

trait TipAssertIf {
  protected val logger: Logger

  def apply[T](
      state: => Future[T],
      predicate: T => Boolean,
      msg: String,
      max: Int = 1,
      delay: FiniteDuration = Defaults.delay
  ): Future[AssertionResult] = {

    implicit val ec = ExecutionContext.assertionExecutionContext
    retry.Pause(max, delay)(odelay.Timer.default) {
      state.map { actualState =>
        Try(assert(predicate(actualState)))
      }
    } map {
      case Failure(_) =>
        logger.error(msg)
        TipAssertFail

      case Success(_) =>
        TipAssertPass
    }

  }

}

object TipAssert extends TipAssertIf with LazyLogging
