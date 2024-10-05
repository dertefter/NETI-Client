package com.dertefter.ficus.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.dertefter.ficus.MainActivity
import com.dertefter.ficus.R
import com.dertefter.ficus.TinyDB
import com.dertefter.neticore.NETICoreClient.ResponseParser
import com.dertefter.neticore.data.schedule.Week
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.DelicateCoroutinesApi

class ScheduleWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        val widgetManager = appWidgetManager
        widgetManager?.notifyAppWidgetViewDataChanged(widgetManager
            .getAppWidgetIds(ComponentName(context?.applicationContext?.packageName!!, ScheduleWidget::class.java.name)),
            R.id.list_view
        )

        for (appWidgetId in appWidgetIds!!) {
            updateWidget(context, appWidgetId, appWidgetManager)
        }
    }

    fun updateWidget(context: Context?, appWidgetId: Int, appWidgetManager: AppWidgetManager?){
        val views = RemoteViews(context?.packageName, R.layout.schedule_widget)
        val appPreferences = com.dertefter.neticore.local.AppPreferences
        appPreferences.setup(context?.applicationContext!!)
        val tinyDB = TinyDB(context?.applicationContext!!)
        val weeks = tinyDB.getListObject("weeks", Week::class.java) as ArrayList<Week>
        Log.e("wwwwwwwwwwwwwwwwwwww", weeks.toString())
        val currentDayIndex = appPreferences.currentDay ?: 0
        val group = appPreferences.group
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        views.setOnClickPendingIntent(R.id.week_text_view, pendingIntent)
        views.setOnClickPendingIntent(R.id.updateButton, pendingIntent)
        views.setOnClickPendingIntent(R.id.openApp, pendingIntent)

        val serviceIntent = Intent(context, ScheduleWidgetService::class.java)
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))
        views.setRemoteAdapter(R.id.list_view, serviceIntent)

        val listOfDaysId = listOf(R.id.d0, R.id.d1, R.id.d2, R.id.d3, R.id.d4, R.id.d5)
        for (id_ in listOfDaysId){
            views.setInt(id_, "setBackgroundResource", 0)
        }
        views.setInt(listOfDaysId[currentDayIndex ?: 0], "setBackgroundResource", R.drawable.rounded_background2)

        for (i in listOfDaysId.indices){
            views.setOnClickPendingIntent(listOfDaysId[i], PendingIntent.getBroadcast(context, 0, Intent(context, ScheduleWidget::class.java).apply {
                action = "d$i"
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        }

        appWidgetManager!!.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val appPreferences = com.dertefter.neticore.local.AppPreferences
        appPreferences.setup(context?.applicationContext!!)
        if (intent?.action != null) {
            Log.e("ddddddddddddddd", intent.action.toString())
            when (intent.action){
                "d0" -> appPreferences.currentDay = 0
                "d1" -> appPreferences.currentDay = 1
                "d2" -> appPreferences.currentDay = 2
                "d3" -> appPreferences.currentDay = 3
                "d4" -> appPreferences.currentDay = 4
                "d5" -> appPreferences.currentDay = 5
            }
            val views = RemoteViews(context?.packageName, R.layout.schedule_widget)


            //full update widget
            val widgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = widgetManager.getAppWidgetIds(ComponentName(context?.applicationContext?.packageName!!, ScheduleWidget::class.java.name))

            for (appWidgetId in widgetIds) {
                updateWidget(context, appWidgetId, widgetManager)
            }
        }

    }
}