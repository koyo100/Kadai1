package models.todolist

import javax.inject.{Inject, Singleton}
import models.Dao
import play.api.db.slick.{DatabaseConfigProvider => DBConfigProvider}
import utility.Digest

import scala.concurrent.{ExecutionContext, Future}

/**
  * task テーブルへの Accessor
  */
@Singleton
class Accounts @Inject()(dbcp: DBConfigProvider)(implicit ec: ExecutionContext) extends Dao(dbcp) {

  import profile.api._
  import utility.Await

  val table = "account"

  /**
    * DB上に保存されている全てのタスクを取得する
    *
    * @return
    */
  def save(account: Account): Int = account match {
    case Account(name, password, _) => {
      val hash = Digest(password)
      Await.result(
        db.run(sqlu"INSERT INTO #$table (name, password) VALUES ('#$name', '#$hash')")
      )
    }
  }

  def update(account: Account): Int = account match {
    case Account(name, password, _) => {
      val hash = Digest(password)
      Await.result(
        db.run(sqlu"UPDATE #$table SET password='#$hash' WHERE name = '#$name' ")
      )
    }
  }

  def delete(account: Account): Unit = {
    account match {
      case Account(name, password, _) =>
        Await.result(db.run(sqlu"DELETE FROM #$table WHERE name = '#$name'"))
    }
  }

  def list: Seq[Account] = Await.result(
    db.run(sql"SELECT name, password,  created_at FROM #$table".as[Account])
  )

  def check(account: Account): Int = account match {
    case Account(name, password, _) => {
      val hash = Digest(password)
      Await.result(
        db.run(sqlu"SELECT name, password,  created_at FROM #$table (name, password) VALUES ('#$name', '#$hash')")
      )
    }

  }

}
