package kz.ilyasmukhlis.spotlights.spotlights

/**
 * Applies Actions for several Views
 */
inline fun <T> applyAction(vararg views: T, action: T.() -> Unit) {
    for (v in views) {
        action.invoke(v)
    }
}