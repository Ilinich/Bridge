# tessera

A state-holder framework for Compose Multiplatform. Pure Kotlin and coroutines — no Compose in the
core types, no platform APIs, no dependency on the rest of this app.

## Which holder to reach for

| | `UiStateDelegate` | `feature()` |
|---|---|---|
| State comes from | one source | several, folded together |
| Intents | ordinary methods | a sealed `Action` type through a channel |
| Use it for | a screen backed by one repository, a dialog | a screen that composes independent sources |

Start with `UiStateDelegate`. Move to `feature()` when a screen has to combine sources, or when
you want intents to arrive as data you can log and replay rather than as method calls.

## A feature

```kotlin
class SquadModel(scope: CoroutineScope, repository: SquadRepository) :
    SimpleFeature<SquadState, SquadAction, SquadEvent> by feature(SquadState(), scope) {

    init {
        composeState(scope, repository.squad) { state, players -> state.copy(players = players) }

        awaitActionsIn(scope) { action ->
            when (action) {
                is SquadAction.PlayerClicked -> emitEvent(SquadEvent.OpenPlayer(action.id))
            }
        }
    }
}
```

```kotlin
@Composable
fun SquadScreen(model: SquadModel, onOpenPlayer: (String) -> Unit) {
    val state by model.collectState()

    model.CollectEventEffect { event ->
        when (event) {
            is SquadEvent.OpenPlayer -> onOpenPlayer(event.id)
        }
    }

    SquadContent(state = state, onPlayerClick = { model.dispatchAction(SquadAction.PlayerClicked(it)) })
}
```

## Three decisions worth knowing about

**`composeState` accepts `StateFlow`, never a bare `Flow`.** `combine` emits nothing until every
source has produced a value, so one silent source leaves the screen blank forever with no error to
show for it. A `StateFlow` always holds a value, which makes that failure unrepresentable. To fold
a cold flow, seed it with `withInitial(scope, initial)` and answer explicitly what the screen shows
before the source speaks.

**`UiStateDelegate` can be read by anyone and written by nobody.** The mutating functions are
member extensions on the delegate, so they resolve only inside the class that mixes it in with
`by`. A screen handed the holder can observe it and cannot change it — enforced by resolution
rules rather than by convention.

**Internal state costs no recomposition.** `internalStateFeature` carries a second state that
drives decisions but is never rendered. Anything the UI reads belongs in the rendered state
instead.

## Not here

Plugins, persistence and code generation. This is the part of the idea that earns its keep in an
app this size; the rest would be ceremony.
