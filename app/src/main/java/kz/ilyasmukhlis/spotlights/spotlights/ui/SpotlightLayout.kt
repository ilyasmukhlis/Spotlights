package kz.ilyasmukhlis.spotlights.spotlights.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import kz.ilyasmukhlis.spotlights.R
import kz.ilyasmukhlis.spotlights.spotlights.applyAction


class SpotlightLayout : FrameLayout {

    // customized attribute
    private var layoutRes = R.layout.tutorial_view
    private var spacing = resources.getDimension(R.dimen.dp_12).toInt()
    private var arrowMargin = spacing * 2
    private var arrowWidth = 2 * spacing

    private var isCancelable = false

    private var nextString = context.getString(R.string.next)
    private var finishString = context.getString(R.string.great)

    private var nextDrawableRes = R.drawable.ic_spotlight_next

    private var tooltipRadius = resources.getDimension(R.dimen.dp_8).toInt()

    // View
    private var viewGroup: ViewGroup? = null
    private var bitmap: Bitmap? = null
    private var lastTutorialView: View? = null
    private var viewPaint: Paint? = null

    // listener
    private var spotlightListener: SpotlightListener? = null

    private var spotlightContentPosition: SpotlightContentPosition? = null
    private var spotlightArrowPosition: SpotlightArrowPosition? = null

    private var highlightLocX = 0f
    private var highlightLocY = 0f
    private var lasthighlightLocX = 0f
    private var lasthighlightLocY = 0f

    // determined if this is last chain
    private var isStart = false
    private var isLast = false

    // нужны для прорисовки стрелки
    private var isOnTheLeftEdgeOfTheScreen = false
    private var isOnTheRightEdgeOfTheScreen = false

    // нужны для правильных марджинов
    private var isLeftEdgeOfTheScreen = false
    private var isRightEdgeOfTheScreen = false

    // path for arrow
    private var path: Path? = null
    private var arrowPaint: Paint? = null
    private var textViewTitle: TextView? = null
    private var textViewDesc: TextView? = null
    private var textViewCounter: TextView? = null
    private var prevButton: ConstraintLayout? = null
    private var nextButtonText: TextView? = null
    private var nextButton: ConstraintLayout? = null
    private var skipButton: AppCompatImageView? = null
    private var prevImageView: AppCompatImageView? = null
    private var nextImageView: AppCompatImageView? = null
    private var buttonContainer: ViewGroup? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        visibility = View.GONE
        if (isInEditMode) {
            return
        }
        //setBackground, color
        initFrame()

