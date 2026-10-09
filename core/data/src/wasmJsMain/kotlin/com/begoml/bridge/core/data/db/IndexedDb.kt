package com.begoml.bridge.core.data.db

import kotlin.js.Promise
import kotlinx.coroutines.await

/**
 * The slice of IndexedDB this app uses, as suspending calls.
 *
 * Hand-written interop rather than a wrapper library: what is needed here is three calls wide —
 * open a database, read a key, write a key — and a dependency covering the rest of the API would
 * be the larger thing to keep working. Each call hands back a `Promise`, which is the one shape
 * that crosses the Wasm boundary cleanly; a Kotlin lambda does not.
 *
 * Values are JSON strings. IndexedDB can hold structured clones, but a structured clone of a
 * Kotlin/Wasm object is not something the platform can make, and serialising at the boundary is
 * what this data already does on the wire.
 */
internal class IndexedDb private constructor(private val name: String) {

    suspend fun read(store: String, key: String): String? =
        idbRead(name, store, key).await<JsString?>()?.toString()

    suspend fun write(store: String, key: String, value: String) {
        idbWrite(name, store, key, value).await<JsAny?>()
    }

    companion object {

        /**
         * Opens the database and creates any store that is missing.
         *
         * The handle is kept on the JS side under the database name: a second `indexedDB.open`
         * would be a second connection, and a write through one is invisible to the other until
         * it commits.
         */
        suspend fun open(name: String): IndexedDb {
            idbOpen(name, Version, Stores.all.joinToString(",")).await<JsAny?>()
            return IndexedDb(name)
        }

        private const val Version = 1
    }
}

/** Every object store this app opens. One missing here cannot be used after the upgrade. */
internal object Stores {
    const val Club = "club"
    const val Venue = "venue"
    const val Player = "player"
    const val Season = "season"
    const val Freshness = "freshness"

    val all = listOf(Club, Venue, Player, Season, Freshness)
}

// The three below read their parameters inside the JavaScript body, which static analysis of the
// Kotlin cannot see; to it every one of them is dead.
@Suppress("UnusedParameter")
private fun idbOpen(name: String, version: Int, stores: String): Promise<JsAny?> = js(
    """
    new Promise((resolve, reject) => {
        const request = indexedDB.open(name, version);
        request.onupgradeneeded = () => {
            const db = request.result;
            for (const store of stores.split(',')) {
                if (!db.objectStoreNames.contains(store)) db.createObjectStore(store);
            }
        };
        request.onsuccess = () => {
            globalThis.__bridgeDb = globalThis.__bridgeDb || {};
            globalThis.__bridgeDb[name] = request.result;
            resolve(null);
        };
        request.onerror = () => reject(request.error);
    })
    """
)

@Suppress("UnusedParameter")
private fun idbRead(name: String, store: String, key: String): Promise<JsString?> = js(
    """
    new Promise((resolve, reject) => {
        const db = globalThis.__bridgeDb[name];
        const request = db.transaction(store, 'readonly').objectStore(store).get(key);
        request.onsuccess = () => resolve(request.result === undefined ? null : request.result);
        request.onerror = () => reject(request.error);
    })
    """
)

@Suppress("UnusedParameter")
private fun idbWrite(name: String, store: String, key: String, value: String): Promise<JsAny?> = js(
    """
    new Promise((resolve, reject) => {
        const db = globalThis.__bridgeDb[name];
        const tx = db.transaction(store, 'readwrite');
        tx.objectStore(store).put(value, key);
        tx.oncomplete = () => resolve(null);
        tx.onerror = () => reject(tx.error);
    })
    """
)
