package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SupplierRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private val supplier = TableQuery[SupplierTable]

  /**
   * The starting point for all queries on the people table.
   */

  /**
   * Create a person with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */
  def create(name: String, address: String): Future[Supplier] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (supplier.map(p => (p.name, p.address))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning supplier.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into { case ((name, address), id) => Supplier(id, name, address) }
      // And finally, insert the supplier into the database
      ) += (name, address)
  }

  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[Supplier]] = db.run {
    supplier.result
  }

  def getById(id: Long): Future[Supplier] = db.run {
    supplier.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Supplier]] = db.run {
    supplier.filter(_.id === id).result.headOption
  }

  def delete(id: Long): Future[Unit] = db.run(supplier.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, new_supplier: Supplier): Future[Unit] = {
    val supplierToUpdate: Supplier = new_supplier.copy(id)
    db.run(supplier.filter(_.id === id).update(supplierToUpdate)).map(_ => ())
  }

  private class SupplierTable(tag: Tag) extends Table[Supplier](tag, "supplier") {


    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */
    def * = (id, name, address) <> ((Supplier.apply _).tupled, Supplier.unapply)

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def name = column[String]("name")

    /** The age column */
    def address = column[String]("address")

  }

}

