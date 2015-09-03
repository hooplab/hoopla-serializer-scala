package no.hoopla.serializer

import org.scalatest.FlatSpec
import org.scalatest.Inside
import org.scalatest.Inspectors
import org.scalatest.Matchers
import org.scalatest.OptionValues


abstract class UnitSpec extends FlatSpec with Matchers with OptionValues with Inside with Inspectors
