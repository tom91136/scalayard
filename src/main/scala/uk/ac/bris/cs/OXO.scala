package uk.ac.bris.cs

object OXO {


	sealed trait State
	case object O extends State
	case object X extends State

	case class SquareMatrix[T](grids: IndexedSeq[IndexedSeq[T]]) extends AnyVal


}
