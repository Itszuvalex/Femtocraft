package com.itszuvalex.femtocraft

/**
  * Created by Christopher Harris (Itszuvalex) on 12/17/15.
  *
  * This package is used to model graph behaviors in an abstract, non-minecraftian form.
  * As such, this will probably get moved to ItszuLib in the near future (if it isn't already.)
  *
  * A word on terminology and duplication concerns.
  *
  * Parents - Incoming connections (on a directional graph.)
  * Children - Outgoing connections (on a directional graph.)
  *
  * Single - Represents 0 or 1 connections.
  * Many - Represents 0 to Infinite connections.
  * Obviously, I can't easily enforce a 1 to Infinite connection.
  *
  * Usage-
  *
  * There is no real implementation difference between Parents and Children, and what the actual 'direction' is.  Instead,
  * refer primarily to reason #1 for choosing which trait to inherit for cases where direction doesn't 'matter'.
  *
  * There are 2 main reasons for the distinct separation:
  *
  * 1. Clear caller responsibilities.
  *       Parents are responsible for informing children of their new parentage.
  *
  * 2. Method naming conflicts
  *       Allow for classes to inherit both a parent and child trait without having conflicting method names.
  *
  * Bi-directional (or, alternatively, non-directional) graphs are easily enforced if you have full control over both Nodes.
  * Simply mirror parent and child addition/subtraction.
  *
  */
package object graph {

}
