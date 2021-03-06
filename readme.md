```
本库应用于快速开发新手引导，支持以下功能：

    1. 半透明蒙版
    2. 单个区域高亮
    3. 多个区域高亮
    4. 引导动画
    5. 引导内容自定义布局文件
    6. 高亮图像：圆形、方形、圆角矩形
    7. 高亮区域大小控制
    8. 引导布局文件位置控制

不足之处，欢迎各位大神指正


/**
 * Created by flying on 2018/1/15.
 */
object SpotlightUtils {

    fun showFaceGuide(view: View): SpotlightView? {
        val lights = java.util.ArrayList<HighLight>()
        val h = HighLight(ViewTarget(view), false)
                .setType(HighLight.Type.ROUND_RECT)
                .setLineHeight(104)
                .setHeadText(view.context.getString(R.string.guide_face))
                .setPadding(-25, -25, -50, view.resources.getDimensionPixelSize(R.dimen.guide_face_padding_bottom))
                .setRoundConner(AutoUtils.getPercentWidthSizeBigger(50))
        lights.add(h)
        return showIntro(lights, getActivity(view))
    }

    fun showTextGuide(view: View): SpotlightView? {
        val lights = java.util.ArrayList<HighLight>()
        val h = HighLight(ViewTarget(view), false)
                .setType(HighLight.Type.ARC_RECT)
                .setLineHeight(108)
                .setOffset(HighLight.Offset.LEFT)
                .setHeadText(view.context.getString(R.string.guide_txt))
                .setGravityRight(58)
                .setPadding(30, view.resources.getDimensionPixelSize(R.dimen.guide_txt_padding_right), 15, 15)
        lights.add(h)
        return showIntro(lights, getActivity(view))
    }

    fun showAddFriendGuide(view: View): SpotlightView? {
        val h = HighLight(ViewTarget(view), false)
                .setType(HighLight.Type.CIRCLE)
                .setLineHeight(144)
                .setRadius(40)
                .setHeadText(view.context.getString(R.string.guide_add_friend))
                .setGravityRight(22)
                .setLineDown(true)// 切换到右上角后，打开注释
        val lights = ArrayList<HighLight>()
        lights.add(h)
        return showIntro(lights, getActivity(view))
    }

    fun showGuardShareGuide(shareView: View): SpotlightView? {
        val h = HighLight(ViewTarget(shareView), false)
                .setType(HighLight.Type.ARC_RECT)
                .setLineHeight(144)
                .setPadding(-30, 0, 20, 20)
                .setHeadText(shareView.context.getString(R.string.defend_map_share_guide))
                .setGravityRight(22)
                .setLineDown(true)
        val lights = ArrayList<HighLight>()
        lights.add(h)
        return showIntro(lights, getActivity(shareView))
    }

    fun showWelcomeHomeGuide(friendMarkerViewList: List<FriendMarkerView>, expandView: View): SpotlightView? {
        SLog.d("SpotlightUtils", "friendMarkerViewList.size = " + friendMarkerViewList.size)
//        if (friendMarkerViewList.size < 3) {
//            return null
//        }
        val lights = ArrayList<HighLight>()
        friendMarkerViewList.forEach {
            val location = intArrayOf(it.x.toInt(), it.y.toInt())
            SLog.d("SpotlightUtils", " x = ${location[0]} , y = ${location[1]}")
            val h = HighLight(ViewTarget(it), true)
                    .setType(HighLight.Type.CIRCLE)
                    .setLineHeight(83)
                    .setRadius(75)
                    .setGravityLeft(58)
            lights.add(h)
        }
        lights.sortBy { it.target.point.y }
        lights.last().isOnlyHighLight = false
        lights.last().headText = friendMarkerViewList[0].context.getString(R.string.guide_welcome_home)
        return showIntro(lights, expandView, getActivity(friendMarkerViewList[0]))
    }

    fun showAddressAndWeatherGuide(addressView: View, chatWeatherView: View): SpotlightView? {
        var weatherLineHeight = addressView.resources.getDimensionPixelSize(R.dimen.guide_weather_line_height)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            //因为4。4以下没有状态栏无法去掉，需要计算该高度
            weatherLineHeight = 255
        }
        val weather = HighLight(ViewTarget(chatWeatherView), false)
                .setType(HighLight.Type.ARC_RECT)
                .setLineHeight(weatherLineHeight)
                .setHeadText(chatWeatherView.context.getString(R.string.guide_weather))
                .setGravityRight(28)
                .setPadding(-20, -20, 15, 15)
                .setOffset(HighLight.Offset.LEFT)

        val address = HighLight(ViewTarget(addressView), false)
                .setType(HighLight.Type.ARC_RECT)
                .setLineHeight(chatWeatherView.resources.getDimensionPixelSize(R.dimen.guide_address_line_height))
                .setHeadText(addressView.context.getString(R.string.guide_address))
                .setGravityRight(28)
                .setPadding(-45, -45, 5, -5)

        val lights = java.util.ArrayList<HighLight>()
        lights.add(weather)
        lights.add(address)
        return showIntro(lights, getActivity(chatWeatherView))
    }

    private fun showIntro(lights: List<HighLight>, activity: Activity): SpotlightView {
        return showIntro(lights, null, activity)
    }


    private fun showIntro(lights: List<HighLight>, expandView: View?, activity: Activity): SpotlightView {
//        spotLight?.removeSpotlightView()
        val build = SpotlightView.Builder(activity)
        if (expandView != null) {
            build.setExpandView(expandView)
        }
        val spotLight = build
                .headingTvColor(Color.parseColor("#ffffff"))
                .headingTvSize(activity.applicationContext.resources.getDimensionPixelSize(R.dimen.guide_head_text_size))
                .maskColor(Color.parseColor("#CC00B0FF"))
                .highLight(lights)
                .lineAndArcColor(Color.parseColor("#ffffff"))
                .lineStroke(3)
                .dismissOnBackPress(true)
                .build()
        spotLight!!.setOnSpotlinghViewTouchEvent { event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if (spotLight.isAnimEnd) {
                        spotLight!!.removeSpotlightView()
                    }
                }
            }
            true
        }
        return build.show()
    }

    private fun getActivity(view: View): Activity {
        val context = view.context
        if (context is Activity) {
            return context as Activity
        }
        return (context as ContextWrapper).baseContext as Activity
    }
}
```
