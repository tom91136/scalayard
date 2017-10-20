package uk.ac.bris.cs

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
		def location(): Location
		def tickets(): Tickets
		def colour(): Colour
	}
	case class MrX(location: Location, tickets: Tickets) extends Player {
		val colour: Black.type = Black
	}
	case class Detective(colour: Colour, location: Location, tickets: Tickets) extends Player

	sealed trait Move[P <: Player] {
		def player(): P
	}

	case class TicketMove[C <: Player](player: C,
									   ticket: Ticket,
									   origin: Location,
									   destination: Location) extends Move[C]

	case class DoubleMove(player: MrX,
						  first: TicketMove[Player],
						  second: TicketMove[Player]) extends Move[MrX]

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
		def round(): Int
		def turn(): Colour
		def mrXRoundVisibility: Visibility
		def isGameOver: Boolean
		def playerTickets(colour: Colour): Option[Tickets]
		def playerLocation(colour: Colour): Option[Location]
		def possibleMoves(colour: Colour): Seq[Move[Player]]
	}

	case class TheBoard(setup: Setup, round: Int, turn: Colour) extends Board {
		private val Setup(graph, rounds, mrX, detectives) = setup
		private val everyone: SortedSet[Player]           = detectives + mrX
		override def mrXRoundVisibility: Visibility = rounds(round)
		override def isGameOver: Boolean = ???
		override def playerTickets(colour: Colour): Option[Tickets] = everyone
			.find {_.colour() == colour}
			.map {_.tickets()}
		override def playerLocation(colour: Colour): Option[Location] = everyone
			.find {_.colour() == colour}
			.map(_.location())
		override def possibleMoves(colour: Colour): Seq[Move[Player]] = ???
	}

	sealed trait Round
	case class DetectiveRound(next: (Board => Move[Detective]) => Round) extends Round
	case class MrXRound(next: (Board => Move[MrX]) => DetectiveRound) extends Round

	def startGame(setup: Setup): Round = {
		// TODO
		???
	}

}

