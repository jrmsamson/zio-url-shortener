package com.jerome.shortener

import doobie.util.transactor.Transactor
import zio.{Has, Task}

package object db {
  type DBTransactor = Has[Transactor[Task]]
}
