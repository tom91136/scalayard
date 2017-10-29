package uk.ac.bris.cs

sealed trait Trampoline[+T] {}
object Trampoline {

	case class Now[+T](t: T) extends Trampoline[T]
	case class Eval[+T](f: () => Eval[T]) extends Trampoline[T]
	object Eval {
		def ||>[T](f: => Eval[T]): Eval[T] = new Eval({ () => f })
	}

	def evaluate[T](trampoline: Trampoline[T]): T = {
		trampoline match {
			case Now(t)  => t
			case Eval(f) => evaluate(f())
		}
	}
}

