package kz.ilyasmukhlis.spotlights.spotlights.model

import android.view.View
import android.view.ViewGroup
import kz.ilyasmukhlis.spotlights.spotlights.ui.SpotlightArrowPosition
import kz.ilyasmukhlis.spotlights.spotlights.ui.SpotlightContentPosition

/**
 * Data Classes for SpotlightItems
 */
class SpotlightItem(
    val view: View? = null,
    val multipleViews: Pair<View?, View?>? = null,
    val spotlightContentPosition: SpotlightContentPosition? = SpotlightContentPosition.UNDEFINED,
    val spotlightArrowPosition: SpotlightArrowPosition? = SpotlightArrowPosition.UNDEFINED,
    val title: String?,
    val text: String?,
    val tintBackgroundColor: Int = 0,
    val scrollView: ViewGroup? = null
)


data class SpotlightUI(
    val code: String,
    val title: String,
    val description: String,
    val sort: Int
)

data class SpotlightsUI(
    val content: List<SpotlightUI>
)