package com.gu.tip

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class TipTest extends FlatSpec {
  "Tip" should "swallow all its exceptions" in {
    assert(Tip.verify("bad name").isSuccess)
  }
}