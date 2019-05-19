package com.gu.tip.assertion

import com.typesafe.scalalogging.Logger
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncFlatSpec, MustMatchers}
import org.mockito.Mockito._
import scala.concurrent.Future

class TipAssertTest extends AsyncFlatSpec with MustMatchers with MockitoSugar {
  "TipAssert" should "succeed" in {
    val f = Future(1)
    TipAssert(
      f,
      (v: Int) => v == 1,
      "Duplicate DD mandates created! Fix ASAP!"
    ) map (_ must be(TipAssertPass))
  }

  it should "fail" in {
    val f = Future(3)
    TipAssert(
      f,
      (v: Int) => v == 1,
      "Duplicate DD mandates created! Fix ASAP!"
    ) map (_ must be(TipAssertFail))
  }

  it should "log error on failed assertion" in {
    val underlying = mock[org.slf4j.Logger]
    when(underlying.isErrorEnabled()).thenReturn(true)
    val loggerMock = Logger(underlying)

    object TipAssertWithMockLogger extends TipAssertIf {
      override val logger: Logger = loggerMock
    }

    val f = Future(3)
    TipAssertWithMockLogger(
      f,
      (v: Int) => v == 1,
      "User double-charged. Fix ASAP!"
    ) map { result =>
      verify(underlying).error("User double-charged. Fix ASAP!")
      result must be(TipAssertFail)
    }
  }

}
