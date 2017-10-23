package uk.ac.bris.cs.scotlandyard

import uk.ac.bris.cs.UndirectedGraph

object ScotlandYard  {

	sealed trait Colour
	final case object Black extends Colour
	final case object Red extends Colour
	final case object Green extends Colour
	final case object Blue extends Colour
	final case object Yellow extends Colour
	final case object White extends Colour

	sealed trait Transport
	final case object Taxi extends Transport
	final case object Bus extends Transport
	final case object Underground extends Transport
	final case object Boat extends Transport

	final val TicketLookup: PartialFunction[Transport, Ticket] = {
		case Taxi        => TaxiTicket
		case Bus         => BusTicket
		case Underground => UndergroundTicket
		case Boat        => SecretTicket
	}

	sealed trait Ticket
	final case object TaxiTicket extends Ticket
	final case object BusTicket extends Ticket
	final case object UndergroundTicket extends Ticket
	final case object SecretTicket extends Ticket
	final case object DoubleTicket extends Ticket

	sealed trait Visibility
	final case object Shown extends Visibility
	final case object Hidden extends Visibility

	final case class Amount(value: Int) extends AnyVal {
		def ++ : Amount = Amount(value + 1)
		def -- : Amount = Amount(if (value == 0) 0 else value - 1)
	}
	object Amount {
		final val Zero: Amount = Amount(0)
	}

	final case class Tickets(map: Map[Ticket, Amount]) extends AnyVal {
		def +(t: Ticket): Tickets = Tickets(map + (t -> (map.getOrElse(t, Amount.Zero) ++)))
		def -(t: Ticket): Tickets = Tickets(map + (t -> (map.getOrElse(t, Amount.Zero) --)))
		def <|(t: Ticket): Boolean = map.contains(t)
		def isEmpty: Boolean = map.isEmpty
	}

	final case class Location(value: Int) extends AnyVal
	type Graph = UndirectedGraph[Location, Transport]

	sealed trait Player {
		def location: Location
		def tickets: Tickets
		def colour: Colour
	}
	final case class MrX(location: Location, tickets: Tickets) extends Player {
		val colour: Black.type = Black
	}
	final case class Detective(colour: Colour, location: Location, tickets: Tickets) extends Player

	sealed trait Move {def colour: Colour}
	sealed trait MrXMove extends Move
	sealed trait DetectiveMove extends Move
	final case class TicketMove(colour: Colour,
								ticket: Ticket,
								origin: Location,
								destination: Location) extends DetectiveMove with MrXMove

	final case class DoubleMove(colour: Black.type,
								first: TicketMove,
								second: TicketMove) extends MrXMove

	def mkDefaultRounds(): Seq[Visibility] = Seq()

	def mkDefaultDetectiveTickets(): Tickets = Tickets(Map(
		TaxiTicket -> Amount(1),
		BusTicket -> Amount(1),
		UndergroundTicket -> Amount(1),
		SecretTicket -> Amount(1),
		DoubleTicket -> Amount(1),
	))

	def mkDefaultMrXTickets(): Tickets = Tickets(Map(
		TaxiTicket -> Amount(1),
		BusTicket -> Amount(1),
		UndergroundTicket -> Amount(1),
		SecretTicket -> Amount(1),
		DoubleTicket -> Amount(1),
	))

	trait Board {
		def graph: Graph
		def round: Int
		def rounds: Seq[Visibility]
		def mrX: MrX
		def detectives: Seq[Detective]
		def currentTurn: Colour
		def mrXRoundVisibility: Visibility
		def mrXTravelLog : Seq[(Int, Ticket, Location)]
		def computePossibleMoves(): Set[Move]
		def computeWinner(): Option[Colour]
		def progress(move: Move): this.type
	}

	sealed trait Round
	sealed trait DetectiveRound extends Round
	sealed trait MrXRound extends Round
	final case class DetectiveSelect(board: Board, next: (DetectiveMove) => Round) extends DetectiveRound
	final case class MrXSelect(board: Board, next: (MrXMove) => DetectiveRound) extends MrXRound
	final case class MrXVictory(board: Board) extends MrXRound with DetectiveRound
	final case class DetectiveVictory(board: Board) extends MrXRound with DetectiveRound


	def startGame(initialBoard: Board): MrXRound = {

		def won(board: Board): Option[MrXRound with DetectiveRound] = {
			board.computeWinner().collect {
				case Black => MrXVictory(board)
				case _     => DetectiveVictory(board)
			}
		}

		def progressDetective(board: Board, move: DetectiveMove): Round = {
			val next = board.progress(move)
			won(next).getOrElse {
				next.currentTurn match {
					case Black => MrXSelect(board, progressMrX(board, _))
					case _     => DetectiveSelect(board, progressDetective(board, _))
				}
			}
		}

		def progressMrX(board: Board, move: MrXMove): DetectiveRound = {
			val next = board.progress(move)
			won(next).getOrElse(DetectiveSelect(next, progressDetective(next, _)))
		}

		won(initialBoard).getOrElse(MrXSelect(initialBoard, progressMrX(initialBoard, _)))
	}

}

