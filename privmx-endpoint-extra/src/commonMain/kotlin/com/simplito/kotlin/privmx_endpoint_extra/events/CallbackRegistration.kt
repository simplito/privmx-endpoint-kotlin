package com.simplito.kotlin.privmx_endpoint_extra.events

class CallbackRegistration<T: Any>(
    var callbackGroup: Any,
    var eventType: EventType<T>,
    var callback: EventCallback<T>
)