package uk.ac.bris.cs.scotlandyard.ui

import uk.ac.bris.cs.RichScalaFX._
import uk.ac.bris.cs.scotlandyard.ScotlandYard._
import uk.ac.bris.cs.scotlandyard.ui.TravelLogPane._

import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Pos
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{Priority, VBox}

class TravelLogPane(ticketImages: Map[Ticket, Image]) extends VBox {

	minWidth = 230
	alignment = Pos.TopCenter
	styleClass += "travel-log"

	type IndexedRow = (Row, Int)

	private val roundCol    = mkColumn[IndexedRow]("#", { case (Row(vis, _), idx) =>
		new Label(idx.toString) {
			alignment = Pos.Center
			prefWidth = 30
			prefHeight <== prefWidth
			styleClass ++= Seq(
				"round",
				vis match {
					case Shown  => "shown"
					case Hidden => "hidden"
				})
		}
	})
	private val ticketCol   = mkColumn[IndexedRow]("Ticket", { v =>
		val view = new ImageView() {
			fitWidth = 70
			fitHeight = 45
			styleClass += "ticket"
		}
		PartialFunction.condOpt(v) {
			case (Row(_, Some((ticket, _))), _) => ticketImages(ticket)
		}.foreach { t => view.image = t }
		view
	})
	private val locationCol = mkColumn[IndexedRow]("@", {
		case (Row(_, Some((_, location))), _) => new Label(location.value.toString)
		case (Row(_, None), _)                => new Label(" - ")
	})

	private val rounds = new TableView[IndexedRow]() {
		columnResizePolicy = TableView.ConstrainedResizePolicy
		columns ++= Seq(roundCol, ticketCol, locationCol)
		vgrow = Priority.Always
	}

	children = Seq(new Label("MrX Travel Log") {styleClass += "title"}, rounds)


	def apply(log: MrXTravelLog): Unit = {
		rounds.items = ObservableBuffer(log.rows.zipWithIndex.map { case (v, i) => (v, i + 1) })
	}


}
object TravelLogPane {

	def mkColumn[T](name: String, f: T => Node): TableColumn[T, T] =
		new TableColumn[T, T](name) {
			cellValueFactory = { x => ObjectProperty(x.value) }
			cellFactory = { _ =>
				new TableCell[T, T]() {
					item.onChangeOption { o =>
						graphic = o.map {f}.orNull
						alignment = Pos.Center
					}
				}
			}
		}


}
