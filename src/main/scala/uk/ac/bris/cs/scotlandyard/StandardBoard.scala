package uk.ac.bris.cs.scotlandyard

import uk.ac.bris.cs.UndirectedGraph.Edge
import uk.ac.bris.cs.scotlandyard.ScotlandYard._

import scala.collection.breakOut

final case class StandardBoard(graph: Graph,
							   mrX: MrX,
							   detectives: Seq[Detective],
							   mrXTravelLog: MrXTravelLog,
							   pendingColours: Set[Colour]) extends Board {

	private val everyone: Seq[Player]         = detectives :+ mrX
	private val lookup  : Map[Colour, Player] = everyone.map { d => (d.colour, d) }(breakOut)


	private def computePossibleMoves(player: Player): Seq[Move] = {

		def mkMovesFrom(source: Location, p: Player): Seq[TicketMove] = for {
			Edge(s, e, t) <- graph.edgesFrom(source)
			move <- Seq(
				TicketMove(p.colour, TicketLookup(t), s, e),
				TicketMove(p.colour, SecretTicket, s, e))
			if (move.ticket ∈: p.tickets) &&
			   detectives.forall { d => d.location != move.destination }
		} yield move

		def mkMoves(p: Player): Seq[Move] = p match {
			case detective@Detective(_, location, _) =>
				val x = mkMovesFrom(location, detective)
				x
			case mrX@MrX(location, _)                =>
				for {
					first <- mkMovesFrom(location, mrX)
					second <- mkMovesFrom(first.destination, mrX)
					if (first.ticket ∈: mrX.tickets) && (second.ticket ∈: mrX.tickets)
				} yield DoubleMove(mrX.colour, first, second)
		}

		mkMoves(player)
	}

	override def pendingSide: Side = if (pendingColours.contains(Black)) MrXSide else DetectiveSide

	override def computePossibleMoves(): Set[Move] = {
		pendingColours.map {lookup}.flatMap {computePossibleMoves}
	}

	override def computeWinner(): Option[Colour] = {

		def allDetectivesStuck: Boolean = detectives.flatMap {computePossibleMoves}.isEmpty

		def mrXStuck: Boolean = computePossibleMoves(mrX).isEmpty

		pendingColours.toList match {
			case Black :: Nil if mrXTravelLog.isFinalRound ||
								 allDetectivesStuck => Some(Black)
			case Black :: Nil if mrXStuck           => None // special case where MrX cannot move
			case _                                  => detectives.collectFirst {
				case Detective(colour, location, _) if location == mrX.location => colour
			}
		}
	}

	override def progress(move: Move): StandardBoard = {
		move match {
			case m@TicketMove(Black, ticket, _, dest) =>
				copy(
					mrX = mrX.copy(location = dest,
						tickets = mrX.tickets - ticket),
					mrXTravelLog = mrXTravelLog.log(m),
					pendingColours = detectives.map {_.colour}.toSet
				)
			case m@DoubleMove(Black, first, second)   =>
				copy(
					mrX = mrX.copy(
						location = second.destination,
						tickets = mrX.tickets - first.ticket),
					mrXTravelLog = mrXTravelLog.log(m),
					pendingColours = detectives.map {_.colour}.toSet
				)

			case TicketMove(colour, ticket, _, dest) =>
				val nextPend = pendingColours - colour
				copy(
					mrX = mrX.copy(
						tickets = mrX.tickets + ticket
					),
					detectives = detectives.collect {
						case d@Detective(`colour`, _, tickets) =>
							d.copy(location = dest,
								tickets = tickets - ticket)
						case d@_                               => d
					},
					pendingColours = if (nextPend.isEmpty) Set(Black) else nextPend
				)
		}
	}
	override def toString = s"StandardBoard(" +
							s"\nplayers=$everyone, " +
							s"\ntravelLog=$mrXTravelLog, " +
							s"\npending=$pendingColours)"
}
object StandardBoard {

	def apply(graph: Graph,
			  rounds: Seq[Visibility],
			  mrX: MrX,
			  detectives: Seq[Detective]): StandardBoard = new StandardBoard(
		graph = graph,
		mrX = mrX,
		detectives = detectives,
		mrXTravelLog = MrXTravelLog(rounds),
		pendingColours = Set(Black))

}