package controllers.todolist

import javax.inject.{Inject, Singleton}
import models.todolist.{Account, Accounts, Task, Tasks}
import play.api.mvc.{AbstractController, ControllerComponents, Result}
import utility.Digest

/**
  * TodoListコントローラ
  */
@Singleton
class TodoListController @Inject()(tasks: Tasks)(accounts: Accounts)(cc: ControllerComponents)
    extends AbstractController(cc) {

  /**
    * インデックスページを表示
    */
  def index = Action { implicit request =>
    // 200 OK ステータスで app/views/index.scala.html をレンダリングする
    Ok(views.html.index("Welcome to Play application!"))
  }

  def Login = Action { request =>
    Ok(views.html.todolist.Login(request)).withNewSession
  }

  def Account = Action { request =>
    Ok(views.html.todolist.account(request)).withSession()
  }

  def AcCom = Action { request =>
    (for {
      param <- request.body.asFormUrlEncoded
      name  <- param.get("name").flatMap(_.headOption)
      pass  <- param.get("password").flatMap(_.headOption)
    } yield {

      var ok: Int = 0

      for (entry <- accounts.list) {
        if (entry.name == name) {
          ok = 1
        }
      }

      if (ok == 1) {
        Ok(views.html.todolist.NotAccount())
      } else {
        accounts.save(new Account(name, pass, null))
        Redirect("/todolist/account").withNewSession
      }
    }).getOrElse[Result](Redirect("/todolist"))
  }

  def AcCheck = Action { request =>
    (for {
      param <- request.body.asFormUrlEncoded
      name  <- param.get("name").flatMap(_.headOption)
      pass  <- param.get("password").flatMap(_.headOption)
    } yield {
      val hash         = Digest(pass)
      var ok: Int      = 0
      var aco: Account = null

      for (entry <- accounts.list) {
        if (entry.name == name && entry.password == hash) {
          ok = 1
          aco = entry
        }
      }
      if (ok == 1) {
        val entries = tasks.list
        Data.acdata = aco //ここでtodolistでtodoListContorllerでアカウントを格納
        Ok(views.html.todolist.list(entries)(Data.acdata)(Data.are))
      } else {
        Ok(views.html.todolist.NotLogin())
      }
    }).getOrElse[Result](Redirect("/todolist"))
  }

  def accountInfo = Action {
    Ok(views.html.todolist.accountinfo(Data.acdata))
  }
  def accountPass = Action { request =>
    Ok(views.html.todolist.accountpass(request))
  }
  def accountPassChange = Action { request =>
    (for {
      param <- request.body.asFormUrlEncoded
      pass  <- param.get("password").flatMap(_.headOption)
    } yield {
      println("123")
      accounts.update(new Account(Data.acdata.name, pass, null))
      Ok(views.html.todolist.list(tasks.list)(Data.acdata)(Data.are))
    }).getOrElse[Result](Redirect("/todolist/messages"))

  }

  def list = Action { request =>
    val entries = tasks.list
    Ok(views.html.todolist.list(entries)(Data.acdata)(Data.are))
  }

  def logout = Action { request =>
    Ok(views.html.todolist.Login(request))
  }

  def accountDelete = Action {
    accounts.delete(new Account(Data.acdata.name, Data.acdata.password, null))
    Ok(views.html.todolist.accountdelete())
  }

  def entry(id: Int) = Action {
    tasks.findByID(id) match {
      case Some(e) => Ok(views.html.todolist.entry(e))
      case None    => NotFound(s"No entry for id=${id}")
    }
  }

  def delete(id: Int) = Action {
    tasks.findByID(id) match {
      case Some(e) => {
        tasks.delete(new Task(e.id, e.title, e.description, e.isDone, e.createdAt))
        Ok(views.html.todolist.list(tasks.list)(Data.acdata)(Data.are))
      }
      case None => NotFound(s"No entry for id=${id}")
    }
  }

  def saveT(id: Int) = Action { request =>
    (for {

      param <- request.body.asFormUrlEncoded
      name  <- param.get("title").flatMap(_.headOption)
    } yield {
      print(id)
      tasks.findByID(id) match {
        case Some(e) => {
          tasks.updateT(new Task(e.id, e.title, e.description, e.isDone, e.createdAt))(name)
          Redirect("/todolist/messages").withNewSession
        }
        case None => NotFound(s"No entry for id=${id}")
      }

    }).getOrElse[Result](Redirect("/todolist/messages"))

  }

  def saveE(id: Int) = Action { request =>
    (for {
      param       <- request.body.asFormUrlEncoded
      description <- param.get("description").flatMap(_.headOption)
    } yield {
      print(id)
      tasks.findByID(id) match {
        case Some(e) => {
          tasks.updateE(new Task(e.id, e.title, e.description, e.isDone, e.createdAt))(description)
          Redirect("/todolist/messages").withNewSession
        }
        case None => NotFound(s"No entry for id=${id}")
      }

    }).getOrElse[Result](Redirect("/todolist/messages"))

  }

  def saveD(id: Int) = Action {
    tasks.findByID(id) match {
      case Some(e) => {
        tasks.updateD(new Task(e.id, e.title, e.description, e.isDone, e.createdAt))(true)
        Ok(views.html.todolist.list(tasks.list)(Data.acdata)(Data.are))
      }
      case None => NotFound(s"No entry for id=${id}")
    }
  }


  def startRegistration = Action { request =>
    Ok(views.html.todolist.nameForm(request)).withNewSession
  }

  def editMessage(id: Int) = Action { request =>
    (for {
      e <- tasks.findByID(id)
    } yield {
      Ok(views.html.todolist.entry_des(e)(request))
    }).getOrElse[Result](Redirect("/todolist/account"))
  }

  def editTitle(id: Int) = Action { request =>
    (for {
      e <- tasks.findByID(id)
    } yield {
      Ok(views.html.todolist.entry_tit(e)(request))
    }).getOrElse[Result](Redirect("/todolist/account"))
  }



  def registerName = Action { request =>
    (for {
      param <- request.body.asFormUrlEncoded
      name  <- param.get("title").flatMap(_.headOption)
    } yield {
      Ok(views.html.todolist.messageForm(request)).withSession(request.session + ("todolist::title" -> name))
    }).getOrElse[Result](Redirect("/todolist/account"))
  }


  def registerMessage = Action { request =>
    (for {
      name <- request.session.get("todolist::title")
      //gender  <- request.session.get("todolist::gender")
      param       <- request.body.asFormUrlEncoded
      description <- param.get("description").flatMap(_.headOption)
    } yield {
      val task = new Task(0, name, description, false, null)
      Ok(views.html.todolist.confirm(task, request))
        .withSession(request.session + ("todolist::description" -> description))
    }).getOrElse[Result](Redirect("/todolist/account"))
  }

  def confirm = Action { request =>
    (for {
      name <- request.session.get("todolist::title")

      message <- request.session.get("todolist::description")
    } yield {

      val sas = new Task(0, name, message, false, null)

      tasks.save(sas)
      Data.are(Data.count) = Data.acdata.name //ここでログインユーザーのみタスクを見れる設定にしてある。
      Data.count += 1


      Ok(views.html.todolist.list(tasks.list)(Data.acdata)(Data.are)).withNewSession
    }).getOrElse[Result](Redirect("/todolist/account"))
  }

}
