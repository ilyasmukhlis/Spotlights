package kz.ilyasmukhlis.spotlights.spotlights.data

import android.view.View
import kz.ilyasmukhlis.spotlights.spotlights.model.SpotlightItem
import kz.ilyasmukhlis.spotlights.spotlights.model.SpotlightsUI

interface UISpotlights {
    /**
     * Interface to Delegate Creating Views
     * @param spotlights - spotlight data
     * @param rootView - root layout of the screen
     */
    fun getSpotlights(
        spotlights: SpotlightsUI,
        rootView: View
    ): ArrayList<SpotlightItem>
}

class UISpotlightsDelegate: UISpotlights {

    override fun getSpotlights(spotlights: SpotlightsUI, rootView: View) = ArrayList<SpotlightItem>().apply {
       spotlights.content.forEach {
           add(
               SpotlightItem(
                   rootView.findViewWithTag(it.code),
                   title = it.title,
                   text = it.description
               )
           )
       }
    }
}
