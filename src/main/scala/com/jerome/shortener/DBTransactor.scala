package com.jerome.shortener

import doobie.util.transactor.Transactor
import zio._

trait DBTransactor {
  def getTransactor: TaskManaged[Transactor[Task]]
}

object DBTransactor {
  def getTransactor: RManaged[Has[DBTransactor], Transactor[Task]] =
    ZManaged.accessManaged(_.get.getTransactor)
}
