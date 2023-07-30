package kz.ilyasmukhlis.spotlights

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kz.ilyasmukhlis.spotlights.spotlights.data.UISpotlights
import kz.ilyasmukhlis.spotlights.spotlights.data.UISpotlightsDelegate
import kz.ilyasmukhlis.spotlights.spotlights.model.SpotlightItem
import kz.ilyasmukhlis.spotlights.spotlights.model.SpotlightUI
import kz.ilyasmukhlis.spotlights.spotlights.model.SpotlightsUI

class MainViewModel : ViewModel(),
    UISpotlights by UISpotlightsDelegate() {

    private val _spotlightItems = MutableLiveData<Pair<ArrayList<SpotlightItem>, String>?>()
    val spotlightItem: LiveData<Pair<ArrayList<SpotlightItem>, String>?>
        get() = _spotlightItems

    val mock = SpotlightUI(
        code = "test",
        title = "Just For Test",
        description = "showing some test examples",
        sort = 1
    )

    val mock2 = SpotlightUI(
        code = "mock",
        title = "Just For Fun",
        description = "showing some more",
        sort = 1
    )


    fun showSpotlights(root: View, fragmentNumber: Int) {
        viewModelScope.launch {
            delay(500L)

            if (fragmentNumber == 1) {
                _spotlightItems.value = getSpotlights(
                    spotlights = SpotlightsUI(
                        listOf(mock, mock2)
                    ),
                    rootView = root
                ) to "First"
            } else {
                _spotlightItems.value = getSpotlights(
                    spotlights = SpotlightsUI(
                        listOf(mock2)
                    ),
                    rootView = root
                ) to "Second"
            }
        }

    }

    fun onCompletion() {
        _spotlightItems.value = null
    }
}