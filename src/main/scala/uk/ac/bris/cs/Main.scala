package uk.ac.bris.cs

import net.kurobako.gesturefx.GesturePane
import uk.ac.bris.cs.Trampoline.Eval.{apply => _, unapply => _}
import uk.ac.bris.cs.scotlandyard.ScotlandYard.Location._
import uk.ac.bris.cs.scotlandyard.ScotlandYard._
import uk.ac.bris.cs.scotlandyard._

import scala.io.Source
import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.scene.Scene
import scalafx.scene.control.{CheckMenuItem, Menu, MenuBar, MenuItem}
import scalafx.scene.image.Image
import scalafx.scene.layout._

object Main extends JFXApp {


	private final val MapImage    : Image                   = new Image("map.png", false)
	private final val Graph       : Graph                   = readGraph(Source.fromResource("graph.txt")).get
	private final val MapLocations: Map[Location, Position] = readMapLocations(Source.fromResource("pos.txt")).get
	private final val TicketImages: Map[Ticket, Image]      = Map(
		TaxiTicket -> "tickets/taxi.png",
		BusTicket -> "tickets/bus.png",
		UndergroundTicket -> "tickets/underground.png",
		SecretTicket -> "tickets/secret.png",
		DoubleTicket -> "tickets/double.png").map { case (k, v) => (k, new Image(v, false)) }


	private val boardPane        = new BoardPane(MapImage, MapLocations, TicketImages)
	private val gestureBoardPane = new GesturePane(boardPane)
	private val travelLogPane    = new TravelLogPane(TicketImages)
	private val ticketPane       = new TicketPane(TicketImages)


	def startGame(board: Board): Unit = {
		board.computeWinner() match {
			case Some(value) => println(s"Winner $value")
			case None        =>
				boardPane(board)
				ticketPane(board)
				travelLogPane(board.mrXTravelLog)
				boardPane.hintMoves(board, board.computePossibleMoves(), { m =>
					println(s"Select $m")
					Platform.runLater{

						startGame(board.progress(m))
					}
				})
		}
	}

	val r = startGame(StandardBoard(
		graph = Graph,
		rounds = StandardGame.Rounds,
		mrX = Player(Black, @!(14), StandardGame.mkDefaultMrXTickets()),
		detectives = Player(Red, @!(26), StandardGame.mkDefaultDetectiveTickets()) :: Nil
	))




	// layout stuff


	gestureBoardPane.hgrow = Priority.Always
	stage = new JFXApp.PrimaryStage()
	stage.title = "ScalaYard"


	private val menu = new MenuBar() {
		useSystemMenuBar = true
		menus = Seq(
			new Menu("File") {
				items = Seq(new MenuItem("Exit") {onAction = { _ => Platform.exit() }})
			},
			new Menu("View") {
				items = Seq(new CheckMenuItem("Tickets") {
					selected = true
					ticketPane.managed <== selected
				}, new CheckMenuItem("Mr.X Travel log") {
					selected = true
					travelLogPane.managed <== selected
				})
			},
			new Menu("Help") {
				items = Seq(new MenuItem("About") {
					onAction = { _ =>

					}
				})
			}
		)
	}

	stage.scene = new Scene(
		new VBox() {
			id = "root"
			stylesheets = Seq(getClass.getResource("/styles.css").toExternalForm)
			children = Seq(menu, new HBox() {
				children ++= Seq(ticketPane, gestureBoardPane, travelLogPane)
				vgrow = Priority.Always
			})
		}, 1000, 700)

}
