//package uk.ac.bris
//
//import uk.ac.bris.cs.ScotlandYard._
//import uk.ac.bris.cs.scotlandyard.ScotlandYard.MrX
//
//package object cs {
//
//
//	import uk.ac.bris.cs.UndirectedGraph
//	import uk.ac.bris.cs.UndirectedGraph.Edge
//
//	import scala.annotation.tailrec
//	import scala.io.BufferedSource
//	import scala.util.Try
//
//	object ScotlandYard {
//
//
//		sealed trait Colour
//		sealed trait DetectiveColour extends Colour
//		final case object Black extends Colour
//		final case object Red extends DetectiveColour
//		final case object Green extends DetectiveColour
//		final case object Blue extends DetectiveColour
//		final case object Yellow extends DetectiveColour
//		final case object White extends DetectiveColour
//
//		sealed trait Transport
//		final case object Taxi extends Transport
//		final case object Bus extends Transport
//		final case object Underground extends Transport
//		final case object Boat extends Transport
//
//		object Transport {
//			final              val Transports = Seq(Taxi, Bus, Underground, Boat)
//			// we need proper enums
//			private final lazy val Names      = Transports.map { s => (s.toString, s) }.toMap
//			def fromString(transport: String): Option[Transport] = Names.get(transport)
//		}
//
//		final val TicketLookup: PartialFunction[Transport, Ticket] = {
//			case Taxi        => TaxiTicket
//			case Bus         => BusTicket
//			case Underground => UndergroundTicket
//			//		case Boat        => SecretTicket
//		}
//
//		sealed trait Ticket
//		final case object TaxiTicket extends Ticket
//		final case object BusTicket extends Ticket
//		final case object UndergroundTicket extends Ticket
//		final case object SecretTicket extends Ticket
//		final case object DoubleTicket extends Ticket
//
//		sealed trait Visibility
//		final case object Shown extends Visibility
//		final case object Hidden extends Visibility
//
//		final case class Amount(value: Int) extends AnyVal {
//			def ++ : Amount = Amount(value + 1)
//			def -- : Amount = Amount(if (value == 0) 0 else value - 1)
//			def empty: Boolean = value == 0
//			def notEmpty: Boolean = !empty
//		}
//		object Amount {
//			final val Zero: Amount = Amount(0)
//		}
//
//		final case class Tickets(map: Map[Ticket, Amount]) extends AnyVal {
//			def +(t: Ticket): Tickets = Tickets(map + (t -> (map.getOrElse(t, Amount.Zero) ++)))
//			def -(t: Ticket): Tickets = Tickets(map + (t -> (map.getOrElse(t, Amount.Zero) --)))
//			def ∈:(t: Ticket): Boolean = map.get(t).exists(_.notEmpty)
//			def ⊆:(ts: Seq[Ticket]): Boolean = {
//				ts.groupBy {identity}.forall { case (t, xs) => map(t).value >= xs.size }
//			}
//			def isEmpty: Boolean = map.isEmpty
//		}
//
//		final case class Location(value: Int) extends AnyVal
//		object Location {def @!(location: Int): Location = apply(location)}
//
//		type Graph = UndirectedGraph[Location, Transport]
//
//		final case class Player[+C](colour: C,
//									location: Location,
//									tickets: Tickets)
//
//		sealed trait Move[+C <: Colour] {def colour: C}
//		final case class TicketMove[+C](colour: C,
//									   ticket: Ticket,
//									   origin: Location,
//									   destination: Location) extends Move[C]
//
//		final case class DoubleMove(colour: Black.type,
//													 first: TicketMove[Black.type],
//													 second: TicketMove[Black.type]) extends Move[Black.type]
//
//
//		sealed trait Side[C <: Colour]
//		case object MrXSide extends Side[Black.type]
//		case object DetectiveSide extends Side[DetectiveColour]
//
//
//		trait Board[C <: Colour, NC <: Board[_, NC]] {
//			def graph: Graph
//			def lookup[That <: Colour](c: That): Option[Player[That]]
//			def mrX: Player[Black.type]
//			def detectives: Seq[Player[DetectiveColour]]
//			def mrXTravelLog: MrXTravelLog
//			def pendingSide: Side[C]
//			def computeMove(): Set[Move[C]]
//			def computeWinner(): Option[Colour]
//			def progress(move: Move[C]): NC
//		}
//
//		case class Row(visibility: Visibility, ticketAndLocation: Option[(Ticket, Location)])
//		case class MrXTravelLog(rows: Seq[Row], currentRound: Option[Int]) {
//
//			lazy val lastVisibleLocation: Option[Location] = {
//				// search backwards
//				@tailrec def lastShown(rs: Seq[Row]): Option[Location] = rs match {
//					case xs :+ Row(Hidden, _)                 => lastShown(xs)
//					case _ :+ Row(Shown, Some((_, location))) => Some(location)
//					case _                                    => None
//				}
//
//				lastShown(rows)
//			}
//			lazy val isFinalRound       : Boolean          = currentRound.contains(totalRounds)
//			val totalRounds: Int = rows.size
//
//
//			def log[C <: Black.type ](move: Move[C]): MrXTravelLog = {
//				move match {
//					case TicketMove(_, ticket, _, dest) => log(ticket, dest)
//					case DoubleMove(_, first, second)   => log(first).log(second)
//				}
//			}
//
//			private def log(ticket: Ticket, location: Location): MrXTravelLog = {
//				// TODO bad bad bad
//				if (isFinalRound) throw new IllegalStateException("Final round")
//				val next = currentRound.map {_ + 1}.getOrElse(0)
//				val updated = rows(next).copy(ticketAndLocation = Some((ticket, location)))
//				copy(rows = rows.updated(next, updated), Some(next))
//			}
//		}
//		object MrXTravelLog {
//			def apply(visibilities: Seq[Visibility]): MrXTravelLog =
//				new MrXTravelLog(visibilities.map { v => Row(v, None) }, None)
//		}
//
//		type Position = (Int, Int)
//
//		def readGraph(source: BufferedSource): Try[Graph] = Try {
//			val (first :: ls) = source.getLines().toList
//			val (nodeCount, edgeCount) = first.split(" ").toList match {
//				case node :: edge :: Nil => (node.toInt, edge.toInt)
//				case bad@_               => throw new IllegalArgumentException(s"Invalid format $bad")
//			}
//			val nodes = ls.take(nodeCount)
//				.foldRight(UndirectedGraph(): Graph) { (l, g) => g + Location(l.toInt) }
//			ls.slice(nodeCount, nodeCount + edgeCount).foldRight(nodes) { (l, g) =>
//				val edge = l.split(" ")
//				g + Edge(
//					Location(edge(0).toInt),
//					Location(edge(1).toInt),
//					Transport.fromString(edge(2)).get)
//			}
//		}
//
//		def readMapLocations(source: BufferedSource): Try[Map[Location, Position]] = Try {
//			val (xOffset, yOffset) = (60, 60)
//			val (first :: ls) = source.getLines().toList
//			val posCount = first.toInt
//			ls.take(posCount)
//				.map {_.split(" ").toList}
//				.map {
//					case l :: x :: y :: Nil => (Location(l.toInt), (x.toInt + xOffset, y.toInt + yOffset))
//					case bad@_              => throw new IllegalArgumentException(s"Invalid format $bad")
//				}.toMap
//		}
//
//
//		def startGame(initialBoard: Board[Black.type, _]) = {
//			???
//		}
//
//	}
//
//
//	final case class GenBoard[C <: Colour](graph: Graph,
//										   mrX: Player[Black.type],
//										   detectives: Seq[Player[DetectiveColour]],
//										   mrXTravelLog: MrXTravelLog,
//										   pendingColours: Set[C]) extends Board[C, GenBoard[_]] {
//
//		import scala.collection.breakOut
//
//		private val everyone : Seq[Player[Colour]]         = detectives :+ mrX
//		private val playerMap: Map[Colour, Player[Colour]] = everyone.map { d => (d.colour, d) }(breakOut)
//
//
//		override def lookup[That <: Colour](c: That): Option[Player[That]] = {
//			// FIXME K ~> [K, V[K]]
//			playerMap.get(c).map { v => v.asInstanceOf[Player[That]] }
//		}
//
//		private def computePossibleMoves[CC <: Colour](player: Player[CC]): Seq[Move[CC]] = {
//
//			def mkMovesFrom(source: Location, p: Player[CC]): Seq[TicketMove[CC]] = for {
//				Edge(s, e, t) <- graph.edgesFrom(source)
//				move <- Seq(
//					TicketMove[CC](p.colour, TicketLookup(t), s, e),
//					TicketMove[CC](p.colour, SecretTicket, s, e))
//				if (move.ticket ∈: p.tickets) &&
//				   detectives.forall { d => d.location != move.destination }
//			} yield move
//
//			def mkMoves[B](p: Player[B]): Seq[Move[B]] = p match {
//				case mrX@Player(Black, location, _)   =>
//
//					val mxx: Player[Black.type ] = mrX.asInstanceOf[Player[Black.type ]]
//					for {
//					first <- mkMovesFrom(location, mxx)
//					second <- mkMovesFrom(first.destination, mxx)
//					if (DoubleTicket ∈: mrX.tickets) &&
//					   (Seq(first.ticket, second.ticket) ⊆: mrX.tickets)
//				} yield DoubleMove(mxx.colour, first, second)
//				case detective@Player(_, location, _) => mkMovesFrom(location, detective)
//			}
//
//			mkMoves(player)
//		}
//
//		override def pendingSide: Side[C] = ??? //if (pendingColours.contains(Black)) MrXSide else DetectiveSide
//
//
//		override def computeMove(): Set[Move[C]] = {
//
//			???
////			pendingColours.map { c => lookup(c) }.flatMap {computePossibleMoves}
//
//		}
//
//
//		override def computeWinner(): Option[Colour] = {
//
//			def allDetectivesStuck: Boolean = detectives.flatMap {computePossibleMoves}.isEmpty
//
//			def mrXStuck: Boolean = computePossibleMoves(mrX).isEmpty
//
//			pendingColours.toList match {
//				case Black :: Nil if mrXTravelLog.isFinalRound ||
//									 allDetectivesStuck => Some(Black)
//				case Black :: Nil if mrXStuck           => None // special case where MrX cannot move
//				case _                                  => detectives.collectFirst {
//					case Player(colour, location, _) if location == mrX.location => colour
//				}
//			}
//		}
//
//		override def progress(move: Move[C]): GenBoard[_] = {
//			println("mv")
//			move match {
//				case m@TicketMove(Black, ticket, _, dest) =>
//					GenBoard[DetectiveColour](
//						graph = graph,
//						mrX = mrX.copy(location = dest, tickets = mrX.tickets - ticket),
//						mrXTravelLog = mrXTravelLog.log(m),
//						detectives = detectives,
//						pendingColours = detectives.map {_.colour}.toSet
//					)
//				case m@DoubleMove(Black, first, second)   =>
//					GenBoard[DetectiveColour](
//						graph = graph,
//						mrX = mrX.copy(
//							location = second.destination,
//							tickets = mrX.tickets - first.ticket - second.ticket),
//						mrXTravelLog = mrXTravelLog.log(m),
//						detectives = detectives,
//						pendingColours = detectives.map {_.colour}.toSet
//					)
//				case x@TicketMove(colour, ticket, _, dest)  =>
//					mrXTravelLog.log(x)
//					val nextPend = pendingColours - colour
//
//					def mkNext[S](pcs: Set[S]) = GenBoard[S](
//						graph = graph,
//						mrX = mrX.copy(tickets = mrX.tickets + ticket),
//						detectives = detectives.collect {
//							case d@Player(`colour`, _, tickets) =>
//								d.copy(location = dest, tickets = tickets - ticket)
//							case d@_                            => d
//						},
//						mrXTravelLog = mrXTravelLog,
//						pendingColours = pcs
//					)
//
//					if (nextPend.isEmpty) mkNext(Set(Black))
//					else mkNext(nextPend)
//			}
//		}
//		override def toString: String = s"GenBoard(" +
//										s"\nplayers=$everyone, " +
//										s"\ntravelLog=$mrXTravelLog, " +
//										s"\npending=$pendingColours)"
//	}
//
//
//}
