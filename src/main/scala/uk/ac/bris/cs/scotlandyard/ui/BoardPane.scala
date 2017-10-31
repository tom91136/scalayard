package uk.ac.bris.cs.scotlandyard.ui


import uk.ac.bris.cs.RichScalaFX.{fadeInTransition, findOrMkUnsafe, propertyToOption}
import uk.ac.bris.cs.scotlandyard.ScotlandYard._
import uk.ac.bris.cs.scotlandyard.ui.BoardPane._

import scalafx.Includes._
import scalafx.beans.property.ReadOnlyDoubleProperty
import scalafx.geometry.{Insets, Pos, Side}
import scalafx.scene.Node
import scalafx.scene.control.{ContextMenu, Label, MenuItem}
import scalafx.scene.effect.BlendMode
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout._
import scalafx.scene.paint.{Color, CycleMethod, LinearGradient, Stop}
import scalafx.scene.shape.{Circle, Rectangle}

class BoardPane(val map: Image,
				val locationPositions: Map[Location, Position],
				val ticketImages: Map[Ticket, Image],
				val moveSelectPane: MoveSelectPane
			   ) extends Pane {


	styleClass += "board"


	private val shadows = new Pane() {
		style = "-fx-background-color: rgba(0,0, 0, 0.48)"
	}
	private val players = new Pane() {
		pickOnBounds = false
	}
	private val hints   = new Pane() {
		pickOnBounds = false
	}


	def highlight(ls: Seq[Location]): Unit = {
		val mask = new Pane() {
			blendMode = BlendMode.Overlay
			children = ls.map {locationPositions(_)}.map { case (x, y) =>
				new Circle() {
					styleClass += "highlight"
					radius = 30
					translateX = x
					translateY = y
				}
			}
		}
		shadows.children = mask
		mask.fadeIn(0, 1, 300 ms)
	}


	def apply(board: Board): Unit = {
		(board.detectives :+ board.mrX).foreach { p =>
			val c: Circle = findOrMkUnsafe[
				javafx.scene.shape.Circle,
				javafx.scene.layout.Pane](players, p.colour.toString) { (p, t) => p.children += t; () } {
				new Circle() {
					styleClass ++= Seq("counter", CounterClasses(p.colour))
					radius = 25
					opacity <== (when(hover) choose 0.3 otherwise 1)
				}.delegate
			}
			val (x, y) = locationPositions(p.location)
			c.translateX = x
			c.translateY = y
		}
	}


	def hintMoves(board: Board, mvs: Set[Move], sfn: Move => Unit): Unit = {

		println(s"Select from ${mvs.size}")


		val selections: Map[Location, Set[MoveSelection]] = mvs.map {
			case tm@TicketMove(_, t, _, l) => MoveSelection(tm, Seq((t, l)))
			case dm@DoubleMove(_,
			TicketMove(_, t1, _, l1),
			TicketMove(_, t2, _, l2))      => MoveSelection(dm, Seq((t1, l1), (t2, l2)))
		}.groupBy { case MoveSelection(_, _ :+ ((_, l))) => l }

		val allLocations = selections.keys.toSeq
		highlight(allLocations)


		hints.children = selections.map { case (destination, ss) =>
			val (x, y) = locationPositions(destination)
			val colours = ss.map { case MoveSelection(m, _) => m.colour }

			def combineColours(cs: Seq[Color]) = {
				val width = 1.0 / cs.size
				val stops: Seq[Stop] = for {
					(c, i) <- cs.zipWithIndex
					from = Stop(i.toFloat / cs.size, c)
					stop <- Seq(from, Stop(from.offset + (width - 0.001), c))
				} yield stop
				LinearGradient(0, 0, 1, 0, true, CycleMethod.NoCycle, stops: _*)
			}

			val c = new Circle() {
				styleClass += "hint-circle"
				// TODO fix this so that generic selector can be used
				stroke = combineColours(colours.map { c => Color.valueOf(c.toString).brighter }.toSeq)
				radius = 35
				onMouseClicked = { _ =>
					val mss = ss.toList.sortBy {
						case MoveSelection(m, ((_, l)) :: _) =>
							dist(locationPositions(m.origin), locationPositions(l))
						case _                               => 0
					}
					mss match {
						case MoveSelection(m, _) :: Nil => sfn(m)
						case ms                         =>
							moveSelectPane(ms,
							{ hs => highlight(if (hs.isEmpty) allLocations else hs) },
							sfn)
					}
				}
			}


			def angleDeg(p1: (Double, Double), p2: (Double, Double)): Double = {
				val deg = math.toDegrees(math.atan2(p2._2 - p1._2, p2._1 - p1._1))
				if (deg < 0) 360 + deg else deg
			}


			val tkds = ss.map {
				case MoveSelection(m, ss@(_ :+ ((_, s)) :+ ((_, l)))) => (ss.map {_._1}, angleDeg(locationPositions(s), locationPositions(l)))
				case MoveSelection(m, _ :+ ((t, l)))                  =>
					println(s"$m ${m.origin} => $l")
					(Seq(t), angleDeg(locationPositions(m.origin), locationPositions(l)))
			}


			val ddd = tkds.map { case (ts, deg) => new HBox() {
				alignment = Pos.TopCenter
				children = ts.map { t =>

					val cc = t match {
						case TaxiTicket        => Color.Yellow
						case BusTicket         => Color.Teal
						case UndergroundTicket =>Color.Red
						case SecretTicket      => Color.Black
						case DoubleTicket      =>Color.Maroon
					}

					new Circle(){
						fill = cc
						radius = 8
						opacity = 0.8
						padding = Insets(-10)
					}
//					new ImageView(ticketImages(t)){
//					rotate = 90
//					fitWidth = 20
//					opacity = 0.8
//					preserveRatio = true
//				}
			}
				rotate =  deg-90
				mouseTransparent = true
			}
			}

			new StackPane() {
				translateX <== -width / 2 + x
				translateY <== -height / 2 + y
				alignment = Pos.TopLeft
				children =    Seq(c) ++ddd
			}
		}


	}

	private val panes = Seq(shadows, players, hints)

	children = new ImageView(map) +: panes

	(panes :+ this).foreach { n => bindDimension(n, map.width, map.height) }


}
object BoardPane {

	case class MoveSelection(move: Move, stops: Seq[(Ticket, Location)])

	val CounterClasses: Map[Colour, String] = Map(
		Black -> "counter-black",
		Red -> "counter-red",
		Green -> "counter-green",
		Blue -> "counter-blue",
		Yellow -> "counter-yellow",
		White -> "counter-white",
	)

	def dist(x: Position, y: Position): Double = math.sqrt(
		math.pow(x._1 - y._1, 2) + math.pow(x._2 - y._2, 2)
	)


	private def bindDimension(region: Region,
							  width: ReadOnlyDoubleProperty,
							  height: ReadOnlyDoubleProperty): Unit = {
		region.minWidth <== width
		region.minHeight <== height
		region.maxWidth <== region.minWidth
		region.maxHeight <== region.minHeight
	}

}