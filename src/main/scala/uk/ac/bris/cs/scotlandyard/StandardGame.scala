package uk.ac.bris.cs.scotlandyard

import uk.ac.bris.cs.scotlandyard.ScotlandYard.Location._
import uk.ac.bris.cs.scotlandyard.ScotlandYard._

object StandardGame {

	final val Rounds: Seq[Visibility] = Seq(
		Hidden, Hidden, Shown, Hidden, Hidden, Hidden, Hidden, Shown,
		Hidden, Hidden, Hidden, Hidden, Shown, Hidden, Hidden, Hidden,
		Hidden, Shown, Hidden, Hidden, Hidden, Hidden, Hidden, Shown,
	)

	final val DetectiveLocations: Seq[Location] = Seq(
		26, 29, 50, 53, 91, 94, 103, 112, 117, 123, 138, 141, 155, 174
	).map {@!}


	final val MrXLocations: Seq[Location] = Seq(
		5, 45, 51, 71, 78, 104, 106, 127, 132, 166, 170, 172
	).map {@!}

	def mkDefaultDetectiveTickets(): Tickets = Tickets(Map(
		TaxiTicket -> Amount(4),
		BusTicket -> Amount(8),
		UndergroundTicket -> Amount(11),
		SecretTicket -> Amount(0),
		DoubleTicket -> Amount(0),
	))

	def mkDefaultMrXTickets(): Tickets = Tickets(Map(
		TaxiTicket -> Amount(4),
		BusTicket -> Amount(3),
		UndergroundTicket -> Amount(3),
		SecretTicket -> Amount(5),
		DoubleTicket -> Amount(2),
	))


}
