package com.gu.tip.assertion

import java.util.concurrent.Executors

import com.typesafe.scalalogging.{LazyLogging, Logger}

import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success, Try}

sealed trait AssertionResult
case object TipAssertPass extends AssertionResult
case object TipAssertFail extends AssertionResult

trait TipAssertIf {
  protected val logger: Logger

  val threadPool = Executors.newFixedThreadPool(
    2,
    (r: Runnable) =>
      new Thread(r, s"tip-assertion-pool-thread-${Random.nextInt(2)}"))
  private val assertionExecutionContext =
    ExecutionContext.fromExecutor(threadPool)

  def apply[T](
      f: => Future[T],
      p: T => Boolean,
      msg: String,
      max: Int = 1,
      delay: FiniteDuration = 0.seconds
  ): Future[AssertionResult] = {

    implicit val ec = assertionExecutionContext
    retry.Pause(max, delay)(odelay.Timer.default) {
      f.map { v =>
        Try(assert(p(v)))
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
