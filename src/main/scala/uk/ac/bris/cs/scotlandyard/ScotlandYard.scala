package uk.ac.bris.cs.scotlandyard

import uk.ac.bris.cs.UndirectedGraph
import uk.ac.bris.cs.UndirectedGraph.Edge

import scala.annotation.tailrec
import scala.io.BufferedSource
import scala.util.Try

/**
  * All concepts related to the scotland yard game
  */
object ScotlandYard {

	/**
	  * Concept of a side, either [[MrXSide]] or [[DetectiveSide]]
	  */
	sealed trait Side
	case object MrXSide extends Side
	case object DetectiveSide extends Side

	/**
	  * Concept of colours
	  */
	sealed trait Colour
	sealed trait DetectiveColour extends Colour
	final case object Black extends Colour
	final case object Red extends DetectiveColour
	final case object Green extends DetectiveColour
	final case object Blue extends DetectiveColour
	final case object Yellow extends DetectiveColour
	final case object White extends DetectiveColour

	/** Concept of visibility(used for Mr'X only) */
	sealed trait Visibility
	final case object Shown extends Visibility
	final case object Hidden extends Visibility

	/** Concept of transports */
	sealed trait Transport
	final case object Taxi extends Transport
	final case object Bus extends Transport
	final case object Underground extends Transport
	final case object Boat extends Transport

	/** Concept of tickets */
	sealed trait Ticket
	final case object TaxiTicket extends Ticket
	final case object BusTicket extends Ticket
	final case object UndergroundTicket extends Ticket
	final case object SecretTicket extends Ticket
	final case object DoubleTicket extends Ticket

	object Transport {
		final              val Transports = Seq(Taxi, Bus, Underground, Boat)
		// we need proper enums
		private final lazy val Names      = Transports.map { s => (s.toString, s) }.toMap
		def fromString(transport: String): Option[Transport] = Names.get(transport)
	}

	/** Maps [[Transport]] to the corresponding [[Ticket]] totally */
	final val TicketLookup: PartialFunction[Transport, Ticket] = {
		case Taxi        => TaxiTicket
		case Bus         => BusTicket
		case Underground => UndergroundTicket
		case Boat        => SecretTicket
	}


	/**
	  * Concept of some positive amount
	  * @param value the amount, not negative
	  */
	final case class Amount(value: Int) extends AnyVal {
		def ++ : Amount = Amount(value + 1)
		def -- : Amount = Amount(if (value == 0) 0 else value - 1)
		def empty: Boolean = value == 0
		def notEmpty: Boolean = !empty
	}
	object Amount {
		final val Zero: Amount = Amount(0)
	}

	/**
	  * Concept of a collection of [[Ticket]] that roughly corresponds to a ticket wallet in the
	  * scotland yard game
	  * @param map the map of ticket to amount
	  */
	final case class Tickets(map: Map[Ticket, Amount]) extends AnyVal {
		/** add one particular type of ticket */
		def +(t: Ticket): Tickets = Tickets(map + (t -> (map.getOrElse(t, Amount.Zero) ++)))
		/** remove one particular type of ticket */
		def -(t: Ticket): Tickets = Tickets(map + (t -> (map.getOrElse(t, Amount.Zero) --)))
		/** whether the collection has any amount of the ticket */
		def ∈:(t: Ticket): Boolean = map.get(t).exists(_.notEmpty)
		/** whether the collection has all the given amount of tickets(tallied) */
		def ⊆:(ts: Seq[Ticket]): Boolean = {
			ts.groupBy {identity}.forall { case (t, xs) => map(t).value >= xs.size }
		}
		def isEmpty: Boolean = map.isEmpty
	}

	/**
	  * Concept of some location on node map
	  * @param value the location, not negative
	  */
	final case class Location(value: Int) extends AnyVal
	object Location {def @!(location: Int): Location = apply(location)}

	/** Concept of the game map */
	type Graph = UndirectedGraph[Location, Transport]


	/**
	  * Concept of an actual player of some [[colour]]
	  * holding on to [[tickets]]
	  * and stays on the map at some [[location]]
	  */
	final case class Player[+C](colour: C,
								location: Location,
								tickets: Tickets)


	/** Concept of a move */
	sealed trait Move {
		def colour: Colour
		def origin: Location
	}
	/** Concept of a kind of move that [[Player]] can make */
	sealed trait NormalMove extends Move
	/** Concept of a kind of move that only a [[Black]] [[Player]] can make */
	sealed trait MrXMove extends Move
	/** Concept of a ticket move */
	final case class TicketMove(colour: Colour,
								ticket: Ticket,
								origin: Location,
								destination: Location) extends NormalMove with MrXMove
	/** Concept of a double move that only Mr.X can make */
	final case class DoubleMove(colour: Black.type,
								first: TicketMove,
								second: TicketMove) extends MrXMove {
		override def origin: Location = first.origin
	}

	/** Concept of a game board that is essentially the state of the game in any given player's mind */
	trait Board {
		def graph: Graph
		def lookup[C <: Colour](c: C): Option[Player[C]]
		def mrX: Player[Black.type]
		def detectives: Seq[Player[DetectiveColour]]
		def mrXTravelLog: MrXTravelLog
		def pendingSide: Side
		def computePossibleMoves(): Set[Move]
		def computeWinner(): Option[Colour]
		def progress(move: Move): Board
	}

	/** Concept of a row in the [[MrXTravelLog]] */
	case class Row(visibility: Visibility, ticketAndLocation: Option[(Ticket, Location)])

	/** Concept of a travel log that corresponds to the actual scotland yard's travel log */
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


		/** Concept of logging a move by placing used tickets in grooves on the travel log */
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

	type Position = (Double, Double)

	def readGraph(source: BufferedSource): Try[Graph] = Try {
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
		val (xOffset, yOffset) = (60.0, 60.0)
		val (first :: ls) = source.getLines().toList
		val posCount = first.toInt
		ls.take(posCount)
			.map {_.split(" ").toList}
			.map {
				case l :: x :: y :: Nil => (Location(l.toInt), (x.toInt + xOffset, y.toInt + yOffset))
				case bad@_              => throw new IllegalArgumentException(s"Invalid format $bad")
			}.toMap
	}


}

