package models.todolist

import javax.inject.{Inject, Singleton}
import models.Dao
import play.api.db.slick.{DatabaseConfigProvider => DBConfigProvider}

import scala.concurrent.ExecutionContext

/**
  * task テーブルへの Accessor
  */
@Singleton
class Tasks @Inject()(dbcp: DBConfigProvider)(implicit ec: ExecutionContext) extends Dao(dbcp) {

  import profile.api._
  import utility.Await

  val table = "task"

  /**
    * DB上に保存されている全てのタスクを取得する
    * @return
    */
  def list: Seq[Task] = Await.result(
    db.run(sql"SELECT id, title, description, is_done, created_at FROM #$table".as[Task])
  )

  def findByID(id: Int): Option[Task] =
    Await.result( //db.run(sql文を書いてる)//A
      db.run(sql"SELECT id, title, description, is_done, created_at FROM #$table WHERE id=#$id".as[Task].headOption)
    )

  def save(task: Task): Int = task match {
    case Task(0, title, description, is_done, _) =>
      Await.result(
        db.run(
          sqlu"INSERT INTO #$table (title, description, is_done) VALUES ('#$title', '#$description', '#$is_done')"
        )
      )
    case Task(id, title, description, is_done, _) =>
      Await.result(
        db.run(sqlu"UPDATE #$table SET title=#$title, description=#$description, is_done=#$is_done WHERE id = #$id")
      )
  }

  def updateE(task: Task)(des: String): Int = task match {
    case Task(0, title, description, is_done, _) =>
      Await.result(
        db.run(sqlu"INSERT INTO #$table (title, description, is_done) VALUES ('#$title', '#$description', '#$is_done')")
      )
    case Task(id, title, description, is_done, _) =>
      Await.result(
        db.run(sqlu"UPDATE #$table SET  title='#$title', description='#$des', is_done='#$is_done' WHERE id = #$id")
      )
  }

  def updateT(task: Task)(tit: String): Int = task match {
    case Task(0, title, description, is_done, _) =>
      Await.result(
        db.run(sqlu"INSERT INTO #$table (title, description, is_done) VALUES ('#$title', '#$description', '#$is_done')")
      )
    case Task(id, title, description, is_done, _) =>
      Await.result(
        db.run(
          sqlu"UPDATE #$table SET  title='#$tit', description='#$description', is_done='#$is_done' WHERE id = #$id"
        )
      )
  }

  def updateD(task: Task)(isd: Boolean): Int = task match {
    case Task(0, title, description, is_done, _) =>
      Await.result(
        db.run(sqlu"INSERT INTO #$table (title, description, is_done) VALUES ('#$title', '#$description', '#$is_done')")
      )
    case Task(id, title, description, is_done, _) =>
      Await.result(
        db.run(
          sqlu"UPDATE #$table SET  title='#$title', description='#$description', is_done='#$isd' WHERE id = #$id"
        )
      )
  }

  def delete(task: Task): Unit = {
    task match {
      case Task(id, title, description, is_done, _) =>
        Await.result(db.run(sqlu"DELETE FROM #$table WHERE id =  #$id"))
    }
  }

}
