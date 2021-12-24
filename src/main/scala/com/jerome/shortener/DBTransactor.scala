package com.jerome.shortener

import doobie.util.transactor.Transactor
import zio._

trait DBTransactor {
  def getTransactor: TaskManaged[Transactor[Task]]
}

object DBTransactor {
  def getTransactor: RManaged[DBTransactor, Transactor[Task]] =
    ZManaged.environmentWithManaged(_.get.getTransactor)
}
