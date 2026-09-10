package com.example.core.data.manager.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher

/**
 * Provides injectable coroutine dispatchers so the data layer never references
 * [Dispatchers] directly. ViewModels do not receive this: they stay on
 * `viewModelScope` and all threading concerns live in the data layer.
 */
interface DispatcherManager {
    val default: CoroutineDispatcher
    val main: MainCoroutineDispatcher
    val io: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

/**
 * Default [DispatcherManager] backed by the standard [Dispatchers].
 */
class DispatcherManagerImpl : DispatcherManager {
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val main: MainCoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}
