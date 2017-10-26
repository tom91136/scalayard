package uk.ac.bris.cs

import uk.ac.bris.cs.scotlandyard.{ScotlandYard, StandardBoard}
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


	def handleDetectiveRound(round: DetectiveRound) = ???
	def handleMrXRound(round: MrXSelect) = {
		val MrXSelect(board, next) = round
		val mvs = board.computePossibleMoves()
		val move: MrXMove = ???


		next(move) match {
			case DetectiveSelect(board, next) =>
			case MrXVictory(board)            =>
			case DetectiveVictory(board)      =>
		}
	}

	def handleMrXVictory(board: Board) = ???
	def handleDetectiveVictory(board: Board) = ???


	ScotlandYard.startGame(StandardBoard(null,
		mkDefaultRounds(),
		MrX(Location(14), mkDefaultMrXTickets()),
		Detective(Red, Location(14), mkDefaultDetectiveTickets()) :: Nil
	)) match {
		case s@MrXSelect(_, _)       => handleMrXRound(s)
		case MrXVictory(board)       => handleMrXVictory(board)
		case DetectiveVictory(board) => handleDetectiveVictory(board)
	}

}
