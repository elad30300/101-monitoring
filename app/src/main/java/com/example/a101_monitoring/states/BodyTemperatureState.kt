package com.example.a101_monitoring.states

open class BodyTemperatureState : State()

class BodyTemperatureNotWorkingState : BodyTemperatureState()

class BodyTemperatureWorkingState : BodyTemperatureState()

class BodyTemperatureDoneState : BodyTemperatureState()

class BodyTemepratureFailedState : BodyTemperatureState()