package uk.ac.bris.cs.scotlandyard

import uk.ac.bris.cs.UndirectedGraph.Edge
import uk.ac.bris.cs.scotlandyard.ScotlandYard._

import scala.collection.breakOut

final case class TheBoard(graph: Graph,
					rounds: Seq[Visibility],
					mrX: MrX,
					detectives: Seq[Detective],
					round: Int,
					mrXTravelLog: Seq[(Int, Ticket, Location)],
					currentTurn: Colour) extends Board {

	private val everyone: Seq[Player]         = detectives :+ mrX
	private val lookup  : Map[Colour, Player] = everyone.map { d => (d.colour, d) }(breakOut)

	override def mrXRoundVisibility: Visibility = rounds(round)
	override def computePossibleMoves(): Set[Move] = {

		def mkMovesFrom(source: Location, p: Player): Seq[TicketMove] = for {
			Edge(s, e, t) <- graph.edgesFrom(source)
			move <- Seq(
				TicketMove(p.colour, TicketLookup(t), s, e),
				TicketMove(p.colour, SecretTicket, s, e))
			if p.tickets <| move.ticket &&
			   detectives.collectFirst { case Detective(_, l, _) => l == p.location }.isEmpty
		} yield move

		def mkMoves(p: Player): Seq[Move] = p match {
			case d@Detective(_, l, _) => mkMovesFrom(l, d)
			case x@MrX(l, _)          =>
				val ms = mkMovesFrom(l, x)
				for {
					first <- ms
					second <- mkMovesFrom(first.destination, x)
					if x.tickets <| first.ticket && x.tickets <| second.ticket
				} yield DoubleMove(x.colour, first, second)
		}

		(currentTurn match {
			case Black => mkMoves(mrX)
			case _     => detectives.flatMap {mkMoves}
		}).toSet
	}
	override def computeWinner(): Option[Colour] = ???
	override def progress(move: Move): TheBoard.this.type = {
		move match {
			case TicketMove(Black, ticket, origin, destination) =>
				mrX.copy(location = destination, tickets = mrX.tickets + ticket)
			case DoubleMove(Black, first, second)               =>

				mrX.copy(location = second.destination, tickets = mrX.tickets + first.ticket)


			case TicketMove(colour, ticket, origin, destination) => ???
		}

		//		copy(round = round + 1, currentTurn = move.colour)
		???
	}
}
object TheBoard {

	def apply(graph: Graph,
			  rounds: Seq[Visibility],
			  mrX: MrX,
			  detectives: Seq[Detective]): TheBoard = new TheBoard(
		graph = graph,
		rounds = rounds,
		mrX = mrX,
		detectives = detectives,
		round = 0, mrXTravelLog = Seq(),
		currentTurn = Black)

}