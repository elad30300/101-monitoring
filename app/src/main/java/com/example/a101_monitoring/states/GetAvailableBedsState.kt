package com.example.a101_monitoring.states

open class GetAvailableBedsState : State()

class GetAvailableBedsNotWorkingState : GetAvailableBedsState()

class GetAvailableBedsWorkingState : GetAvailableBedsState()

class GetAvailableBedsDoneState : GetAvailableBedsState()

class GetAvailableBedsFailedState : GetAvailableBedsState()