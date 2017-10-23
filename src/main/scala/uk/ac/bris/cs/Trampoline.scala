package uk.ac.bris.cs

import scala.annotation.tailrec

sealed trait Trampoline[+T] {}
object Trampoline {

	case class Now[T](t: T) extends Trampoline[T]
	case class Eval[T](f: () => Eval[T]) extends Trampoline[T]

	def evaluate[T](trampoline: Trampoline[T]) : T = {
		trampoline match {
			case Now(t)  => t
			case Eval(f) => evaluate(f())
		}
	}
}

