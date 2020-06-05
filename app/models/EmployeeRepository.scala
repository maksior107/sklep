package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class EmployeeRepository @Inject() (dbConfigProvider: DatabaseConfigProvider, positionRepository: PositionRepository)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private val employee = TableQuery[EmployeeTable]

  /**
   * The starting point for all queries on the people table.
   */

  import positionRepository.PositionTable

  private val posit = TableQuery[PositionTable]

  /**
   * Create a person with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */
  def create(name: String, position: Long): Future[Employee] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (employee.map(p => (p.name, p.position))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning employee.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into { case ((name, position), id) => Employee(id, name, position) }
    // And finally, insert the employee into the database
    ) += (name, position)
  }

  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[Employee]] = db.run {
    employee.result
  }

  def getByPosition(positionID: Long): Future[Seq[Employee]] = db.run {
    employee.filter(_.position === positionID).result
  }

  def getById(id: Long): Future[Employee] = db.run {
    employee.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Employee]] = db.run {
    employee.filter(_.id === id).result.headOption
  }

  def getByCategories(positionIDs: List[Long]): Future[Seq[Employee]] = db.run {
    employee.filter(_.position inSet positionIDs).result
  }

  def delete(id: Long): Future[Unit] = db.run(employee.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newEmployee: Employee): Future[Unit] = {
    val employeeToUpdate: Employee = newEmployee.copy(id)
    db.run(employee.filter(_.id === id).update(employeeToUpdate)).map(_ => ())
  }

  private class EmployeeTable(tag: Tag) extends Table[Employee](tag, "employee") {

    def positionFk = foreignKey("posit_fk", position, posit)(_.id)

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */
    def * = (id, name, position) <> ((Employee.apply _).tupled, Employee.unapply)

    def position = column[Long]("position")

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def name = column[String]("name")

  }

}

