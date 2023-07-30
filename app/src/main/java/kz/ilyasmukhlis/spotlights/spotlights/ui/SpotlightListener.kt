package kz.ilyasmukhlis.spotlights.spotlights.ui

interface SpotlightListener {
    fun onNext()
    fun onPrevious()
    fun onComplete()
}