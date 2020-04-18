package com.example.a101_monitoring.states

open class SignInPatientState : State()

class SignInPatientNotWorkingState : SignInPatientState()

class SignInPatientWorkingState : SignInPatientState()

class SignInPatientDoneState : SignInPatientState()

class SignInPatientFailedState : SignInPatientState()