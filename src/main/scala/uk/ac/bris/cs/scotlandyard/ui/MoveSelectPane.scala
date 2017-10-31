package uk.ac.bris.cs.scotlandyard.ui

import uk.ac.bris.cs.scotlandyard.ScotlandYard.{Location, Move, Ticket}
import uk.ac.bris.cs.scotlandyard.ui.BoardPane.MoveSelection
import uk.ac.bris.cs.RichScalaFX.{fadeInTransition, findOrMkUnsafe, propertyToOption}

import scalafx.scene.control.{Button, ScrollPane}
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, Priority, TilePane}

class MoveSelectPane(ticketImageMap: Map[Ticket, Image]) extends ScrollPane {

	minHeight = 40
	maxHeight = 300
	fitToWidth = true
	vbarPolicy = ScrollBarPolicy.Never
	hbarPolicy = ScrollBarPolicy.AsNeeded
	visible = false

	def apply(ms: Seq[MoveSelection], hf: Seq[Location] => Unit, sfn: Move => Unit): Unit = {
		val tickets = new HBox() {
			children = ms.groupBy {_.move.colour}
				.map { case (c, xs) =>
					val colourClass = BoardPane.CounterClasses(c)
					val buttons = xs.map { case MoveSelection(m, stops) =>
						new Button() {
							styleClass ++= Set(colourClass, "ticket-choice")
							graphic = new HBox() {
								children = stops.map {
									case (t, _) => new ImageView(ticketImageMap(t))
								}
							}
							hover.onChangeOption {
								case Some(h) =>
									println(s"$h")
									hf(if (h) stops.map { case (_, l) => l } else Nil)
								case None    => ()
							}
							onAction = { _ =>
								hide()
								sfn(m)
							}
						}
					}
					new TilePane() {
						hgrow = Priority.Always
						children = buttons
						minHeight = 0
						styleClass ++= Seq(colourClass, "ticket-colour-group")
					}
				}
		}
		content = tickets
		visible = true
	}

	def showing: Boolean = visible.value

	def hide(): Unit = {
		visible = false
	}


}
