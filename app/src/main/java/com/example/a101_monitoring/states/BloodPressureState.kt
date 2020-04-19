package com.example.a101_monitoring.states

open class BloodPressureState : State()

class BloodPressureNotWorkingState : BloodPressureState()

class BloodPressureWorkingState : BloodPressureState()

class BloodPressureDoneState : BloodPressureState()

class BloodPressureFailedState : BloodPressureState()