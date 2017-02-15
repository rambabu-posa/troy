package troy
package driver.schema

import troy.driver.{ CassandraDataType => CDT }
import shapeless._
import troy.tast._
import troy.tutils.TNone

/*
 * Represents Cql function type
 * This type-class is meant to be instantiated at the call site (might be auto-generated by a macro/plugin)
 * to give the compiler a hint about the schema
 */
trait FunctionTypeResolver[Version, F <: FunctionName[_, _], Params <: HList, ParamsKeyspace <: MaybeKeyspaceName] {
  type Out <: CDT
}

object FunctionTypeResolver {
  type Aux[Version, F <: FunctionName[_, _], Ps <: HList, PK <: MaybeKeyspaceName, O] = FunctionTypeResolver[Version, F, Ps, PK] { type Out = O }

  // Function can belong to a keyspace different from the table and params
  // For example: `select k1.f(c) from k2.t;`  t and c belong to k2, while f belong to k1
  def instance[Version, F <: FunctionName[_, _], Ps <: HList, PK <: MaybeKeyspaceName, O <: CDT]: Aux[Version, F, Ps, PK, O] =
    new FunctionTypeResolver[Version, F, Ps, PK] { type Out = O }

  def builtin0[V, FN <: String, O <: CDT] = instance[V, FunctionName[MaybeKeyspaceName, FN], HNil, MaybeKeyspaceName, O]
  def builtin1[V, FN <: String, P1 <: CDT, O <: CDT] = instance[V, FunctionName[MaybeKeyspaceName, FN], P1 :: HNil, MaybeKeyspaceName, O]

  implicit def nowFunctionType[V] = builtin0[V, "now", CDT.TimeUuid]
  implicit def writetimeFunctionType[V, P1 <: CDT] = builtin1[V, "writetime", P1, CDT.BigInt]
  implicit def dateOfFunctionType[V] = builtin1[V, "dateof", CDT.TimeUuid, CDT.Timestamp]
  // TODO
}