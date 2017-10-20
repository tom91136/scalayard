package uk.ac.bris.cs

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


}
