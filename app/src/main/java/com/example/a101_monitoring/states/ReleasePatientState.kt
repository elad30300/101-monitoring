package com.example.a101_monitoring.states

open class ReleasePatientState : State()

class ReleasePatientNotWorkingState : ReleasePatientState()

class ReleasePatientWorkingState : ReleasePatientState()

class ReleasePatientDoneState : ReleasePatientState()

class ReleasePatientFailedState : ReleasePatientState()