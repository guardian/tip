package com.gu.tip

import cats.Semigroup

case class Log(level: String, message: String)

//object Log {
//  implicit val logSemigroup: Semigroup[Log] = new Semigroup[Log] {
//    def combine(x: Log, y: Log): Log = Log(x.level + "," + y.level, x.message + "," + y.message)
//  }
//}