        // setContentView
        initContent(context)
        isClickable = isCancelable
        isFocusable = isCancelable
        if (isCancelable) {
            setOnClickListener { onNextClicked() }
        }
    }

    private fun onNextClicked() {
        if (spotlightListener != null) {
            if (isLast) {
                this@SpotlightLayout.spotlightListener!!.onComplete()
            } else {
                this@SpotlightLayout.spotlightListener!!.onNext()
            }
        }
    }

    private fun initFrame() {
        bringToFront()
        setWillNotDraw(false)
        viewPaint = Paint()
        viewPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        arrowPaint = Paint()
        arrowPaint!!.color = ContextCompat.getColor(context, R.color.colorFill2)

        setBackgroundColor(ContextCompat.getColor(context, R.color.tooltipBgColor))
    }

    fun setTooltipListener(showCaseListener: SpotlightListener?) {
        this.spotlightListener = showCaseListener
    }

    @Throws(Throwable::class)
    fun showTutorial(
        view: View?,
        coupleViews: Pair<View?, View?>? = null,
        title: String?,
        text: String?,
        currentTutorIndex: Int,
        tutorsListSize: Int,
        spotlightContentPosition: SpotlightContentPosition?,
        tintBackgroundColor: Int,
        spotlightArrowPosition: SpotlightArrowPosition
    ) {
        isStart = currentTutorIndex == 0
        isLast = currentTutorIndex == tutorsListSize - 1
        this.spotlightContentPosition = spotlightContentPosition
        this.spotlightArrowPosition = spotlightArrowPosition
        if (bitmap != null) {
            bitmap!!.recycle()
        }
        if (title?.isEmpty() == true) {
            textViewTitle!!.visibility = View.GONE
        } else {
            textViewTitle!!.text = title
            textViewTitle!!.visibility = View.VISIBLE
        }
        textViewDesc!!.text = text

        if (isStart) {
            prevButton!!.visibility = View.GONE
        } else {
            prevButton!!.visibility = View.VISIBLE
        }
        if (isLast) {
            nextButtonText!!.text = finishString
        } else if (currentTutorIndex < tutorsListSize - 1) { // has next
            nextButtonText!!.text = nextString
        }

        if (isLast) {
            nextButtonText!!.text = finishString
            nextImageView!!.visibility = View.GONE
        } else if (currentTutorIndex < tutorsListSize - 1) { // has next
            nextImageView!!.visibility = View.VISIBLE
        }

        applyAction(
            buttonContainer!!,
            textViewCounter!!
        ) {
            isVisible = tutorsListSize > 1
        }

        setCounter(currentTutorIndex, tutorsListSize)

        when {
            view != null -> {
                lastTutorialView = view

                if (tintBackgroundColor == 0) {
                    bitmap = getBitmapFromView(view)
                } else {
                    val bitmapTemp: Bitmap? = getBitmapFromView(view)
                    val bigBitmap = Bitmap.createBitmap(
                        view.measuredWidth,
                        view.measuredHeight, Bitmap.Config.ARGB_8888
                    )
                    val bigCanvas = Canvas(bigBitmap)
                    bigCanvas.drawColor(tintBackgroundColor)
                    val paint = Paint()

                    if (bitmapTemp != null) {
                        bigCanvas.drawBitmap(bitmapTemp, 0f, 0f, paint)
                    }
                    bitmap = bigBitmap
                }

                val location = IntArray(2)
                view.getLocationInWindow(location)
                highlightLocX = location[0].toFloat()
                highlightLocY = location[1].toFloat()
                val tv = TypedValue()
                if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    val actionBarHeight =
                        TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
                    highlightLocY = location[1].toFloat() - actionBarHeight
                }
                lasthighlightLocY = 0f
                lasthighlightLocX = 0f

                this.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        if (this@SpotlightLayout.bitmap != null) {
                            moveViewBasedHighlight(
                                this@SpotlightLayout.highlightLocX,
                                this@SpotlightLayout.highlightLocY,
                                this@SpotlightLayout.highlightLocX + this@SpotlightLayout.bitmap!!.width,
                                this@SpotlightLayout.highlightLocY + this@SpotlightLayout.bitmap!!.height
                            )
                            this@SpotlightLayout.viewTreeObserver
                                .removeOnGlobalLayoutListener(this)
                            invalidate()
                        }
                    }
                })
            }
            coupleViews != null -> {
                val firstView = coupleViews.first
                val lastView = coupleViews.second

                lastTutorialView = lastView

                if (firstView != null && lastView != null) {
                    val firstViewLocation = IntArray(2)
                    val lastViewLocation = IntArray(2)
                    firstView.getLocationInWindow(firstViewLocation)
                    lastView.getLocationInWindow(lastViewLocation)

                    if (tintBackgroundColor == 0) {
                        bitmap = getBitmapFromSeveralViews(coupleViews)
                    } else {
                        val bitmapTemp: Bitmap? = getBitmapFromSeveralViews(coupleViews)
                        val bigBitmap = Bitmap.createBitmap(
                            firstView.measuredWidth,
                            firstView.measuredHeight
                                    + (lastViewLocation[1] - firstViewLocation[1])
                                    + lastView.measuredHeight, Bitmap.Config.ARGB_8888
                        )
                        val bigCanvas = Canvas(bigBitmap)
                        bigCanvas.drawColor(tintBackgroundColor)
                        val paint = Paint()

                        if (bitmapTemp != null) {
                            bigCanvas.drawBitmap(bitmapTemp, 0f, 0f, paint)
                        }
                        bitmap = bigBitmap
                    }

                    highlightLocX = firstViewLocation[0].toFloat()
                    highlightLocY = firstViewLocation[1].toFloat()
                    lasthighlightLocX = lastViewLocation[0].toFloat()
                    lasthighlightLocY = lastViewLocation[1].toFloat()
                    val tv = TypedValue()
                    if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                        val actionBarHeight =
                            TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
                        highlightLocY = firstViewLocation[1].toFloat() - actionBarHeight
                        lasthighlightLocY = lastViewLocation[1].toFloat() - actionBarHeight
                    }

                    this.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            if (this@SpotlightLayout.bitmap != null) {
                                moveViewBasedHighlight(
                                    this@SpotlightLayout.highlightLocX,
                                    this@SpotlightLayout.highlightLocY,
                                    this@SpotlightLayout.highlightLocX + this@SpotlightLayout.bitmap!!.width,
                                    this@SpotlightLayout.highlightLocY + this@SpotlightLayout.bitmap!!.height
                                )
                                this@SpotlightLayout.viewTreeObserver
                                    .removeOnGlobalLayoutListener(this)
                                invalidate()
                            }
                        }
                    })

                }
            }
            else -> {
                lastTutorialView = null
                bitmap = null
                highlightLocX = 0f
                highlightLocY = 0f
                moveViewToCenter()
            }
        }

        this.visibility = View.VISIBLE
    }

    private fun setCounter(currentTutorIndex: Int, tutorsListSize: Int) {
        textViewCounter!!.text = "${currentTutorIndex + 1}/$tutorsListSize"
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun getBitmapFromSeveralViews(coupleViews: Pair<View?, View?>): Bitmap? {
        val firstView = coupleViews.first
        val lastView = coupleViews.second
        return if (firstView != null && lastView != null) {
            val firstViewLocation = IntArray(2)
            val lastViewLocation = IntArray(2)
            firstView.getLocationInWindow(firstViewLocation)
            lastView.getLocationInWindow(lastViewLocation)

            if (firstViewLocation[1] != lastViewLocation[1]) { // Vertical Recycler
                val bitmap = Bitmap.createBitmap(firstView.width, firstView.height + (lastViewLocation[1] - firstViewLocation[1]), Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                firstView.draw(canvas)
                bitmap
            } else { // Horizontal Recycler
                val bitmap = Bitmap.createBitmap(firstView.width + (lastViewLocation[0] - firstViewLocation[0]), firstView.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                firstView.draw(canvas)
                bitmap
            }

        } else null
    }

    fun hideTutorial() {
        this.visibility = View.GONE
    }

    fun closeTutorial() {
        visibility = View.GONE
        if (bitmap != null) {
            bitmap!!.recycle()
            bitmap = null
        }
        if (lastTutorialView != null) {
            lastTutorialView = null
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        recycleResources()
    }

    override fun onDraw(canvas: Canvas) {
        if (bitmap == null || bitmap!!.isRecycled) {
            return
        }
        super.onDraw(canvas)
        // Draw Spotlighted Area

        when {
            lasthighlightLocY != 0f && highlightLocY != lasthighlightLocY -> {
                canvas.drawRoundRect(
                    highlightLocX - resources.getDimension(R.dimen.dp_8),
                    highlightLocY - resources.getDimension(R.dimen.dp_12),
                    highlightLocX + lastTutorialView!!.width + resources.getDimension(R.dimen.dp_8),
                    highlightLocY + (lasthighlightLocY - highlightLocY) + lastTutorialView!!.height + resources.getDimension(R.dimen.dp_12),
                    resources.getDimension(R.dimen.dp_24),
                    resources.getDimension(R.dimen.dp_24),
                    viewPaint!!
                )
            }
            lasthighlightLocY != 0f && highlightLocY == lasthighlightLocY -> {
                canvas.drawRoundRect(
                    highlightLocX - resources.getDimension(R.dimen.dp_8),
                    highlightLocY - resources.getDimension(R.dimen.dp_12),
                    highlightLocX + (lasthighlightLocX - highlightLocX) + lastTutorialView!!.width + resources.getDimension(R.dimen.dp_8),
                    highlightLocY + lastTutorialView!!.height + resources.getDimension(R.dimen.dp_12),
                    resources.getDimension(R.dimen.dp_24),
                    resources.getDimension(R.dimen.dp_24),
                    viewPaint!!
                )
            }
            else -> {
                if (lastTutorialView!!.width == lastTutorialView!!.height) {
                    canvas.drawCircle(
                        highlightLocX + resources.getDimension(R.dimen.dp_12),
                        highlightLocY + resources.getDimension(R.dimen.dp_12),
                        resources.getDimension(R.dimen.dp_24),
                        viewPaint!!
                    )
                } else {
                    canvas.drawRoundRect(
                        highlightLocX - resources.getDimension(R.dimen.dp_8),
                        highlightLocY + resources.getDimension(R.dimen.dp_12),
                        highlightLocX + lastTutorialView!!.width + resources.getDimension(R.dimen.dp_8),
                        highlightLocY + lastTutorialView!!.height + resources.getDimension(R.dimen.dp_24),
                        resources.getDimension(R.dimen.dp_24),
                        resources.getDimension(R.dimen.dp_24),
                        viewPaint!!
                    )
                }
            }
        }

        // drawArrow
        if (path != null && arrowPaint != null && viewGroup!!.visibility == View.VISIBLE) {
            canvas.drawPath(path!!, arrowPaint!!)
        }
    }

    private fun initContent(context: Context) {
        viewGroup = LayoutInflater.from(context).inflate(layoutRes, this, false) as ViewGroup
        val viewGroupTutorContent: CardView = viewGroup!!.findViewById(R.id.view_group_tutor_content) as CardView
        viewGroupTutorContent.radius = tooltipRadius.toFloat()
        textViewTitle = viewGroupTutorContent.findViewById(R.id.text_title)
        textViewDesc = viewGroupTutorContent.findViewById(R.id.text_description)
        textViewCounter = viewGroupTutorContent.findViewById(R.id.counterTextView)
        prevButton = viewGroupTutorContent.findViewById(R.id.targetPreviousButton)
        nextButtonText = viewGroupTutorContent.findViewById(R.id.text_next)
        nextButton = viewGroupTutorContent.findViewById(R.id.targetNextButton)
        prevImageView = viewGroupTutorContent.findViewById(R.id.ic_previous)
        skipButton = viewGroupTutorContent.findViewById(R.id.skipButton)
        nextImageView = viewGroupTutorContent.findViewById(R.id.ic_next)
        buttonContainer = viewGroupTutorContent.findViewById(R.id.ll_bottom_container)

        if (prevButton != null) {
            prevButton!!.setOnClickListener {
                if (spotlightListener != null) {
                    if (this@SpotlightLayout.isStart) {
                        this@SpotlightLayout.spotlightListener!!.onComplete()
                    } else {
                        this@SpotlightLayout.spotlightListener!!.onPrevious()
                    }
                }
            }

            if (nextButton != null) {
                nextButton!!.setOnClickListener { onNextClicked() }
            }

            if (nextImageView != null) {
                if (nextButtonText != null) {
                    nextImageView!!.setImageResource(nextDrawableRes)
                    nextImageView!!.setOnClickListener { onNextClicked() }
                }
            }
        }

        skipButton?.setOnClickListener {
            this@SpotlightLayout.spotlightListener!!.onComplete()
        }

        this.addView(viewGroup)
    }

    private fun moveViewBasedHighlight(
        highlightXstart: Float,
        highlightYstart: Float,
        highlightXend: Float,
        highlightYend: Float
    ) {
        if (spotlightContentPosition === SpotlightContentPosition.UNDEFINED) {
            val widthCenter = this.width / 2
            val heightCenter = this.height / 2
            spotlightContentPosition = if (highlightYend <= heightCenter) {
                SpotlightContentPosition.BOTTOM
            } else if (highlightYstart >= heightCenter) {
                SpotlightContentPosition.TOP
            } else { // not fit anywhere
                // if bottom is bigger, put to bottom, else put it on top
                if (this.height - highlightYend > highlightYstart) {
                    SpotlightContentPosition.BOTTOM
                } else {
                    SpotlightContentPosition.TOP
                }
            }
        }
        val layoutParams: LayoutParams
        when (spotlightContentPosition) {
            SpotlightContentPosition.BOTTOM -> {
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT,
                    Gravity.TOP
                )
                layoutParams.topMargin = (highlightYend + spacing * 3).toInt()
                layoutParams.bottomMargin = 0

                isLeftEdgeOfTheScreen = highlightXstart <= resources.getDimension(R.dimen.dp_28).toInt()
                isRightEdgeOfTheScreen = highlightXend >= width - resources.getDimension(R.dimen.dp_28).toInt()
                when {
                    isLeftEdgeOfTheScreen && !isRightEdgeOfTheScreen -> {
                        layoutParams.leftMargin = resources.getDimension(R.dimen.dp_12).toInt()
                        layoutParams.rightMargin = resources.getDimension(R.dimen.dp_52).toInt()
                    }
                    isRightEdgeOfTheScreen && !isLeftEdgeOfTheScreen -> {
                        layoutParams.leftMargin = resources.getDimension(R.dimen.dp_52).toInt()
                        layoutParams.rightMargin = resources.getDimension(R.dimen.dp_12).toInt()
                    }
                    else -> {
                        layoutParams.leftMargin = resources.getDimension(R.dimen.dp_24).toInt()
                        layoutParams.rightMargin = resources.getDimension(R.dimen.dp_24).toInt()
                    }
                }

                setLayoutViewGroup(layoutParams)
                if (arrowWidth == 0) {
                    path = null
                } else {
                    val spotlightWidth = highlightXend - highlightXstart
                    val highLightCenterX = (highlightXend + highlightXstart) / 2
                    val recalcArrowWidth = getRecalculateArrowWidth(highLightCenterX.toInt(), width)

                    val safeArrowWidth = spacing + arrowWidth
                    isOnTheLeftEdgeOfTheScreen = highLightCenterX < safeArrowWidth
                    isOnTheRightEdgeOfTheScreen = highLightCenterX > (width - safeArrowWidth)

                    val bufferX: Float = if (isOnTheLeftEdgeOfTheScreen) {
                        if (highlightXend < spacing + tooltipRadius) {
                            (spacing + tooltipRadius) - highlightXend
                        } else {
                            0f
                        }
                    } else if (isOnTheRightEdgeOfTheScreen) {
                        if (highlightXstart > width - (spacing + tooltipRadius)) {
                            highlightXstart - (width - (spacing + tooltipRadius))
                        } else {
                            0f
                        }
                    } else {
                        0f
                    }

                    if (recalcArrowWidth == 0) {
                        val safeStartPoint = if (spotlightWidth > arrowWidth) {
                            if (isOnTheLeftEdgeOfTheScreen) {
                                // If on the left side, get X end point
                                highlightXend + bufferX
                            } else {
                                // If on the right side, get X starting point
                                highlightXstart - bufferX
                            }
                        } else {
                            if (isOnTheLeftEdgeOfTheScreen) {
                                highlightXend + spotlightWidth / 2 + arrowWidth / 2
                            } else {
                                highlightXstart - spotlightWidth / 2 - arrowWidth / 2
                            }
                        }

                        path = Path()
                        path!!.moveTo(safeStartPoint, highlightYend + arrowMargin + spacing / 3)
                        path!!.lineTo(
                            safeStartPoint - arrowWidth / 2,
                            highlightYend + spacing + arrowMargin
                        )
                        path!!.lineTo(
                            safeStartPoint + arrowWidth / 2,
                            highlightYend + spacing + arrowMargin
                        )
                        path!!.close()
                    } else {
                        path = Path()
                        when (spotlightArrowPosition) {
                            SpotlightArrowPosition.LEFT -> {
                                path!!.moveTo(highlightXstart + safeArrowWidth, highlightYend + arrowMargin + spacing / 3)
                                path!!.lineTo(
                                    highlightXstart + safeArrowWidth - recalcArrowWidth / 2,
                                    highlightYend + spacing + arrowMargin
                                )
                                path!!.lineTo(
                                    highlightXstart + safeArrowWidth + recalcArrowWidth / 2,
                                    highlightYend + spacing + arrowMargin
                                )
                            }

                            SpotlightArrowPosition.RIGHT -> {
                                path!!.moveTo(highlightXend - safeArrowWidth, highlightYend + arrowMargin + spacing / 3)
                                path!!.lineTo(
                                    highlightXend - safeArrowWidth - recalcArrowWidth / 2,
                                    highlightYend + spacing + arrowMargin
                                )
                                path!!.lineTo(
                                    highlightXend - safeArrowWidth + recalcArrowWidth / 2,
                                    highlightYend + spacing + arrowMargin
                                )
                            }
                            else -> {
                                path!!.moveTo(highLightCenterX, highlightYend + arrowMargin + spacing / 3)
                                path!!.lineTo(
                                    highLightCenterX - recalcArrowWidth / 2,
                                    highlightYend + spacing + arrowMargin
                                )
                                path!!.lineTo(
                                    highLightCenterX + recalcArrowWidth / 2,
                                    highlightYend + spacing + arrowMargin
                                )
                            }
                        }
                        path!!.close()

                    }
                }
            }
            SpotlightContentPosition.TOP -> {
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
                )
                layoutParams.bottomMargin = (height - highlightYstart + spacing + spacing).toInt()
                layoutParams.topMargin = 0

                isLeftEdgeOfTheScreen = highlightXstart <= resources.getDimension(R.dimen.dp_28).toInt()
                isRightEdgeOfTheScreen = highlightXend >= width - resources.getDimension(R.dimen.dp_28).toInt()
                when {
                    isLeftEdgeOfTheScreen && !isRightEdgeOfTheScreen -> {
                        layoutParams.leftMargin = resources.getDimension(R.dimen.dp_12).toInt()
                        layoutParams.rightMargin = resources.getDimension(R.dimen.dp_52).toInt()
                    }
                    isRightEdgeOfTheScreen && !isLeftEdgeOfTheScreen -> {
                        layoutParams.leftMargin = resources.getDimension(R.dimen.dp_52).toInt()
                        layoutParams.rightMargin = resources.getDimension(R.dimen.dp_12).toInt()
                    }
                    else -> {
                        layoutParams.leftMargin = resources.getDimension(R.dimen.dp_32).toInt()
                        layoutParams.rightMargin = resources.getDimension(R.dimen.dp_32).toInt()
                    }
                }

                setLayoutViewGroup(layoutParams)
                if (arrowWidth == 0) {
                    path = null
                } else {
                    val spotlightWidth = highlightXend - highlightXstart
                    val highLightCenterX = (highlightXend + highlightXstart) / 2
                    val recalcArrowWidth = getRecalculateArrowWidth(highLightCenterX.toInt(), width)

                    val safeArrowWidth = spacing + arrowWidth / 2
                    isOnTheLeftEdgeOfTheScreen = highLightCenterX < safeArrowWidth
                    isOnTheRightEdgeOfTheScreen = highLightCenterX > (width - safeArrowWidth)

                    val bufferX: Float = if (isOnTheLeftEdgeOfTheScreen) {
                        if (highlightXend < spacing + tooltipRadius) {
                            (spacing + tooltipRadius) - highlightXend
                        } else {
                            0f
                        }
                    } else if (isOnTheRightEdgeOfTheScreen) {
                        if (highlightXstart > width - (spacing + tooltipRadius)) {
                            highlightXstart - (width - (spacing + tooltipRadius))
                        } else {
                            0f
                        }
                    } else {
                        0f
                    }

                    if (recalcArrowWidth == 0) {
                        val safeStartPoint = if (spotlightWidth > arrowWidth) {
                            if (isOnTheLeftEdgeOfTheScreen) {
                                // If on the left side, get X end point
                                highlightXend + bufferX
                            } else {
                                // If on the right side, get X starting point
                                highlightXstart - bufferX
                            }
                        } else {
                            if (isOnTheLeftEdgeOfTheScreen) {
                                highlightXend + spotlightWidth / 2 + arrowWidth / 2
                            } else {
                                highlightXstart - spotlightWidth / 2 - arrowWidth / 2
                            }
                        }

                        path = Path()
                        path!!.moveTo(safeStartPoint, highlightYstart - arrowMargin - spacing / 3)
                        path!!.lineTo(
                            safeStartPoint - arrowWidth / 2,
                            highlightYstart - spacing - arrowMargin
                        )
                        path!!.lineTo(
                            safeStartPoint + arrowWidth / 2,
                            highlightYstart - spacing - arrowMargin
                        )
                        path!!.close()
                    } else {
                        path = Path()
                        when (spotlightArrowPosition) {
                            SpotlightArrowPosition.LEFT -> {
                                path!!.moveTo(highlightXstart + safeArrowWidth, highlightYend + arrowMargin + spacing / 3)
                                path!!.lineTo(
                                    highlightXstart + safeArrowWidth - recalcArrowWidth / 2,
                                    highlightYend + spacing + arrowMargin
                                )
                                path!!.lineTo(
                                    highlightXstart + safeArrowWidth + recalcArrowWidth / 2,
                                    highlightYend + spacing + arrowMargin
                                )
                            }

                            SpotlightArrowPosition.RIGHT -> {
                                path!!.moveTo(highlightXend - safeArrowWidth, highlightYend + arrowMargin + spacing / 3)
                                path!!.lineTo(
                                    highlightXend - safeArrowWidth - recalcArrowWidth / 2,
                                    highlightYend + spacing + arrowMargin
                                )
                                path!!.lineTo(
                                    highlightXend - safeArrowWidth + recalcArrowWidth / 2,
                                    highlightYend + spacing + arrowMargin
                                )
                            }
                            else -> {
                                path!!.moveTo(highLightCenterX, highlightYstart - arrowMargin - spacing / 3)
                                path!!.lineTo(
                                    highLightCenterX - recalcArrowWidth / 2,
                                    highlightYstart - spacing - arrowMargin
                                )
                                path!!.lineTo(
                                    highLightCenterX + recalcArrowWidth / 2,
                                    highlightYstart - spacing - arrowMargin
                                )
                            }
                        }
                        path!!.close()
                    }
                }
            }
            SpotlightContentPosition.UNDEFINED -> moveViewToCenter()
            else -> {
                // Do Nothing
            }
        }
    }

    private fun setLayoutViewGroup(params: LayoutParams) {
        viewGroup!!.visibility = View.INVISIBLE
        viewGroup!!.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                this@SpotlightLayout.viewGroup!!.visibility = View.VISIBLE
                this@SpotlightLayout.viewGroup!!.removeOnLayoutChangeListener(this)
            }
        })
        viewGroup!!.layoutParams = params
        invalidate()
    }

    private fun getRecalculateArrowWidth(highlightCenter: Int, maxWidthOrHeight: Int): Int {
        var recalcArrowWidth = arrowWidth
        val safeArrowWidth = spacing + arrowWidth / 2
        if (highlightCenter < safeArrowWidth ||
            highlightCenter > maxWidthOrHeight - safeArrowWidth
        ) {
            recalcArrowWidth = 0
        }
        return recalcArrowWidth
    }

    private fun moveViewToCenter() {
        spotlightContentPosition = SpotlightContentPosition.UNDEFINED
        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        layoutParams.rightMargin = spacing
        layoutParams.leftMargin = spacing
        layoutParams.bottomMargin = spacing
        layoutParams.topMargin = spacing
        setLayoutViewGroup(layoutParams)
        path = null
    }

    private fun recycleResources() {
        if (bitmap != null) {
            bitmap!!.recycle()
        }
        bitmap = null
        lastTutorialView = null
        viewPaint = null
    }
}