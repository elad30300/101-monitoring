package com.example.a101_monitoring.states

open class SubmitSensorToPatientState : State()

class SubmitSensorToPatientNotWorkingState : SubmitSensorToPatientState()

class SubmitSensorToPatientWorkingState : SubmitSensorToPatientState()

class SubmitSensorToPatientDoneState : SubmitSensorToPatientState()

class SubmitSensorToPatientFailedState : SubmitSensorToPatientState()