package uk.ac.bris.cs.scotlandyard

import uk.ac.bris.cs.UndirectedGraph
import uk.ac.bris.cs.UndirectedGraph.Edge

import scala.annotation.tailrec
import scala.io.BufferedSource
import scala.util.Try

object ScotlandYard {


	sealed trait Side
	case object MrXSide extends Side
	case object DetectiveSide extends Side

	sealed trait Colour
	sealed trait MrXColour extends Colour
	sealed trait DetectiveColour extends Colour

	final case object Black extends MrXColour
	final case object Red extends DetectiveColour
	final case object Green extends DetectiveColour
	final case object Blue extends DetectiveColour
	final case object Yellow extends DetectiveColour
	final case object White extends DetectiveColour

	sealed trait Transport
	final case object Taxi extends Transport
	final case object Bus extends Transport
	final case object Underground extends Transport
	final case object Boat extends Transport

	object Transport {
		final              val Transports = Seq(Taxi, Bus, Underground, Boat)
		// we need proper enums
		private final lazy val Names      = Transports.map { s => (s.toString, s) }.toMap
		def fromString(transport: String): Option[Transport] = Names.get(transport)
	}

	final val TicketLookup: PartialFunction[Transport, Ticket] = {
		case Taxi        => TaxiTicket
		case Bus         => BusTicket
		case Underground => UndergroundTicket
		//		case Boat        => SecretTicket
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
		def empty: Boolean = value == 0
		def notEmpty: Boolean = !empty
	}
	object Amount {
		final val Zero: Amount = Amount(0)
	}

	final case class Tickets(map: Map[Ticket, Amount]) extends AnyVal {
		def +(t: Ticket): Tickets = Tickets(map + (t -> (map.getOrElse(t, Amount.Zero) ++)))
		def -(t: Ticket): Tickets = Tickets(map + (t -> (map.getOrElse(t, Amount.Zero) --)))
		def ∈:(t: Ticket): Boolean = map.get(t).exists(_.notEmpty)
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
	final case class Detective(colour: DetectiveColour, location: Location, tickets: Tickets) extends Player

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


	trait Board {
		def graph: Graph
		def mrX: MrX
		def detectives: Seq[Detective]
		def mrXTravelLog: MrXTravelLog
		def pendingSide: Side
		def computePossibleMoves(): Set[Move]
		def computeWinner(): Option[Colour]
		def progress(move: Move): Board
	}


	case class Row(visibility: Visibility, ticketAndLocation: Option[(Ticket, Location)])
	case class MrXTravelLog(rows: Seq[Row], currentRound: Option[Int]) {

		lazy val lastVisibleLocation: Option[Location] = {
			// search backwards
			@tailrec def lastShown(rs: Seq[Row]): Option[Location] = rs match {
				case xs :+ Row(Hidden, _)                 => lastShown(xs)
				case _ :+ Row(Shown, Some((_, location))) => Some(location)
				case _                                    => None
			}

			lastShown(rows)
		}
		lazy val isFinalRound       : Boolean          = currentRound.contains(totalRounds)
		val totalRounds: Int = rows.size


		def log(move: MrXMove): MrXTravelLog = {
			move match {
				case TicketMove(_, ticket, _, dest) => log(ticket, dest)
				case DoubleMove(_, first, second)   => log(first).log(second)
			}
		}

		private def log(ticket: Ticket, location: Location): MrXTravelLog = {
			// TODO bad bad bad
			if (isFinalRound) throw new IllegalStateException("Final round")
			val next = currentRound.map {_ + 1}.getOrElse(0)
			val updated = rows(next).copy(ticketAndLocation = Some((ticket, location)))
			copy(rows = rows.updated(next, updated), Some(next))
		}
	}
	object MrXTravelLog {
		def apply(visibilities: Seq[Visibility]): MrXTravelLog =
			new MrXTravelLog(visibilities.map { v => Row(v, None) }, None)
	}


	sealed trait Round
	sealed trait DetectiveRound extends Round
	sealed trait MrXRound extends Round
	final case class DetectiveSelect(board: Board, next: (DetectiveMove) => Round) extends DetectiveRound
	final case class MrXSelect(board: Board, next: (MrXMove) => DetectiveRound) extends MrXRound
	final case class MrXVictory(board: Board) extends MrXRound with DetectiveRound
	final case class DetectiveVictory(board: Board) extends MrXRound with DetectiveRound

	type Position = (Int, Int)

	def readGraph(source: BufferedSource): Try[Graph] = Try {
		// TODO proper error handling and stuff
		val (first :: ls) = source.getLines().toList

		val (nodeCount, edgeCount) = first.split(" ").toList match {
			case node :: edge :: Nil => (node.toInt, edge.toInt)
			case bad@_               => throw new IllegalArgumentException(s"Invalid format $bad")
		}

		val nodes = ls.take(nodeCount)
			.foldRight(UndirectedGraph(): Graph) { (l, g) => g + Location(l.toInt) }


		ls.slice(nodeCount, nodeCount + edgeCount).foldRight(nodes) { (l, g) =>
			val edge = l.split(" ")
			g + Edge(
				Location(edge(0).toInt),
				Location(edge(1).toInt),
				Transport.fromString(edge(2)).get)
		}
	}

	def readMapLocations(source: BufferedSource): Try[Map[Location, Position]] = Try {
		// TODO proper error handling and stuff
		val (first :: ls) = source.getLines().toList
		val posCount = first.toInt
		ls.take(posCount)
			.map {_.split(" ").toList}
			.map {
				case l :: x :: y :: Nil => (Location(l.toInt), (x.toInt, y.toInt))
				case bad@_              => throw new IllegalArgumentException(s"Invalid format $bad")
			}.toMap
	}


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
				next.pendingSide match {
					case MrXSide       => MrXSelect(board, progressMrX(board, _))
					case DetectiveSide => DetectiveSelect(board, progressDetective(board, _))
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

