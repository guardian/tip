package example

import com.gu.tip.{TipConfig, TipFactory}

object Hello extends App {
  val tipConfig = TipConfig(
    repo = "mario-galic/sandbox",
    cloudEnabled = false
  )
  val tip = TipFactory.create(tipConfig)
  tip.verify("Register")
  tip.verify("Update User")
}

