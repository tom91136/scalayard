package uk.ac.bris.cs.scotlandyard

import uk.ac.bris.cs.UndirectedGraph.Edge
import uk.ac.bris.cs.scotlandyard.ScotlandYard._

import scala.collection.breakOut

final case class StandardBoard(graph: Graph,
							   mrX: Player[Black.type],
							   detectives: Seq[Player[DetectiveColour]],
							   mrXTravelLog: MrXTravelLog,
							   pendingColours: Set[Colour]) extends Board {

	private val everyone : Seq[Player[Colour]]         = detectives :+ mrX
	private val playerMap: Map[Colour, Player[Colour]] = everyone.map { d => (d.colour, d) }(breakOut)

	override def lookup[C <: Colour](c: C): Option[Player[C]] = playerMap.get(c).map {_.asInstanceOf[Player[C]]}

	private def computePossibleMoves(player: Player[Colour]): Seq[Move] = {

		def mkMovesFrom(source: Location, p: Player[Colour]): Seq[TicketMove] = for {
			Edge(s, e, t) <- graph.edgesFrom(source)
			move <- Seq(
				TicketMove(p.colour, TicketLookup(t), s, e),
				TicketMove(p.colour, SecretTicket, s, e))
			if (move.ticket ∈: p.tickets) &&
			   detectives.forall { d => d.location != move.destination }
		} yield move

		def mkMoves(p: Player[Colour]): Seq[Move] = p match {
			// TODO seems to be a bug where not all tickets are generated
			case mrX@Player(Black, location, _)   =>
				val moves = mkMovesFrom(location, mrX)
				moves ++ (for {
					first <- moves
					second <- mkMovesFrom(first.destination, mrX)
					if (DoubleTicket ∈: mrX.tickets) &&
					   (Seq(first.ticket, second.ticket) ⊆: mrX.tickets)
				} yield DoubleMove(Black, first, second))
			case detective@Player(_, location, _) =>
				mkMovesFrom(location, detective)
		}

		mkMoves(player)
	}

	override def pendingSide: Side = if (pendingColours.contains(Black)) MrXSide else DetectiveSide


	override def computePossibleMoves(): Set[Move] = {
		pendingColours.map {playerMap(_)}.flatMap {computePossibleMoves}
	}

	override def computeWinner(): Option[Colour] = {

		def allDetectivesStuck: Boolean = detectives.flatMap {computePossibleMoves}.isEmpty

		def mrXStuck: Boolean = computePossibleMoves(mrX).isEmpty

		pendingColours.toList match {
			case Black :: Nil if mrXTravelLog.isFinalRound ||
								 allDetectivesStuck => Some(Black)
			case Black :: Nil if mrXStuck           => None // special case where MrX cannot move
			case _                                  => detectives.collectFirst {
				case Player(colour, location, _) if location == mrX.location => colour
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
						tickets = mrX.tickets - first.ticket - second.ticket),
					mrXTravelLog = mrXTravelLog.log(m),
					pendingColours = detectives.map {_.colour}.toSet
				)
			case TicketMove(colour, ticket, _, dest)  =>
				val nextPend = pendingColours - colour
				copy(
					mrX = mrX.copy(
						tickets = mrX.tickets + ticket
					),
					detectives = detectives.collect {
						case d@Player(`colour`, _, tickets) =>
							d.copy(location = dest,
								tickets = tickets - ticket)
						case d@_                            => d
					},
					pendingColours = if (nextPend.isEmpty) Set(Black) else nextPend
				)
		}
	}
	override def toString: String = s"StandardBoard(" +
									s"\nplayers=$everyone, " +
									s"\ntravelLog=$mrXTravelLog, " +
									s"\npending=$pendingColours)"
}
object StandardBoard {

	def apply(graph: Graph,
			  rounds: Seq[Visibility],
			  mrX: Player[Black.type],
			  detectives: Seq[Player[DetectiveColour]]): StandardBoard = new StandardBoard(
		graph = graph,
		mrX = mrX,
		detectives = detectives,
		mrXTravelLog = MrXTravelLog(rounds),
		pendingColours = Set(Black))

}