package com.begoml.bridge.foundation.tessera

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * The scope a state holder and whatever owns it share.
 *
 * Created outside both so neither decides the other's lifetime: the owner cancels it once and
 * everything started on it ends together. A supervisor job because one failed collector must not
 * take the rest of a screen down with it.
 *
 * The main dispatcher is named here, and only here, for the same reason `viewModelScope` names it:
 * state reaches the UI, so the default place to resume is the thread that draws. Work that must
 * not run there takes its own dispatcher as a parameter.
 */
@Suppress("InjectDispatcher")
fun stateHolderScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
