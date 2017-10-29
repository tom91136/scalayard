package uk.ac.bris.cs.scotlandyard

import java.lang.Math._

import uk.ac.bris.cs.RichScalaFX.{findOrMkUnsafe, propertyToOption}
import uk.ac.bris.cs.scotlandyard.BoardPane.{CounterClasses, bindDimension, dist}
import uk.ac.bris.cs.scotlandyard.ScotlandYard._

import scalafx.Includes._
import scalafx.beans.property.ReadOnlyDoubleProperty
import scalafx.geometry.Side
import scalafx.scene.control.{ContextMenu, MenuItem}
import scalafx.scene.effect.BlendMode
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, Pane, Region}
import scalafx.scene.shape.Circle

class BoardPane(val map: Image,
				val locationPositions: Map[Location, Position],
				val ticketImages: Map[Ticket, Image]
			   ) extends Pane {


	styleClass += "board"


	private val shadows = new Pane() {
		style = "-fx-background-color: rgba(0,0, 0, 0.5)"
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
					radius = 35
					translateX = x
					translateY = y
				}
			}
		}
		shadows.children = mask
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

		case class MoveSelection(move: Move, stops: Seq[(Ticket, Location)])

		val selections = mvs.map {
			case tm@TicketMove(_, t, _, l) => MoveSelection(tm, Seq((t, l)))
			case dm@DoubleMove(_,
			TicketMove(_, t1, _, l1),
			TicketMove(_, t2, _, l2))      => MoveSelection(dm, Seq((t1, l1), (t2, l2)))
		}.groupBy { case MoveSelection(_, _ :+ ((_, l))) => l }

		highlight(selections.keys.toSeq)

		hints.children = selections.map { case (destination, ss) =>


			val is = ss.toSeq.sortBy {
				case MoveSelection(m, ((_, l)) :: _) =>
					board.lookup(m.colour) match {
						case Some(p) => dist(locationPositions(p.location), locationPositions(l))
						case None    => 0
					}
				case _                               => 0
			}.map { case MoveSelection(m, xs) =>
				new MenuItem() {
					graphic = new HBox() {
						children = xs.map { case (t, _) => new ImageView(ticketImages(t)) }
						hover.onChangeOption {
							case Some(b) => if (b) highlight(xs.map { case (_, l) => l })
							case None    =>
						}
					}
					onAction = { _ =>
						disable = true
						sfn(m)
					}
				}
			}
			val menu = new ContextMenu(is: _*) {
				styleClass += "hint-menu"
				onAutoHide = { _ => highlight(selections.keys.toSeq) }
			}


			val (x, y) = locationPositions(destination)
			new Circle() {
				styleClass += "hint"
				radius = 35
				translateX = x
				translateY = y
				onMouseClicked = { _ =>
					if (!menu.isShowing) menu.show(this, Side.Bottom, 0, 0)
				}
			}
		}


	}

	private val panes = Seq(shadows, players, hints)

	children = new ImageView(map) +: panes

	(panes :+ this).foreach { n => bindDimension(n, map.width, map.height) }


}
object BoardPane {

	val CounterClasses: Map[Colour, String] = Map(
		Black -> "counter-black",
		Red -> "counter-red",
		Green -> "counter-green",
		Blue -> "counter-blue",
		Yellow -> "counter-yellow",
		White -> "counter-white",
	)

	def dist(x: Position, y: Position): Double = sqrt(pow(x._1 - y._1, 2) + pow(x._2 - y._2, 2))


	private def bindDimension(region: Region,
							  width: ReadOnlyDoubleProperty,
							  height: ReadOnlyDoubleProperty): Unit = {
		region.minWidth <== width
		region.minHeight <== height
		region.maxWidth <== region.minWidth
		region.maxHeight <== region.minHeight
	}

}