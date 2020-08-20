@file:Suppress("unused")

package it.cammino.risuscito.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations

/**
 * This function creates a [LiveData] of a [Pair] of the two types provided. The resulting LiveData is updated whenever either input LiveData updates and both LiveData have updated to a non-null value at least once before.
 *
 * If the zip of A and B is C, and A and B are updated in this pattern: `AABA`, C would be updated twice (once with the second A value and first B value, and once with the third A value and first B value).
 *
 * @param a the first LiveData
 * @param b the second LiveData
 * @since 0.1.0
 * @author Mitchell Skaggs
 */
fun <A, B> zipLiveData(a: LiveData<A>, b: LiveData<B>): LiveData<Pair<A, B>> {
    return MediatorLiveData<Pair<A, B>>().apply {
        var lastA: A? = null
        var lastB: B? = null

        fun update() {
            val localLastA = lastA
            val localLastB = lastB
            if (localLastA != null && localLastB != null)
                this.value = Pair(localLastA, localLastB)
        }

        addSource(a) {
            lastA = it
            update()
        }
        addSource(b) {
            lastB = it
            update()
        }
    }
}

/**
 * This function creates a [LiveData] of a [Pair] of the two types provided. The resulting LiveData is updated whenever either input LiveData updates and both LiveData have updated at least once before.
 *
 * @see zipLiveData
 * @param a the first LiveData
 * @param b the second LiveData
 * @since 0.2.0
 * @author Mitchell Skaggs
 */
fun <A, B> zipLiveDataNullable(a: LiveData<A>, b: LiveData<B>): LiveData<Pair<A?, B?>> {
    return MediatorLiveData<Pair<A?, B?>>().apply {
        var lastA: A? = null
        var hasAUpdatedOnce = false

        var lastB: B? = null
        var hasBUpdatedOnce = false

        fun update() {
            if (hasAUpdatedOnce && hasBUpdatedOnce)
                this.value = Pair(lastA, lastB)
        }

        addSource(a) {
            lastA = it
            hasAUpdatedOnce = true
            update()
        }
        addSource(b) {
            lastB = it
            hasBUpdatedOnce = true
            update()
        }
    }
}

/**
 * This is merely an extension function for [zipLiveData].
 *
 * @see zipLiveData
 * @since 0.1.0
 * @author Mitchell Skaggs
 */
infix fun <A, B> LiveData<A>.zipTo(b: LiveData<B>): LiveData<Pair<A, B>> = zipLiveData(this, b)

/**
 * This is merely an extension function for [zipLiveDataNullable].
 *
 * @see zipLiveData
 * @since 0.1.0
 * @author Mitchell Skaggs
 */
infix fun <A, B> LiveData<A>.zipToNullable(b: LiveData<B>): LiveData<Pair<A?, B?>> = zipLiveDataNullable(this, b)

/**
 * This is an extension function that calls to [Transformations.map]. If null is received, null is returned instead of calling the provided function.
 *
 * @see Transformations.map
 * @since 0.1.0
 * @author Mitchell Skaggs
 */
inline fun <A, B> LiveData<A>.map(crossinline function: (A) -> B): LiveData<B> =
        Transformations.map(this) {
            if (it == null) null else function(it)
        }

/**
 * This is an extension function that calls to [Transformations.map]. It exposes the possibilities of receiving and returning null.
 *
 * @see Transformations.map
 * @since 0.1.0
 * @author Mitchell Skaggs
 */
fun <A, B> LiveData<A>.mapNullable(function: (A?) -> B?): LiveData<B> =
        Transformations.map(this, function)

/**
 * This is an extension function that calls to [Transformations.switchMap]. If null is received, null is returned instead of calling the provided function.
 *
 * @see Transformations.switchMap
 * @since 0.1.0
 * @author Mitchell Skaggs
 */
fun <A, B> LiveData<A>.switchMap(function: (A) -> LiveData<B>): LiveData<B> =
        Transformations.switchMap(this) {
            if (it == null) null else function(it)
        }

/**
 * This is an extension function that calls to [Transformations.switchMap]. It exposes the possibilities of receiving and returning null.
 *
 * @see Transformations.switchMap
 * @since 0.1.0
 * @author Mitchell Skaggs
 */
fun <A, B> LiveData<A>.switchMapNullable(function: (A?) -> LiveData<B>?): LiveData<B> =
        Transformations.switchMap(this, function)

/**
 * This is an extension function that links two [MediatorLiveData] instances using a function [apply] and its inverse, [unapply]. Changes made to either [MediatorLiveData] are reflected in the other based on the two functions.
 *
 * @param apply A function from [A] to [B]
 * @param unapply A function from [B] to [A], the inverse of [apply]
 * @since 0.1.0
 * @author Mitchell Skaggs
 */
fun <A, B> MediatorLiveData<A>.bidiMap(apply: (currentA: A, oldB: B?) -> B?, unapply: (oldA: A, currentB: B) -> A?): MediatorLiveData<B> {
    return MediatorLiveData<B>().apply {
        this@apply.addSource(this@bidiMap) {
            it?.let { a ->
                this@apply.value.let { b ->
                    val newB = apply(a, b)
                    if (b != newB)
                        this@apply.value = newB
                }
            }
        }

        this@bidiMap.addSource(this@apply) {
            it?.let { b ->
                this@bidiMap.value?.let { a ->
                    val newA = unapply(a, b)
                    if (a != newA)
                        this@bidiMap.value = newA
                }
            }
        }
    }
}