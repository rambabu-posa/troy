package troy
package driver.query.select

import shapeless._
import troy.driver.schema.FunctionType
import troy.driver.schema.column.ColumnType

import scala.reflect.macros.blackbox.Context

trait Selection[Version, Keyspace, Table, S] {
  type Out <: HList
}

object Selection extends LowPriority {
  import Select._

  type Aux[V, K, T, S, O] = Selection[V, K, T, S] { type Out = O }

  def apply[V, K, T, S](implicit s: Selection[V, K, T, S]): Aux[V, K, T, S, s.Out] = s

  def instance[V, K, T, S, O <: HList]: Aux[V, K, T, S, O] = new Selection[V, K, T, S] { type Out = O }

  implicit def hNilInstance[V, K, T]: Aux[V, K, T, HNil, HNil] = instance[V, K, T, HNil, HNil]

  implicit def hColumnInstance[V, K, T, ColumnName, ST <: HList](
    implicit
    headType: ColumnType[V, K, T, ColumnName],
    tailType: Selection[V, K, T, ST]
  ) = instance[V, K, T, Column[ColumnName] :: ST, headType.Out :: tailType.Out]

  implicit def hFunctionInstance[V, K, T, FKeyspace, FName, FParams <: HList, FParamTypes <: HList, ST <: HList](
    implicit
    fParamTypes: Selection.Aux[V, K, T, FParams, FParamTypes],
    headType: FunctionType[V, FKeyspace, FName, FParamTypes],
    tailType: Selection[V, K, T, ST]
  ) = instance[V, K, T, Function[FKeyspace, FName, FParams] :: ST, headType.Out :: tailType.Out]
}

trait LowPriority {
  import Select._
  import Selection.Aux

  implicit def hColumnNotFoundInstance[V, K, T, ColumnName, ST <: HList]: Aux[V, K, T, Column[ColumnName] :: ST, Nothing] = macro ImplicitNotFoundMacro.columnNotFound[V, K, T, ColumnName]
}

object ImplicitNotFoundMacro {
  def columnNotFound[V, K, T, ColumnName](c: Context) = {
    c.abort(c.enclosingPosition, "Some column doesn't exist in this query") // TODO: Extract column names from literal types
  }
}