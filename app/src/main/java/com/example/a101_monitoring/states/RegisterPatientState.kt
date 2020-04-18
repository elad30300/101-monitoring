package com.example.a101_monitoring.states

open class RegisterPatientState : State()

class RegisterPatientNotWorkingState : RegisterPatientState()

class RegisterPatientWorkingState : RegisterPatientState()

class RegisterPatientDoneState : RegisterPatientState()

class RegisterPatientFailedState : RegisterPatientState()