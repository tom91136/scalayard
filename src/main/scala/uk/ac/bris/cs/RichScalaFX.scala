package uk.ac.bris.cs

import javafx.scene.{Node, Parent}

import scalafx.beans.value.ObservableValue
import scalafx.event.subscriptions.Subscription

object RichScalaFX {

	implicit class propertyToOption[T, J](value: ObservableValue[T, J]) {
		def onChangeOption(op: Option[J] => Unit): Subscription = {
			value.onChange { (_, _, n) => op(Option(n)) }
		}

		def onChangeOptions(op: ((Option[J], Option[J])) => Unit): Subscription = {
			value.onChange { (_, p, n) => op((Option(p), Option(n))) }
		}

	}

	def findOrMkUnsafe[T <: Node, P <: Parent](p: P, id: String)
											  (an: (P, T) => Unit)(mk: => T): T =
		Option(p.lookup(s"#$id")) match {
			case Some(v) => v.asInstanceOf[T]
			case None    =>
				val n = mk
				n.setId(id)
				an(p, n)
				n
		}

}
