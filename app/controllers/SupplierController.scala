package controllers

import javax.inject._
import models.{Supplier, SupplierRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class SupplierController @Inject()(suppliersRepo: SupplierRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val supplierForm: Form[CreateSupplierForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "address" -> nonEmptyText,
    )(CreateSupplierForm.apply)(CreateSupplierForm.unapply)
  }

  val updateSupplierForm: Form[UpdateSupplierForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "address" -> nonEmptyText,
    )(UpdateSupplierForm.apply)(UpdateSupplierForm.unapply)
  }

  def getSuppliers: Action[AnyContent] = Action.async { implicit request =>
    val suppliers = suppliersRepo.list()
    suppliers.map(suppliers => Ok(views.html.suppliers(suppliers)))
  }

  def getSupplier(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val supplier = suppliersRepo.getByIdOption(id)
    supplier.map {
      case Some(p) => Ok(views.html.supplier(p))
      case None => Redirect(routes.SupplierController.getSuppliers())
    }
  }

  def delete(id: Long): Action[AnyContent] = Action {
    suppliersRepo.delete(id)
    Redirect("/suppliers")
  }

  def updateSupplier(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val supplier = suppliersRepo.getById(id)
    supplier.map(supplier => {
      val prodForm = updateSupplierForm.fill(UpdateSupplierForm(supplier.id, supplier.name, supplier.address))
      //  id, supplier.name, supplier.description, supplier.category)
      //updateSupplierForm.fill(prodForm)
      Ok(views.html.supplierupdate(prodForm))
    })
  }

  def updateSupplierHandle(): Action[AnyContent] = Action.async { implicit request =>
    updateSupplierForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.supplierupdate(errorForm))
        )
      },
      supplier => {
        suppliersRepo.update(supplier.id, Supplier(supplier.id, supplier.name, supplier.address)).map { _ =>
          Redirect(routes.SupplierController.updateSupplier(supplier.id)).flashing("success" -> "supplier updated")
        }
      }
    )
  }

  def addSupplier(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.supplieradd(supplierForm))
  }

  def addSupplierHandle(): Action[AnyContent] = Action.async { implicit request =>
    supplierForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.supplieradd(errorForm))
        )
      },
      supplier => {
        suppliersRepo.create(supplier.name, supplier.address).map { _ =>
          Redirect(routes.SupplierController.addSupplier()).flashing("success" -> "supplier.created")
        }
      }
    )

  }

  // AAAAJSONAAAA
  //  def getSuppliers(): Action[AnyContent] = Action.async { implicit request =>
  //    val suppliers = suppliersRepo.list();
  //    suppliers.map(suppliers => Ok(Json.toJson(suppliers)))
  //  }
  //
  //  def addSupplier: Action[AnyContent] = Action { implicit request =>
  //    var supplier: Supplier = request.body.asJson.get.as[Supplier]
  //    suppliersRepo.create(supplier.name, supplier.description, supplier.category)
  //    Ok(request.body.asJson)
  //  }

  /*
    def addSupplier = Action { implicit request: MessagesRequest[AnyContent] =>

      var categ:Seq[Category] = Seq[Category]()
      val categories = categoryRepo.list().onComplete{
        case Success(cat) => categ = cat
        case Failure(_) => print("fail")
      }

      val errorFunction = { supplierForm =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.supplieradd(supplierForm, categ))
    }

    val successFunction = { data: Supplier =>
      // This is the good case, where the form was successfully parsed as a Data object.
      suppliersRepo.create(data.name, data.description, data.category).map { _ =>
        Redirect(routes.HomeController.addSupplier()).flashing("success" -> "supplier.created")
      }
    }

    val formValidationResult = supplierForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
  */
}

case class CreateSupplierForm(name: String, address: String)

case class UpdateSupplierForm(id: Long, name: String, address: String)
