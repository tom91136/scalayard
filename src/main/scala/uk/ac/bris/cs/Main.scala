package uk.ac.bris.cs

import uk.ac.bris.cs.scotlandyard.{ScotlandYard, StandardBoard, StandardGame}
import uk.ac.bris.cs.scotlandyard.ScotlandYard._

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

object Main extends JFXApp {


	val root = new HBox() {
		children = new Rectangle() {
			width = 200
			height = 200
			fill = Color.Red
		}
	}

	stage = new JFXApp.PrimaryStage()
	stage.title = "test"
	stage.scene = new Scene(root)


	def handleRound(round: Round) = {
		round match {
			case MrXSelect(board, next)       => println(s"MrX select, $board => ${board.computePossibleMoves().size}")
			case DetectiveSelect(board, next) => println(s"Detective select, $board => ${board.computePossibleMoves().size}")
			case MrXVictory(board)            => println(s"MrX Won, $board")
			case DetectiveVictory(board)      => println(s"Detective  Won, $board")
		}
	}


	val r = ScotlandYard.startGame(StandardBoard(readGraph(getClass.getResourceAsStream("/graph.txt")),
		StandardGame.Rounds,
		MrX(Location(14), StandardGame.mkDefaultMrXTickets()),
		Detective(Red, Location(26), StandardGame.mkDefaultDetectiveTickets()) :: Nil
	))

	handleRound(r)

}
