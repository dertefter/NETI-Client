package com.dertefter.ficus.widgets
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.lifecycle.MutableLiveData
import com.dertefter.ficus.R
import com.dertefter.ficus.TinyDB
import com.dertefter.neticore.NETICoreClient.ResponseParser
import com.dertefter.neticore.data.schedule.Lesson
import com.dertefter.neticore.data.schedule.Week


class ScheduleWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return WidgetItemFactory(applicationContext,intent)
    }

    inner class WidgetItemFactory(private val context: Context, intent: Intent) : RemoteViewsFactory {

        private val appWidgetId: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID)
        var currentDayIndex = 0
        var tinyDB = TinyDB(context.applicationContext)
        val appPreferences = com.dertefter.neticore.local.AppPreferences
        private lateinit var lessonLiveData: MutableLiveData<List<Lesson>?>
        private var lessonList = mutableListOf<Lesson>()


        init {
            currentDayIndex = appPreferences.currentDay ?: 0
            appPreferences.setup(context.applicationContext)
            lessonLiveData = MutableLiveData()
        }

        override fun onCreate() {
            val weeks = tinyDB.getListObject("weeks", Week::class.java) as ArrayList<Week>
            if (weeks != null){
                val currentWeek = weeks.find { it.isCurrent }
                if (currentWeek != null){
                    val group = appPreferences.group
                    if (group != null){
                        if (group == "individual"){
                            val bodyString = tinyDB.getString("individual_schedule")
                            val schedule = ResponseParser().parseIndividualTimetable(bodyString, currentWeek.weekQuery)
                            val dayList = schedule?.days
                            if ((dayList?.get(currentDayIndex) ?: -1) != -1){
                                lessonLiveData.postValue(dayList?.get(currentDayIndex)!!.lessons)
                            }

                        }
                    }else{
                        val bodyString = tinyDB.getString("${currentWeek.groupTitle}_${currentWeek.weekQuery}")
                        val schedule = ResponseParser().parseIndividualTimetable(bodyString, currentWeek.weekQuery)
                        val dayList = schedule?.days
                        if ((dayList?.get(currentDayIndex) ?: -1) != -1){
                            lessonLiveData.postValue(dayList?.get(currentDayIndex)!!.lessons)
                        }
                    }
                }
            }
            lessonLiveData.observeForever { list->
                if (list != null) {
                    updateList(list)
                }
            }

        }

        override fun onDataSetChanged() {
            currentDayIndex = appPreferences.currentDay ?: 0
            val weeks = tinyDB.getListObject("weeks", Week::class.java) as ArrayList<Week>
            if (weeks != null){
                val currentWeek = weeks.find { it.isCurrent }
                if (currentWeek != null){
                    val group = appPreferences.group
                    if (group != null){
                        if (group == "individual"){
                            val bodyString = tinyDB.getString("individual_schedule")
                            val schedule = ResponseParser().parseIndividualTimetable(bodyString, currentWeek.weekQuery)
                            val dayList = schedule?.days
                            if ((dayList?.get(currentDayIndex) ?: -1) != -1){
                                lessonLiveData.postValue(dayList?.get(currentDayIndex)!!.lessons)
                            }
                        }else{
                            val bodyString = tinyDB.getString("${currentWeek.groupTitle}_${currentWeek.weekQuery}")
                            val schedule = ResponseParser().parseIndividualTimetable(bodyString, currentWeek.weekQuery)
                            val dayList = schedule?.days
                            if (dayList?.isNotEmpty() == true){
                                lessonLiveData.postValue(dayList?.get(currentDayIndex)!!.lessons)
                            }
                        }
                    }
                }
            }
            if(lessonList.isNotEmpty()) {
                getViewAt(0)
            }
        }

        override fun onDestroy() {
            //Close datasource connection
        }

        override fun getCount(): Int {
            return lessonList.size
        }

        override fun getViewAt(position: Int): RemoteViews {
            val remoteViews = RemoteViews(context.packageName, R.layout.item_timetable_widget_lesson)
            try{
                remoteViews.setTextViewText(R.id.title, lessonList[position].title)
                remoteViews.setTextViewText(R.id.aud, lessonList[position].aud)
                remoteViews.setTextViewText(R.id.timeStart, lessonList[position].timeStart)
                remoteViews.setTextViewText(R.id.timeEnd, lessonList[position].timeEnd)
                remoteViews.setTextViewText(R.id.type, lessonList[position].type)
                remoteViews.setTextViewText(R.id.person, lessonList[position].person)

            }catch (e: Exception){
                Log.e("widget", e.stackTraceToString())
            }
            return remoteViews

        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        private fun updateList(newList: List<Lesson>) {
            lessonList.clear()
            lessonList.addAll(newList)
            onDataSetChanged()
        }

    }

}