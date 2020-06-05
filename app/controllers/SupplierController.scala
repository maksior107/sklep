package controllers

import forms.{ CreateSupplierForm, UpdateSupplierForm }
import javax.inject._
import models.{ Supplier, SupplierRepository }
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class SupplierController @Inject() (suppliersRepo: SupplierRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val supplierForm: Form[CreateSupplierForm] = CreateSupplierForm.form

  val updateSupplierForm: Form[UpdateSupplierForm] = UpdateSupplierForm.form

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
    Redirect("/supplier")
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
  //  def getSuppliers: Action[AnyContent] = Action.async { implicit request =>
  //    val suppliers = suppliersRepo.list();
  //    suppliers.map(suppliers => Ok(Json.toJson(suppliers)))
  //  }
  //
  //  def addSupplier(): Action[AnyContent] = Action { implicit request =>
  //    var supplier: Supplier = request.body.asJson.get.as[Supplier]
  //    suppliersRepo.create(supplier.name, supplier.address)
  //    Ok(request.body.asJson)
  //  }
}