package uk.ac.bris.cs.scotlandyard


import uk.ac.bris.cs.RichScalaFX._
import uk.ac.bris.cs.scotlandyard.BoardPane.CounterClasses
import uk.ac.bris.cs.scotlandyard.ScotlandYard._

import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Orientation, Pos}
import scalafx.scene.control.{Label, ListCell, ListView}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, Priority, StackPane, VBox}
import scalafx.scene.shape.Circle

class TicketPane(ticketImageMap: Map[Ticket, Image]) extends VBox {

	minWidth = 230
	alignment = Pos.TopCenter
	styleClass += "ticket"

	private val players = new ListView[Player[Colour]]() {
		cellFactory = { _ =>
			new ListCell[Player[Colour]]() {
				item.onChangeOption { o =>
					graphic = o.map { p =>

						def mkBar(ticket: Ticket) = {
							val n = p.tickets.map(ticket).value
							new HBox() {
								children = Seq(
									new ImageView(ticketImageMap(ticket)) {
										fitWidth = 30
										preserveRatio = true
									},
									new Label(s" $n") {
										styleClass += "amount"
										minWidth = 28
										alignment = Pos.CenterRight
									},
									new Label("|" * n) {styleClass += "bar"})
							}
						}

						val whitelist = p match {
							case Player(Black, _, tickets) => tickets.map.keys.toSeq
							case Player(_, _, _)           => Seq(TaxiTicket, BusTicket, UndergroundTicket)
						}

						new HBox() {
							styleClass += "item"
							children = Seq(
								new StackPane() {
									styleClass += "counter-preview"
									children = Seq(
										new Circle() {
											radius = 20
											styleClass ++= Seq("counter", CounterClasses(p.colour))
										},
										new Label(s"${p.location.value}") {styleClass += "location"}
									)
								},
								new VBox() {
									styleClass += "rows"
									children = whitelist.map {mkBar}
									hgrow = Priority.Always
								}
							)
						}

					}.orNull
				}
			}
		}
		orientation = Orientation.Vertical
		vgrow = Priority.Always
	}

	children = Seq(new Label("Tickets") {styleClass += "title"}, players)

	def apply(board: Board): Unit = {
		players.items = ObservableBuffer(board.mrX +: board.detectives)
	}


}
