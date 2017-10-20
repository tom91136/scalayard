package uk.ac.bris.cs

import scala.collection.immutable
import scala.collection.immutable.SortedSet

object ScotlandYard extends App {

	sealed trait Colour
	case object Black extends Colour
	case object Red extends Colour
	case object Green extends Colour
	case object Blue extends Colour
	case object Yellow extends Colour
	case object White extends Colour

	sealed trait Transport
	case object Taxi extends Transport
	case object Bus extends Transport
	case object Underground extends Transport
	case object Boat extends Transport

	sealed trait Ticket
	case object TaxiTicket extends Ticket
	case object BusTicket extends Ticket
	case object UndergroundTicket extends Ticket
	case object SecretTicket extends Ticket
	case object DoubleTicket extends Ticket

	sealed trait Visibility
	case object Shown extends Visibility
	case object Hidden extends Visibility

	case class Amount(value: Int) extends AnyVal {
		def ++(): Amount = Amount(value + 1)
		def --(): Amount = Amount(if (value == 0) 0 else value - 1)
	}

	case class Location(value: Int) extends AnyVal
	type Graph = UndirectedGraph[Location, Transport]
	type Tickets = Map[Ticket, Amount]


	sealed trait Player {
		type M <: Move
		def location: Location
		def tickets: Tickets
		def colour: Colour
		def moveTo(move: M): this.type
	}
	case class MrX(location: Location, tickets: Tickets) extends Player {
		val colour: Black.type = Black
		override type M = MrXMove
		override def moveTo(move: MrXMove): MrX.this.type = move match {
			case TicketMove(colour, ticket, origin, destination) => ???
			case DoubleMove(colour, first, second)               => ???
		}
	}
	case class Detective(colour: Colour, location: Location, tickets: Tickets) extends Player {
		override type M = DetectiveMove
		override def moveTo(move: DetectiveMove): Detective.this.type = move match {
			case TicketMove(colour, ticket, origin, destination) => ???
		}

	}

	sealed trait Move {def colour: Colour}
	sealed trait MrXMove extends Move
	sealed trait DetectiveMove extends Move
	case class TicketMove(colour: Colour,
						  ticket: Ticket,
						  origin: Location,
						  destination: Location) extends DetectiveMove with MrXMove

	case class DoubleMove(colour: Black.type,
						  first: TicketMove,
						  second: TicketMove) extends MrXMove

	def mkDefaultRounds(): Seq[Visibility] = Seq()

	def mkDefaultDetectiveTickets(): Tickets = Map(
		TaxiTicket -> Amount(1),
		BusTicket -> Amount(1),
		UndergroundTicket -> Amount(1),
		SecretTicket -> Amount(1),
		DoubleTicket -> Amount(1),
	)

	def mkDefaultMrXTickets(): Tickets = Map(
		TaxiTicket -> Amount(1),
		BusTicket -> Amount(1),
		UndergroundTicket -> Amount(1),
		SecretTicket -> Amount(1),
		DoubleTicket -> Amount(1),
	)

	case class Setup(graph: Graph,
					 rounds: Seq[Visibility],
					 mrX: MrX,
					 detectives: SortedSet[Player])

	trait Board {
		def setup: Setup
		def round: Int
		def currentTurn: Colour
		def nextTurn: Colour
		def mrXRoundVisibility: Visibility
		def playerTickets(colour: Colour): Option[Tickets]
		def playerLocation(colour: Colour): Option[Location]
		def possibleMoves(): Seq[Move]
	}

	case class TheBoard(setup: Setup,
						round: Int,
						currentTurn: Colour) extends Board {
		private val Setup(graph, rounds, mrX, detectives)  = setup
		private val everyone: IndexedSeq[Player] = (detectives + mrX).toIndexedSeq
		override def nextTurn: Colour = everyone.indexOf()
		override def mrXRoundVisibility: Visibility = rounds(round)
		override def playerTickets(colour: Colour): Option[Tickets] = everyone
			.find {_.colour == colour}
			.map {_.tickets}
		override def playerLocation(colour: Colour): Option[Location] = everyone
			.find {_.colour == colour}
			.map {_.location}
		override def possibleMoves(): Seq[Move] = ???
	}


	sealed trait Round

	sealed trait DetectiveRound extends Round
	sealed trait MrXRound extends Round

	case class DetectiveSelect(board: Board, next: (DetectiveMove) => Round) extends DetectiveRound
	case class MrXSelect(board: Board, next: (MrXMove) => DetectiveRound) extends MrXRound

	case class MrXVictory(board: Board) extends MrXRound with DetectiveRound
	case class DetectiveVictory(board: Board) extends MrXRound with DetectiveRound


	//	case class DetectiveRound(board: Board, next: (DetectiveMove) => Round) extends Round
	//	case class MrXRound(board: Board, next: MrXMove => DetectiveRound) extends Round
	//	sealed trait Victory
	//	case class DetectiveVictory(board: Board) extends Victory
	//	case class MrXVictory(board: Board) extends Victory

	def startGame(setup: Setup): MrXRound = {

		def won(board: Board): Option[MrXRound with DetectiveRound] = {
			val winner: Option[Colour] = ??? // TODO check for winners
			winner.collect {
				case Black => MrXVictory(board)
				case _     => DetectiveVictory(board)
			}
		}


		def progressDetective(board: Board, move: DetectiveMove): Round = {
			won(board) match {
				case Some(won) => won
				case None      => DetectiveSelect(board, progressDetective(board, _))
			}
		}

		def progressMrX(board: Board, move: MrXMove): DetectiveRound = {
			won(board) match {
				case Some(won) => won
				case None      => DetectiveSelect(board, progressDetective(board, _))
			}
		}

		val board = TheBoard(setup, 0, Black)
		MrXSelect(board, progressMrX(board, _))
	}

}

