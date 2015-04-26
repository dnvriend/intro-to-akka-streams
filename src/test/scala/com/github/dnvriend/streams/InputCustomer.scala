package com.github.dnvriend.streams

import scala.util.Random

case class InputCustomer(name: String)
case class OutputCustomer(firstName: String, lastName: String)

object InputCustomer {
  def random() = InputCustomer(s"FirstName${Random.nextInt(1000)} LastName${Random.nextInt(1000)}")
}