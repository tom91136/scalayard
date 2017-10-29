package uk.ac.bris.cs

object RichMap {

	implicit class WithAdjust[K, V](m: Map[K, V]) {
		def adjust(k: K)(f: V => V): Map[K, V] = m.updated(k, f(m(k)))
	}

}
