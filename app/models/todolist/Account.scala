package models.todolist

import java.sql.Timestamp

import models.DomainModel

/**
  * Domain model of Account
  * @param name         名前
  * @param password     パスワード名
  * @param createdAt
  */
case class Account(name: String, password: String, createdAt: Timestamp)

object Account extends DomainModel[Account] {
  import slick.jdbc.GetResult
  override implicit def getResult: GetResult[Account] =
    GetResult(r => Account(r.nextString, r.nextString, r.nextTimestamp))

  def apply(name: String, password: String): Account = new Account(name, password, null) //コンストラクタを呼ぶ
}
