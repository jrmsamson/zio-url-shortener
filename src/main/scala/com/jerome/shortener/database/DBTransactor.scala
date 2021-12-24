package com.jerome.shortener.database

import doobie.util.transactor.Transactor
import zio._

trait DBTransactor {
  def getTransactor: TaskManaged[Transactor[Task]]
}

object DBTransactor {
  def getTransactor: RManaged[DBTransactor, Transactor[Task]] =
    ZManaged.serviceWithManaged(_.getTransactor)
}
