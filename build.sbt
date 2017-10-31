
lazy val scalaYard = Project(
	id = "scalayard",
	base = file("."))
	.settings(
		name := "scalayard",
		version := "0.1.0-SNAPSHOT",
		scalaVersion := "2.12.4",
		scalacOptions ++= Seq(
			"-target:jvm-1.8",
			"-encoding", "UTF-8",
			"-unchecked",
			"-deprecation",
			"-Xfuture",
			"-Yno-adapted-args",
			"-Ywarn-dead-code",
			"-Ywarn-numeric-widen",
			"-Ywarn-value-discard",
			"-Ywarn-unused",
			//		"-Xfatal-warnings"
			//		"-Xlog-implicits"
		),
		javacOptions ++= Seq(
			"-target", "1.8",
			"-source", "1.8",
			"-Xlint:deprecation"),
		resolvers ++= Seq(
			Resolver.mavenLocal,
			Resolver.jcenterRepo,
			"Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
		),
		libraryDependencies ++= Seq(
			"org.scalafx" %% "scalafx" % "8.0.102-R11",
			"org.scalafx" %% "scalafxml-core-sfx8" % "0.4",
			"org.scalafx" %% "scalafxml-macwire-sfx8" % "0.4",
			"net.kurobako.gesturefx" % "gesturefx" % "0.2.0",
		),
	)