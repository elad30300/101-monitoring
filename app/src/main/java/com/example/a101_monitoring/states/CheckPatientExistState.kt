package com.example.a101_monitoring.states

open class CheckPatientExistState : State()

class CheckPatientExistNotWorkingState : CheckPatientExistState()

class CheckPatientExistWorkingState : CheckPatientExistState()

class CheckPatientExistDoneState : CheckPatientExistState()

class CheckPatientExistFailedState : CheckPatientExistState()