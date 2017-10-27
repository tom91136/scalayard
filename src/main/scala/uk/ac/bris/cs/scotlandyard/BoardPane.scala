package uk.ac.bris.cs.scotlandyard

import uk.ac.bris.cs.scotlandyard.ScotlandYard.{Location, Position}

import scalafx.beans.property.ReadOnlyDoubleProperty
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{Pane, Region}
import scalafx.Includes._

class BoardPane(val map: Image,
				val positionMap: Map[Location, Position]) extends Pane {


	val selections = new Pane() {

	}
	children = Seq(new ImageView(map), selections)


	Seq(selections, this).foreach { n => BoardPane.bindDimension(n, map.width, map.height) }


}
object BoardPane {


	private def bindDimension(region: Region,
							  width: ReadOnlyDoubleProperty,
							  height: ReadOnlyDoubleProperty): Unit = {
		region.minWidth <== width
		region.minHeight <== height
		region.maxWidth <== region.minWidth
		region.maxHeight <== region.minHeight
	}

}