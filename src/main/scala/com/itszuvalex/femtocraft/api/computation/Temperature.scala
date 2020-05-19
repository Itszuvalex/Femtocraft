package com.itszuvalex.femtocraft.api.computation

/**
 * Measurement of the heat energy in an object.
 * Dependent on factors such as mass, a material's heat capacity, and the measure of energy in said object.
 */
trait Temperature {
  /**
   * Kelvin has a minimum of absolute 0.
   *
   * @return Kelvin measurement of this temperature.
   */
  def kelvin: Double

  /**
   * Fahrenheit is useful as it represents a more human-sensitive scale.
   * 0^o^F is very cold, whereas 100^o^F is very hot.
   *
   * @return Fahrenheit measurement of this temperature.
   */
  def fahrenheit: Double

  /**
   * Celsius is an offset from Kelvin, slid to correspond to the states of water.
   * 0^o^C is solid water, whereas 100^o^C is boiling water.
   *
   * @return Celsius measurement of this temperature.
   */
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
