package com.gu.tip.assertion

import com.typesafe.scalalogging.Logger
import org.mockito.IdiomaticMockito
import org.scalatest.{AsyncFlatSpec, MustMatchers}
import scala.concurrent.Future

class TipAssertTest
    extends AsyncFlatSpec
    with MustMatchers
    with IdiomaticMockito {

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
    underlying.isErrorEnabled() shouldReturn true
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
      underlying.error("User double-charged. Fix ASAP!") was called
      result must be(TipAssertFail)
    }
  }

}
