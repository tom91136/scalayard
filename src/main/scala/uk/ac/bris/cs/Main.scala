package uk.ac.bris.cs

import net.kurobako.gesturefx.GesturePane
import net.kurobako.gesturefx.GesturePane.FitMode
import scalafx.Includes._
import scalafx.animation.{FadeTransition, Interpolator, ParallelTransition, ScaleTransition}
import scalafx.application.{JFXApp, Platform}
import scalafx.geometry.Point2D
import scalafx.scene.control.{CheckMenuItem, Menu, MenuBar, MenuItem}
import scalafx.scene.image.Image
import scalafx.scene.layout._
import scalafx.scene.{Node, Scene}
import uk.ac.bris.cs.Trampoline.Eval.{apply => _, unapply => _}
import uk.ac.bris.cs.scotlandyard.ScotlandYard.Location._
import uk.ac.bris.cs.scotlandyard.ScotlandYard._
import uk.ac.bris.cs.scotlandyard._
import uk.ac.bris.cs.scotlandyard.ui.{BoardPane, MoveSelectPane, TicketPane, TravelLogPane}

import scala.io.Source

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

	private val moveSelectPane   = new MoveSelectPane(TicketImages)
	private val boardPane        = new BoardPane(MapImage, MapLocations, TicketImages, moveSelectPane)
	private val gestureBoardPane = new GesturePane(boardPane) {
		setFitMode(FitMode.FIT)
		setMinScale(0.1)
		setMaxScale(10)
		zoomTo(0, Point2D.Zero)
		setScrollBarEnabled(false)
	}
	private val travelLogPane    = new TravelLogPane(TicketImages)
	private val ticketPane       = new TicketPane(TicketImages)


	Platform.runLater {
		fadeIn(gestureBoardPane)
	}

	def startGame(board: Board): Unit = {
		board.computeWinner() match {
			case Some(value) => println(s"Winner $value")
			case None        =>
				boardPane(board)
				ticketPane(board)
				travelLogPane(board.mrXTravelLog)
				boardPane.hintMoves(board, board.computePossibleMoves(), { m =>
					println(s"Select $m")
					Platform.runLater {

						startGame(board.progress(m))
					}
				})
		}
	}


	val r = startGame(StandardBoard(
		graph = Graph,
		rounds = StandardGame.Rounds,
		mrX = Player(Black, @!(171), StandardGame.mkDefaultMrXTickets()),
		detectives = StandardGame.mkDetectives[DetectiveColour](Red, Green, Blue, Yellow, White)
			.map { case (l, c) => Player(c, l, StandardGame.mkDefaultDetectiveTickets()) }
	))


	// layout stuff


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
				children = Seq(
					ticketPane,
					new AnchorPane() {
						hgrow = Priority.Always
						children ++= Seq(gestureBoardPane, moveSelectPane)
						AnchorPane.setAnchors(gestureBoardPane, 0, 0, 0, 0)
						AnchorPane.setBottomAnchor(moveSelectPane, 0)
						AnchorPane.setLeftAnchor(moveSelectPane, 0)
						AnchorPane.setRightAnchor(moveSelectPane, 0)
					},
					travelLogPane)
				vgrow = Priority.Always
			})
		}, 1000, 700)


	def fadeIn(n: Node): Unit = {
		new ParallelTransition(n, Seq(
			new ScaleTransition(0.5 s, n) {
				fromX = 0.75
				fromY <== fromX
				toX = 1
				toY <== toX
			}, new FadeTransition(0.5 s, n) {
				fromValue = 0
				toValue = 1
			})) {
			interpolator = Interpolator.EaseBoth
		} play()
	}

}
