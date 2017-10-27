package uk.ac.bris.cs

import net.kurobako.gesturefx.GesturePane
import uk.ac.bris.cs.scotlandyard.{BoardPane, ScotlandYard, StandardBoard, StandardGame}
import uk.ac.bris.cs.scotlandyard.ScotlandYard._

import scala.io.Source
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.beans.property.ReadOnlyDoubleProperty
import scalafx.scene.Scene
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, Pane, Region}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

object Main extends JFXApp {


	private final val Map         : Image                     = new Image("map.png", false)
	private final val Graph       : Graph                     = readGraph(Source.fromResource("graph.txt")).get
	private final val MapLocations: Map[Location, (Int, Int)] = readMapLocations(Source.fromResource("pos.txt")).get


	val board = new BoardPane(Map, MapLocations)


	def handleRound(round: Round) = {
		round match {
			case MrXSelect(board, next)       => println(s"MrX select, $board => ${board.computePossibleMoves().size}")
			case DetectiveSelect(board, next) => println(s"Detective select, $board => ${board.computePossibleMoves().size}")
			case MrXVictory(board)            => println(s"MrX Won, $board")
			case DetectiveVictory(board)      => println(s"Detective  Won, $board")
		}
	}

	val r = ScotlandYard.startGame(StandardBoard(
		graph = Graph,
		rounds = StandardGame.Rounds,
		mrX = MrX(Location(14), StandardGame.mkDefaultMrXTickets()),
		detectives = Detective(Red, Location(26), StandardGame.mkDefaultDetectiveTickets()) :: Nil
	))

	handleRound(r)


	stage = new JFXApp.PrimaryStage()
	stage.title = "ScalaYard"
	stage.scene = new Scene(new GesturePane(board), 800, 600)

}
