package kz.ilyasmukhlis.spotlights.spotlights.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kz.ilyasmukhlis.spotlights.R
import kz.ilyasmukhlis.spotlights.spotlights.model.SpotlightItem

class SpotlightsDialog : DialogFragment() {

    var onCompletion: (() -> Unit)? = null

    private val DELAY_SCROLLING = 350
    private val TAG: String = SpotlightsDialog::class.java.simpleName

    private var tutorsList: ArrayList<SpotlightItem>? = null
    private var currentTutorIndex = -1

    private var hasViewGroupHandled = false
    private var mFragmentManager: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Tooltip)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = SpotlightLayout(requireActivity())
        initViews(view as SpotlightLayout)
        return view
    }

    override fun onDestroyView() {
        val layout: SpotlightLayout = this@SpotlightsDialog.view as SpotlightLayout
        layout.closeTutorial()
        super.onDestroyView()
    }

    private fun initViews(view: SpotlightLayout) {
        view.setTooltipListener(object : SpotlightListener {
            override
            fun onPrevious() {
                previous()
            }

            override
            fun onNext() {
                next()
            }

            override
            fun onComplete() {
                onCompletion?.invoke()
                this@SpotlightsDialog.close()
            }
        })
    }

    operator fun next() {
        if (currentTutorIndex + 1 >= tutorsList!!.size) {
            close()
        } else {
            this@SpotlightsDialog.show(
                activity,
                mFragmentManager!!,
                tutorsList!!,
                currentTutorIndex + 1
            )

        }
    }

    fun previous() {
        if (currentTutorIndex - 1 < 0) {
            currentTutorIndex = 0
        } else {
            if (tutorsList != null) {
                this@SpotlightsDialog.show(
                    activity,
                    mFragmentManager!!,
                    tutorsList!!,
                    currentTutorIndex - 1
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val window: Window? = dialog!!.window
        if (window != null) {
            window.setDimAmount(0f)
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    fun show(
        activity: Activity?,
        fm: FragmentManager,
        tutorList: ArrayList<SpotlightItem>
    ) {
        mFragmentManager = fm
        show(activity, fm, tutorList, 0)
    }

    private fun show(
        activity: Activity?,
        fm: FragmentManager,
        tutorList: ArrayList<SpotlightItem>,
        index: Int,
        onStep: ((Int) -> Unit)? = null
    ) {
        if (activity == null || activity.isFinishing) {
            return
        }

        var indexToShow = index

        try {
            tutorsList = tutorList

            if (indexToShow < 0 || indexToShow >= tutorList.size) {
                indexToShow = 0
            }

            currentTutorIndex = indexToShow
            hasViewGroupHandled = false

            onStep?.invoke(currentTutorIndex)

            if (currentTutorIndex == tutorList.lastIndex + 1) {
                hasViewGroupHandled = true
            }


            // has been handled by listener
            if (hasViewGroupHandled) {
                return
            }

            val spotlightObject: SpotlightItem = tutorList[currentTutorIndex]
            val viewGroup: ViewGroup? = spotlightObject.scrollView
            if (viewGroup != null) {
                val viewToFocus: View? = spotlightObject.view
                hasViewGroupHandled = if (viewToFocus != null) {
                    hideLayout()
                    viewGroup.post {
                        if (viewGroup is ScrollView) {
                            val relativeLocation = IntArray(2)
                            getRelativePositionRec(
                                viewToFocus,
                                viewGroup,
                                relativeLocation
                            )
                            viewGroup.smoothScrollTo(0, relativeLocation[1])
                            viewGroup.postDelayed(
                                {
                                    showLayout(activity, fm, spotlightObject)
                                },
                                DELAY_SCROLLING.toLong()
                            )
                        } else if (viewGroup is NestedScrollView) {
                            val relativeLocation = IntArray(2)
                            getRelativePositionRec(
                                viewToFocus,
                                viewGroup,
                                relativeLocation
                            )
                            viewGroup.smoothScrollTo(0, relativeLocation[1])
                            viewGroup.postDelayed(
                                {
                                    showLayout(activity, fm, spotlightObject)
                                },
                                DELAY_SCROLLING.toLong()
                            )
                        }
                    }
                    true
                } else {
                    false
                }
            }
            if (!hasViewGroupHandled) {
                showLayout(activity, fm, tutorsList!![currentTutorIndex])
            }
        } catch (e: Exception) {
            // to Handle the unknown exception.
            // Since this only for first guide, if any error appears, just don't show the guide
            Log.e(TAG, e.stackTraceToString())
            try {
                this@SpotlightsDialog.dismiss()
            } catch (e2: Exception) {
                // no op
                Log.e(TAG, e2.stackTraceToString())
            }
        }
    }

    private fun showLayout(activity: Activity?, fm: FragmentManager?, spotlightObject: SpotlightItem) {
        if (activity == null || activity.isFinishing) {
            return
        }

        if (!isVisible) {
            try {
                if (fm != null) {
                    if (!isAdded) {
                        if (fm.findFragmentByTag(TAG) == null) {
                            show(fm, TAG)
                        }
                    } else if (isHidden) {
                        val ft: FragmentTransaction = fm.beginTransaction()
                        ft.show(this@SpotlightsDialog)
                        ft.commit()
                    }
                }
            } catch (e: IllegalStateException) {
                // called in illegal state. just return.
                return
            }
        }
        val view: View? = spotlightObject.view
        val coupleViews: Pair<View?, View?>? = spotlightObject.multipleViews
        val title: String? = spotlightObject.title
        val text: String? = spotlightObject.text
        val spotlightContentPosition = spotlightObject.spotlightContentPosition ?: SpotlightContentPosition.UNDEFINED
        val tintBackgroundColor: Int = spotlightObject.tintBackgroundColor
        val spotlightArrowPosition = spotlightObject.spotlightArrowPosition ?: SpotlightArrowPosition.UNDEFINED

        when {
            view != null -> {
                view.post(Runnable {
                    layoutShowTutorial(
                        view = view,
                        title = title,
                        text = text,
                        showCaseContentPosition = spotlightContentPosition,
                        tintBackgroundColor = tintBackgroundColor,
                        spotlightArrowPosition = spotlightArrowPosition
                    )
                })
            }
            coupleViews != null -> {
                coupleViews.first?.post {
                    layoutShowTutorial(
                        view = null,
                        coupleViews = coupleViews,
                        title = title,
                        text = text,
                        showCaseContentPosition = spotlightContentPosition,
                        tintBackgroundColor = tintBackgroundColor,
                        spotlightArrowPosition = spotlightArrowPosition
                    )
                }
            }
            else -> {
                layoutShowTutorial(
                    view = null,
                    title = title,
                    text = text,
                    showCaseContentPosition = spotlightContentPosition,
                    tintBackgroundColor = tintBackgroundColor,
                    spotlightArrowPosition = spotlightArrowPosition
                )
            }
        }
    }

    fun hideLayout() {
        val layout: SpotlightLayout = this@SpotlightsDialog.view as SpotlightLayout
        layout.hideTutorial()
    }

    private fun layoutShowTutorial(
        view: View? = null,
        coupleViews: Pair<View?, View?>? = null,
        title: String?,
        text: String?,
        showCaseContentPosition: SpotlightContentPosition,
        tintBackgroundColor: Int,
        spotlightArrowPosition: SpotlightArrowPosition
    ) {
        try {
            val layout: SpotlightLayout = this@SpotlightsDialog.view as SpotlightLayout
            layout.showTutorial(
                view, coupleViews, title, text, currentTutorIndex, tutorsList!!.size,
                showCaseContentPosition, tintBackgroundColor, spotlightArrowPosition
            )
        } catch (t: Throwable) {
            // do nothing
        }
    }

    fun close() {
        try {
            dismiss()
            val layout: SpotlightLayout = this@SpotlightsDialog.view as SpotlightLayout
            layout.closeTutorial()
        } catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())
        }
    }

    private fun getRelativePositionRec(myView: View, root: ViewParent, location: IntArray) {
        if (myView.parent === root) {
            location[0] += myView.left
            location[1] += myView.top
        } else {
            location[0] += myView.left
            location[1] += myView.top
            getRelativePositionRec(myView.parent as View, root, location)
        }
    }

}