ScalaYard
======

A fully graphical Scala implementation of the Scotland Yard board game
 
## How to build

Prerequisites:

 * JDK 8
 * sbt 1.x

The project uses sbt for build so you will need to install the latest 1.x branch of sbt.

Clone the project and then in project root:

    sbt run
    
## Structure 

The project follows standard SBT layout, some packages of interest:

 * `uk.ac.bris.cs.scotlandyard` - contains classes that model the actual game
 * `uk.ac.bris.cs.scotlandyard.ui` - contains all UI components
 * `uk.ac.bris.cs.UndirectedGraph` - a minimal undirected graph implementation
 * `uk.ac.bris.cs.RichScalaFX` - nice implicits for ScalaFX UI components
 * `uk.ac.bris.cs.RichMap` - nice implicits for the standard `Map`
 * `uk.ac.bris.cs.Main` - the GUI's setup and main entry point

## Status

Mostly complete

TODO

 * Added unit tests
 * Better ticket selector
 * Notification of game events
 * Mr.X hiding
 * FatJar assembly

## Motivation

To convince Tilo to use Scala instead of Java for year 1 OO course

