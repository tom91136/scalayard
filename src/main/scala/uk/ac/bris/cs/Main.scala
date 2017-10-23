package uk.ac.bris.cs

import uk.ac.bris.cs.scotlandyard.ScotlandYard
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
			val move : MrXMove = ???




			next(move) match {
				case DetectiveSelect(board, next) =>
				case MrXVictory(board)            =>
				case DetectiveVictory(board)      =>
			}
	}

	def handleMrXVictory(board: Board) = ???
	def handleDetectiveVictory(board: Board) = ???

	ScotlandYard.startGame(null) match {
		case s@MrXSelect(_, _)       => handleMrXRound(s)
		case MrXVictory(board)       => handleMrXVictory(board)
		case DetectiveVictory(board) => handleDetectiveVictory(board)
	}

	ScotlandYard.startGame(null) match {
		case MrXSelect(board, next)  => next(null: DoubleMove) match {
			case DetectiveSelect(board, next) => next(null: DetectiveMove) match {
				case DetectiveSelect(board, next) =>
				case MrXSelect(board, next)       =>
				case MrXVictory(board)            =>
				case DetectiveVictory(board)      =>
			}
			case MrXVictory(board)            =>
			case DetectiveVictory(board)      =>
		}
		case MrXVictory(board)       =>
		case DetectiveVictory(board) =>
	}


}
