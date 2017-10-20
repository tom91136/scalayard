package uk.ac.bris.cs


case class Edge[N, E](source: UndirectedGraph[N, E],
					  target: UndirectedGraph[N, E],
					  value: E)
case class UndirectedGraph[N, E](ns: Seq[(N, E, N)]) {
	def edges(): Seq[E] = ???
	def nodes(): Seq[N] = ???
	def edgesFromNode(n: N): Seq[E] = ???

}
