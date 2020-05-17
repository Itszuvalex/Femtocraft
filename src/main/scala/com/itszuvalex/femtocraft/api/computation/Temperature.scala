package com.itszuvalex.femtocraft.api.computation

trait Temperature {
  def kelvin: Double

  def fahrenheit: Double

  def celsius: Double

  def toKelvin: TemperatureKelvin = TemperatureKelvin(kelvin)

  def toCelsius: TemperatureCelsius = TemperatureCelsius(celsius)

  def toFahrenheit: TemperatureFahrenheit = TemperatureFahrenheit(fahrenheit)
}

object Temperature {
  val CELSIUS_TO_KELVIN          = 273.15d
  val CELSIUS_TO_FAHRENHEIT_MULT = 1.8000d
  val CELSIUS_TO_FAHRENHEIT_ADD  = 32.00d

  def kelvin(tempK: Double): TemperatureKelvin = TemperatureKelvin(tempK)

  def celsius(tempC: Double): TemperatureCelsius = TemperatureCelsius(tempC)

  def fahrenheit(tempF: Double): TemperatureFahrenheit = TemperatureFahrenheit(tempF)
}

case class TemperatureKelvin(tempK: Double) extends Temperature {
  override def kelvin: Double = tempK

  override def fahrenheit: Double = (celsius * Temperature.CELSIUS_TO_FAHRENHEIT_MULT) + Temperature.CELSIUS_TO_FAHRENHEIT_ADD

  override def celsius: Double = tempK - Temperature.CELSIUS_TO_KELVIN
}

case class TemperatureFahrenheit(tempF: Double) extends Temperature {
  override def kelvin: Double = celsius + Temperature.CELSIUS_TO_KELVIN

  override def fahrenheit: Double = tempF

  override def celsius: Double = (fahrenheit - Temperature.CELSIUS_TO_FAHRENHEIT_ADD) / Temperature.CELSIUS_TO_FAHRENHEIT_MULT
}

case class TemperatureCelsius(tempC: Double) extends Temperature {
  override def kelvin: Double = tempC + Temperature.CELSIUS_TO_KELVIN

  override def fahrenheit: Double = (tempC * Temperature.CELSIUS_TO_FAHRENHEIT_MULT) + Temperature.CELSIUS_TO_FAHRENHEIT_ADD

  override def celsius: Double = tempC
}
