package uk.ac.bris.cs

import uk.ac.bris.cs.RichMap._
import uk.ac.bris.cs.UndirectedGraph.Edge


case class UndirectedGraph[N, E](
									nodes: Seq[N],
									edges: Seq[Edge[N, E]],
									sourceEdges: Map[N, Seq[Edge[N, E]]],
									destinationEdges: Map[N, Seq[Edge[N, E]]]
								) {


	def edgesFrom(n: N): Seq[Edge[N, E]] = sourceEdges(n)
	def edgesTo(n: N): Seq[Edge[N, E]] = destinationEdges(n)
	val size: Int = nodes.size

	def +(n: N): UndirectedGraph[N, E] = {
		// TODO bad bad bad
		if (nodes.contains(n)) throw new IllegalArgumentException("node already exists")
		copy(nodes = n +: nodes,
			sourceEdges = sourceEdges + (n -> Vector()),
			destinationEdges = destinationEdges + (n -> Vector()))
	}

	private def appendDirected(e: Edge[N, E]) = copy(
		sourceEdges = sourceEdges.adjust(e.source) {_ :+ e},
		destinationEdges = sourceEdges.adjust(e.target) {_ :+ e},
		edges = edges :+ e
	)

	def +(edge: Edge[N, E]): UndirectedGraph[N, E] = {
		val Edge(source, target, _) = edge
		// TODO bad bad bad
		if (!nodes.contains(source) || !nodes.contains(target))
			throw new IllegalArgumentException("target or node not in graph")
		appendDirected(edge).appendDirected(edge.swapped())
	}

}


object UndirectedGraph {


	def apply[N, E](): UndirectedGraph[N, E] =
		new UndirectedGraph[N, E](Vector(), Vector(), Map(), Map())

	case class Edge[N, E](source: N, target: N, value: E) {
		def swapped(): Edge[N, E] = Edge(target, source, value)
	}


}

