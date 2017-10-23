package uk.ac.bris.cs

import uk.ac.bris.cs.UndirectedGraph.Edge


class UndirectedGraph[N, E] {
	def edges(): Seq[Edge[N,E]] = ???
	def nodes(): Seq[N] = ???
	def edgesFrom(n: N): Seq[Edge[N,E]] = ???
	def edgesTo(n: N): Seq[Edge[N,E]] = ???
	def size = ???
}


object UndirectedGraph {

	case class Edge[N, E](source: N, target: N, value: E)

//	case class Node[N](value: N) extends AnyVal


}

