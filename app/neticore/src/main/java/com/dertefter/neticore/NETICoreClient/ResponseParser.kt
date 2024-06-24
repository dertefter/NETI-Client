package com.dertefter.neticore.NETICoreClient

import android.util.Log
import com.dertefter.neticore.data.Person
import com.dertefter.neticore.data.User
import com.dertefter.neticore.data.campus_pass.CampusPass
import com.dertefter.neticore.data.campus_pass.CampusPassData
import com.dertefter.neticore.data.campus_pass.CampusPassDate
import com.dertefter.neticore.data.campus_pass.CampusPassTime
import com.dertefter.neticore.data.dispace.di_cources.DiCourse
import com.dertefter.neticore.data.dispace.di_cources.DiCourseContent
import com.dertefter.neticore.data.dispace.di_cources.DiCourseView
import com.dertefter.neticore.data.dispace.di_messages.DiMessage
import com.dertefter.neticore.data.dispace.di_messages.DiMessagesData
import com.dertefter.neticore.data.dispace.di_messages.DiSenderPerson
import com.dertefter.neticore.data.messages.Message
import com.dertefter.neticore.data.messages.SenderPerson
import com.dertefter.neticore.data.news.News
import com.dertefter.neticore.data.news.NewsContent
import com.dertefter.neticore.data.schedule.Day
import com.dertefter.neticore.data.schedule.Group
import com.dertefter.neticore.data.schedule.Lesson
import com.dertefter.neticore.data.schedule.Schedule
import com.dertefter.neticore.data.schedule.SessiaScheduleItem
import com.dertefter.neticore.data.schedule.Week
import com.dertefter.neticore.data.sessia_results.SessiaItem
import com.dertefter.neticore.data.sessia_results.SessiaResults
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import okhttp3.ResponseBody
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class ResponseParser {

    fun getQueryString(input: String): String? {
        val regex = Regex("\\?(.+)")
        val matchResult = regex.find(input)

        return matchResult?.groups?.get(1)?.value
    }

    fun parseUserInfo(input: ResponseBody?): User?{
        try{
            val pretty = input?.string().toString()
            val doc: Document = Jsoup.parse(pretty)
            val fullName = doc.select("span.fio").text().split(",")[0]
            val name = doc.select("span.fio").text().split(" ")[1]
            val groupTitle = doc.select("span.fio").text().split(",")[1].replace(" ", "")
            val group = Group(title = groupTitle, isIndividual = true)
            return User(name = name, fullName = fullName, group = group)
        }catch (e: Exception){
            return null
        }
    }

    fun parseWeeks(input: ResponseBody?, isIndividual: Boolean = false): List<Week>?{
        try{
            val pretty = input!!.string()
            val doc: Document = Jsoup.parse(pretty)
            val weeks_content = doc.select("div.schedule__weeks-content")
            val weeks_a = weeks_content.select("a")
            val week_label_today = doc.select("span.schedule__title-label").text()
            val groupTitle = doc.select("h1.schedule__title-h1").text().split(" ").last()
            val output = mutableListOf<Week>()
            for (it in weeks_a){
                val title = it.text()
                val query = it.attr("data-week")
                var isToday = false
                if (week_label_today.contains(query)){
                    isToday = true
                }

                val week_item = Week(query, title, isToday, groupTitle, isIndividual)
                if (query.toInt() > 0){
                    output.add(week_item)
                }

            }
            return output
        }catch (e: Exception) {
            return null
        }

    }

    fun parseSessiaSchedule(input: ResponseBody?): List<SessiaScheduleItem>?{
        try{
            val output = mutableListOf<SessiaScheduleItem>()
            val pretty = input!!.string()
            val doc: Document = Jsoup.parse(pretty)
            val table = doc.select("div.schedule__session-body").first()
            val items = table!!.select("> *")
            for (i in items){
                var date = i.select("div.schedule__session-day").first()?.text().toString()
                val dayName = formatDateDayName(date)
                date = formatDate(date)
                val time = i.select("div.schedule__session-time").first()?.text().toString()
                val name = i.select("div.schedule__session-item").first()?.ownText().toString()
                val aud = i.select("div.schedule__session-class").first()?.text().toString()
                val type = i.select("div.schedule__session-label").first()?.text().toString()
                val personLink = i.select("div.schedule__session-item").first()?.select("a")?.first()?.attr("href")+"/"
                output.add(SessiaScheduleItem(name, time, date, type, aud, personLink, dayName))
                Log.e("sesese", "$name")
            }
            return output
        }catch (e: Exception) {
            return null
        }

    }

    fun formatDate(dateString: String): String {
        val locale = Locale("ru", "RU")
        val inputFormat = SimpleDateFormat("dd.MM.yy", locale)
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("dd.MM", locale)
        return outputFormat.format(date)
    }

    fun formatDateDayName(dateString: String): String {
        val locale = Locale("ru", "RU")
        val inputFormat = SimpleDateFormat("dd.MM.yy", locale)
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("EEEE", locale)
        return outputFormat.format(date).toString()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    fun parseIndividualTimetable(input: String?, weekQuery: String): Schedule? {
        try{
            val output = Schedule()
            val pretty = input?.toString()
            val dayItems = mutableListOf<Day>()
            val doc: Document = Jsoup.parse(pretty)
            val table_body = doc.body().select("div.schedule__table-body").first()
            val table_days = table_body?.select("> *")
            val week_label_today = doc.select("span.schedule__title-label").text()
            if (table_days != null) {
                for (it in table_days){
                    val title = it.select("div.schedule__table-day").first()?.ownText().toString()

                    val date = it.select("span.schedule__table-date").text()
                    var dayToday: Boolean = false
                    if (week_label_today.split(" ").size > 1 && week_label_today.split(" ")[0].contains(weekQuery)){
                        val today = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru"))
                        if (today == title){
                            dayToday = true
                        }
                    }
                    val dayItem = Day(title, date, null, dayToday)
                    val lessonsItems = mutableListOf<Lesson>()
                    val cell = it.select("div.schedule__table-cell")[1]
                    val lessons = cell.select("> *")
                    for (l in lessons){
                        val time = l.select("div.schedule__table-time").text()
                        val timeStart = time.split("-")[0]
                        val timeEnd = time.split("-")[1]
                        val items = l.select("div.schedule__table-item")
                        for (t in items){
                            var incorrect = false
                            var label = t.select("span.schedule__table-label").first()
                            if (label != null && !label.text().isNullOrEmpty()){
                                val labelText = label.text()
                                if (labelText.contains("недели")){
                                    val available_weeks = label.text().split(" ")
                                    if (!available_weeks.contains(weekQuery)){
                                        incorrect = true
                                    }
                                }
                                else if (labelText.contains("по чётным") && weekQuery.toInt() % 2 != 0){
                                    incorrect = true
                                }
                                else if (labelText.contains("по нечётным") && weekQuery.toInt() % 2 == 0){
                                    incorrect = true
                                }



                            }
                            var lesson_title = t.ownText().replace("·", "").replace(",", "")
                            val type = t.select("span.schedule__table-typework").first()?.ownText()
                            val aud = t.parent()?.parent()?.select("div.schedule__table-class")?.text()
                            var person = ""
                            for (p in t.select("a")){
                                person = person + p.text() + "\n"
                            }
                            if (person != ""){
                                person = person.substring(0, person.length - 1)
                            }
                            if (lesson_title != "" && !incorrect){
                                val lesson_item = Lesson(lesson_title, timeStart, timeEnd, type, aud, person, null)
                                lessonsItems.add(lesson_item)
                            }


                        }
                    }
                    dayItem.lessons = lessonsItems
                    dayItems.add(dayItem)

                }
            }
            output.days = dayItems
            if (output.days.isNullOrEmpty()){
                output.isError = true
            }
            return output

        }
        catch (e: Exception){
            Log.e("ResponseParser", "parseIndividualTimetable: ${e.stackTraceToString()}")
            return null
        }

    }

    fun parseGroupTimetable(input: String?): Schedule? {
        try{
            val output = Schedule()
            val pretty = input
            val dayItems = mutableListOf<Day>()
            val doc: Document = Jsoup.parse(pretty)
            val table_body = doc.body().select("div.schedule__table-body").first()
            val table_days = table_body?.select("> *")

            if (table_days != null) {
                for (it in table_days){
                    val title = it.select("div.schedule__table-day").first()?.ownText().toString()
                    val date = it.select("span.schedule__table-date").text()
                    val dayToday: Boolean = it.select("div.schedule__table-day").first()?.attr("data-today").toBoolean()
                    val dayItem = Day(title, date, null, dayToday)
                    val lessonsItems = mutableListOf<Lesson>()
                    val cell = it.select("div.schedule__table-cell")[1]
                    val lessons = cell.select("> *")
                    for (l in lessons){
                        val time = l.select("div.schedule__table-time").text()
                        val timeStart = time.split("-")[0]
                        val timeEnd = time.split("-")[1]
                        val items = l.select("div.schedule__table-item")
                        for (t in items){
                            var lesson_title = t.ownText().replace("·", "").replace(",", "")
                            val type = t.select("span.schedule__table-typework").first()?.ownText()
                            val aud = t.parent()?.parent()?.select("div.schedule__table-class")?.text()
                            var person = ""
                            for (p in t.select("a")){
                                person = person + p.text() + "\n"
                            }
                            if (person != ""){
                                person = person.substring(0, person.length - 1)
                            }
                            if (lesson_title != ""){
                                val lesson_item = Lesson(lesson_title, timeStart, timeEnd, type, aud, person, null)
                                lessonsItems.add(lesson_item)

                            }


                        }
                    }
                    dayItem.lessons = lessonsItems
                    dayItems.add(dayItem)
                }
            }
            output.days = dayItems
            return output
        } catch (e: Exception) {
            Log.e("ResponseParser", e.stackTraceToString().toString())
            return null
        }
    }

    fun parseGroups(input: ResponseBody?): List<Group>? {
        val outputGroupList = mutableListOf<Group>()

        val jsonString = input!!.string()
        val jsonObject = JSONObject(jsonString)
        val items = jsonObject.getString("items")

        val doc: Document = Jsoup.parse(items.replace("\n", "").replace("\t", ""))
        val available_group_items = doc.body().select("a")
        val unavailable_group_items = doc.body().select("span")

        for (it in available_group_items){
            val title = it.text().toString()
            val item = Group(title, false)
            outputGroupList.add(item)
        }
        for (it in unavailable_group_items){
            val title = it.text().toString()
            val item = Group(title, false)
            outputGroupList.add(item)
        }
        return outputGroupList

    }

    fun parsePersonList(input: ResponseBody?): List<Person> {
        val outputPersonList = mutableListOf<Person>()
        val pretty = input!!.string().toString()
        val doc: Document = Jsoup.parse(pretty)
        val result = doc.select("div.search-result")
        val results = result.select("div.search-result__item")
        for (it in results){
            val name = it.select("div.search-result__title").text()
            var mail = ""
            var hasSchedule = false
            if (it.html().toString().contains("edu_actions/timetables/joint_timetable")){
                hasSchedule = true
            }
            if (!it.select("span.insert_mail").attr("before").isNullOrEmpty()){
                val before = it.select("span.insert_mail").select("span").first()?.attr("before").toString()
                val after = it.select("span.insert_mail").select("span").first()?.attr("after").toString()
                mail = "$before@$after"
            }
            var site = ""
            var id: String? = null
            val links = it.select("a")
            for (l in links){
                if (l.attr("title").contains("персональный сайт")){
                    site = l.attr("href")
                    id = site.replace("https://ciu.nstu.ru/kaf/persons/", "")
                }
            }
            site = "https://ciu.nstu.ru/kaf/persons/$id"
            var pic = it.select("img").attr("src").toString()
            if (!pic.isNullOrEmpty()){
                pic = "https://www.nstu.ru/" + pic
            }

            val person = Person(name, mail, site, pic, hasSchedule, id)
            outputPersonList.add(person)
        }
        Log.e("personParser", outputPersonList.toString())
        return outputPersonList

    }

    fun parseNews(input: ResponseBody?): List<News>? {
        val outputNewsList = mutableListOf<News>()
        try{
            val jsonString = input!!.string()
            val jsonObject = JSONObject(jsonString)
            val items = jsonObject.getString("items")
            val haveMore = jsonObject.getBoolean("haveMore")
            val doc: Document = Jsoup.parse(items.replace("\n", "").replace("\t", ""))
            val news_items = doc.body().select("a")
            for (it in news_items){
                var imageUrl: String? = null
                if (it.attr("style").toString().replace("background-image: url(", "").replace(");", "").replace("//", "/") != ""){
                    imageUrl = "https://www.nstu.ru/" + it.attr("style").toString().replace("background-image: url(", "").replace(");", "").replace("//", "/")
                }
                val title = it.select("div.main-events__item-title").text().toString()
                val tag = it.select("div.main-events__item-tags").text().toString()
                val date = it.select("div.main-events__item-date").text().toString()
                val link = it.attr("href")
                val type = it.select("div.main-events__item-type").text().toString()
                val newsid = getQueryString(link)!!.replace("idnews=", "")
                val dataid  = it.attr("data-type")
                if (dataid == "video" || dataid == "photo"){
                    continue
                }

                val item = News(newsid, title, date, imageUrl, tag, type)

                outputNewsList.add(item)
            }
            return outputNewsList
        } catch (e: Exception) {
            Log.e("ResponseParser", e.stackTraceToString().toString())
            return null
        }
    }

    fun parseNewsContent(input: ResponseBody?): NewsContent? {
        try {
            val pretty = input!!.string().toString()
            val doc: Document = Jsoup.parse(pretty)
            val htmlContent = doc.body().select("div.row")[1].toString()
            val htmlContacts = doc.body().select("div.aside-events__contacts").toString()
            return NewsContent(htmlContent, htmlContacts)
        } catch (e: Exception) {
            Log.e("ResponseParser", e.stackTraceToString().toString())
            return null
        }
    }

    fun parseSenderList(input: ResponseBody?, tab: Int): List<SenderPerson> {
        val outputSenderList = mutableListOf<SenderPerson>()
        try {
            val pretty = input?.string().toString()
            val doc: Document = Jsoup.parse(pretty)
            val tabContainer = if (tab == 0) doc.select("div#tabs1-messages").first() else doc.select("div#tabs2-messages").first()
            val messageItems = tabContainer?.select("div.pad")
            for (it in messageItems!!){
                var is_new = false
                if (it.hasClass("new_message_header")){ is_new = true }
                val send_by = it.select("div.col-2.col-sm-6").first()?.text().toString()
                val mes_id = it.select("div.col-8.col-sm-6").first()?.attr("onclick").toString()
                    .replace("openWin2('https://ciu.nstu.ru/student_study/mess_teacher/view?id=","")
                    .replace("');return false;","")
                val mes_text = it.select("div.col-8.col-sm-6").first()?.text().toString()
                val text = mes_text.split(" -- ")[1]
                val title = mes_text.split(" -- ")[0]
                var date = it.select("div.col-1.col-sm-3").first()?.text().toString()
                val message = Message(mes_id, title, text, is_new, date)
                if (outputSenderList.find { it.name == send_by } == null){
                    val sender = SenderPerson(send_by, mutableListOf(message))
                    outputSenderList.add(sender)
                }else{
                    val sender = outputSenderList.find { it.name == send_by }
                    sender?.messages?.add(message)
                }
            }
            return outputSenderList

        }catch (e: Exception) {
            Log.e("ResponseParser", e.stackTraceToString().toString())
            return outputSenderList
        }
    }

    fun parseMessage(input: ResponseBody?): String {
        try {
            val pretty = input?.string().toString()
            val doc = Jsoup.parse(pretty).select("main.page-content").first()
            val mes = doc?.select("form")?.select("span")?.get(6)
            Log.e("mes", mes.toString())
            return mes!!.html()
        }
        catch (e: Exception) {
            return ""
        }
    }

    fun get18Weeks(): List<Week> {
        val output = mutableListOf<Week>()
        for (i in 1..18){
            val title = "Неделя $i"
            val query = i.toString()
            val isToday = false
            val groupTitle = ""
            val isIndividual = false
            val week_item = Week(query, title, isToday, groupTitle, isIndividual)
            output.add(week_item)
        }
        return output
    }

    fun parsePersonTimetable(input: ResponseBody?, weekQuery: String): Schedule? {
        try{
            val output = Schedule()
            val pretty = input?.string().toString()
            val dayItems = mutableListOf<Day>()
            val doc: Document = Jsoup.parse(pretty)
            val table_body = doc.body().select("div.schedule__table-body").first()
            val table_days = table_body?.select("> *")

            if (table_days != null) {
                for (it in table_days){
                    val title = it.select("div.schedule__table-day").first()?.ownText().toString()

                    val date = it.select("span.schedule__table-date").text()
                    val dayToday: Boolean = it.select("div.schedule__table-day").first()?.attr("data-today").toBoolean()
                    val dayItem = Day(title, date, null, dayToday)
                    val lessonsItems = mutableListOf<Lesson>()
                    val cell = it.select("div.schedule__table-cell")[1]
                    val lessons = cell.select("> *")
                    for (l in lessons){
                        val time = l.select("div.schedule__table-time").text()
                        val timeStart = time.split("-")[0]
                        val timeEnd = time.split("-")[1]
                        val items = l.select("div.schedule__table-item")
                        for (t in items){
                            var incorrect = false
                            var label = t.select("span.schedule__table-label").first()
                            if (label != null && !label.text().isNullOrEmpty()){
                                val labelText = label.text()
                                if (labelText.contains("недели")){
                                    val available_weeks = label.text().split(" ")
                                    if (!available_weeks.contains(weekQuery)){
                                        incorrect = true
                                    }
                                }
                                else if (labelText.contains("по чётным") && weekQuery.toInt() % 2 != 0){
                                    incorrect = true
                                }
                                else if (labelText.contains("по нечётным") && weekQuery.toInt() % 2 == 0){
                                    incorrect = true
                                }



                            }
                            var lesson_title = t.ownText().replace("·", "").replace(",", "")
                            val type = t.select("span.schedule__table-typework").first()?.ownText()
                            val aud = t.parent()?.parent()?.select("div.schedule__table-class")?.text()
                            var person = l?.select("b")?.first()?.text().toString()
                            if (lesson_title != "" && !incorrect){
                                val lesson_item = Lesson(lesson_title, timeStart, timeEnd, type, aud, person, null)
                                lessonsItems.add(lesson_item)
                            }


                        }
                    }
                    dayItem.lessons = lessonsItems
                    dayItems.add(dayItem)

                }
            }
            output.days = dayItems
            return output

        }
        catch (e: Exception){
            Log.e("ResponseParser", "parseIndividualTimetable: ${e.stackTraceToString()}")
            return null
        }

    }

    fun parseDiChats(input: ResponseBody?): List<DiSenderPerson> {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val prettyJson = gson.toJson(
            JsonParser.parseString(
                input?.string()
            )
        )
        val json = JSONObject(prettyJson)
        val messages = json.getJSONArray("list")
        val output = mutableListOf<DiSenderPerson>()
        for (i in 0 until messages.length()) {
            val message = messages.getJSONObject(i)
            output.add(
                DiSenderPerson(
                    message.optString("companion_id"),
                    message.optString("date"),
                    message.optString("group_title"),
                    message.optString("id"),
                    message.optString("is_new"),
                    message.optString("is_read"),
                    Jsoup.parse(message.optString("last_msg")).text(),
                    message.optString("message"),
                    message.optString("name"),
                    message.optString("patronymic"),
                    message.optString("photo"),
                    message.optString("surname"),
                    message.optString("theme"),
                    message.optString("w_color"),
                    message.optString("w_title"),
                    message.optString("workspace_id")
                )
            )
        }
        return output
    }

    fun parseDiMessages(input: ResponseBody?): DiMessagesData {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val prettyJson = gson.toJson(JsonParser.parseString(input?.string()))
        val json = JSONObject(prettyJson)
        var messages = json.getJSONArray("data")
        val remain = json.getInt("remain")
        val mesList = mutableListOf<DiMessage>()
        for(i in 0 until messages.length()){
            val message = messages.getJSONObject(i)
            mesList.add(
                DiMessage(
                    message.optString("date"),
                    message.optString("date_full"),
                    message.optString("id"),
                    message.optString("is_read"),
                    message.optString("message"),
                    message.optString("msg_files"),
                    message.optString("priv_msg_id"),
                    message.optString("recipient_delete"),
                    message.optString("recipient_id"),
                    message.optString("sender_delete"),
                    message.optString("sender_id"),
                    message.optString("theme")
                )
            )

        }
        return DiMessagesData(mesList, remain)
    }

    fun parseDiCourseList(input: ResponseBody?): List<DiCourse> {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val prettyJson = gson.toJson(JsonParser.parseString(input?.string()))
        val json = JSONObject(prettyJson)
        val courses = json.getJSONArray("courses")
        val output = mutableListOf<DiCourse>()
        for (i in 0 until courses.length()){
            output.add(
                DiCourse(
                    courses.getJSONObject(i).optString("access_type"),
                    courses.getJSONObject(i).optString("author_id"),
                    courses.getJSONObject(i).optString("award_bronze"),
                    courses.getJSONObject(i).optString("award_description"),
                    courses.getJSONObject(i).optString("award_gold"),
                    courses.getJSONObject(i).optString("award_register"),
                    courses.getJSONObject(i).optString("award_reject"),
                    courses.getJSONObject(i).optString("award_request"),
                    courses.getJSONObject(i).optString("award_silver"),
                    courses.getJSONObject(i).optString("ciu_hash"),
                    courses.getJSONObject(i).optString("ciu_id"),
                    courses.getJSONObject(i).optString("colleague"),
                    courses.getJSONObject(i).optString("competencies"),
                    courses.getJSONObject(i).optString("curs_in_ws"),
                    courses.getJSONObject(i).optString("date_created"),
                    courses.getJSONObject(i).optString("date_finish"),
                    courses.getJSONObject(i).optString("description"),
                    courses.getJSONObject(i).optString("description_en"),
                    courses.getJSONObject(i).optString("description_system"),
                    courses.getJSONObject(i).optString("difficulty"),
                    courses.getJSONObject(i).optString("faculty"),
                    courses.getJSONObject(i).optString("goals"),
                    courses.getJSONObject(i).optString("id"),
                    courses.getJSONObject(i).optInt("infav"),
                    courses.getJSONObject(i).optString("interval_id"),
                    courses.getJSONObject(i).optString("is_dpo"),
                    courses.getJSONObject(i).optString("learning_format"),
                    courses.getJSONObject(i).optString("loads_week"),
                    courses.getJSONObject(i).optString("loads_ze"),
                    courses.getJSONObject(i).optString("logo"),
                    courses.getJSONObject(i).optString("mooc_status"),
                    courses.getJSONObject(i).optString("name"),
                    courses.getJSONObject(i).optString("password"),
                    courses.getJSONObject(i).optString("price"),
                    courses.getJSONObject(i).optString("requirements"),
                    null,
                    courses.getJSONObject(i).optString("specialty"),
                    courses.getJSONObject(i).optString("status"),
                    courses.getJSONObject(i).optString("sync_catalog"),
                    courses.getJSONObject(i).optString("type"),
                    courses.getJSONObject(i).optString("updated_at"),
                    courses.getJSONObject(i).optString("video_preview"),
                    courses.getJSONObject(i).optString("workspaces")
                )
            )
        }
        return output
    }

    fun parseCourseView(body: ResponseBody?): DiCourseView {
        val MenuItems = mutableListOf<String>()
        val pretty = body?.string().toString()
        val doc: Document = Jsoup.parse(pretty)
        val doc_menu = doc.select("div.dd-main-menu")
        val doc_menu_items = doc_menu.select("a")
        for (it in doc_menu_items){
            if (it.text() != "home"){
                MenuItems.add(it.text())
            }
        }
        val diCourseTitle = doc.select("h1.page-title").text()
        val diCourseContent = mutableListOf<DiCourseContent>()
        val annotation = doc.select("div.course-annotation").first()
        if (annotation != null){
            diCourseContent.add(DiCourseContent("typography", annotation.html()))
        }
        val infos = doc.select("div.info-block")
        for (it in infos){
            if (!it.classNames().contains("invisible") && it.text().isNotEmpty() && !it.classNames().contains("table-of-contents")){
                diCourseContent.add(DiCourseContent("info", it.text()))
            }
        }


        val infoblocks = doc.select("div.dd-infoblock-outer")
        for (it in infoblocks){
            val title = it.select("div.nav-wrapper").first()?.text()
            if (title != null){
                diCourseContent.add(DiCourseContent("header", title))

            }

            val typography = it.select("div.html")
            for (t in typography){
                diCourseContent.add(DiCourseContent("typography", t.html()))
            }
            val attachments = it.select("div.btn-group-part")
            for (a in attachments){
                val butt = a.select("button").first()
                if (butt != null){
                    val name = butt.ownText()
                    val link = a.parent()?.select("a")?.first()?.attr("href")
                    if (!name.isNullOrEmpty() && !link.isNullOrEmpty()){
                        diCourseContent.add(DiCourseContent("attachment", null, "https://dispace.edu.nstu.ru/$link/", name))
                    }
                }
            }

            val imagesChips_ = it.select("div.chip.image")
            val imagesChips = mutableListOf<String>()
            for (i in imagesChips_){
                imagesChips.add("https://dispace.edu.nstu.ru/" + i.select("img").attr("src"))
            }
            if (imagesChips.isNotEmpty()){
                diCourseContent.add(DiCourseContent("image_chip", imagesChips = imagesChips))
            }


        }


        return DiCourseView(MenuItems, diCourseContent, title = diCourseTitle)
    }

    fun parseSessiaResults(input: ResponseBody? = null): List<SessiaResults> {
        val output = mutableListOf<SessiaResults>()
        val pretty = input?.string().toString()
        val doc: Document = Jsoup.parse(pretty)
        val s = doc.body().select("main.page-content")
        var tables = s.select("table.tdall")
        for (table in tables){
            val title = "Семестр " + (tables.indexOf(table)).toString()
            val sessiaItems = mutableListOf<SessiaItem>()
            val trs = table.select("tr.last_progress")
            for (tr in trs){
                var isFail = false
                val title = tr.select("td")[1]?.ownText().toString()
                val date = tr.select("td")[2]?.ownText().toString()
                var value = tr.select("td")[3]?.text().toString()
                if (value.contains("Н")){
                    isFail = true
                }
                if (!isFail){
                    val valueFivePoint = tr.select("td")[4]?.text().toString()
                    if ((value.isEmpty() || value == "0") && valueFivePoint == "Зач"){
                        value = "100"
                    }
                    val valueECTS = tr.select("td")[5]?.text().toString()
                    val valuedBy = tr.select("td")[6]?.text().toString()
                    val teacher = tr.select("td")[7]?.text().toString()
                    val item = SessiaItem(title, date, value, valueFivePoint, valueECTS, valuedBy, teacher)
                    sessiaItems.add(item)
                }else{
                    val valuedBy = tr.select("td")[4]?.text().toString()
                    val teacher = tr.select("td")[5]?.text().toString()
                    val item = SessiaItem(title, date, value, "", "", valuedBy, teacher)
                    sessiaItems.add(item)
                }

            }
            if (title != "Семестр 0"){
                output.add(SessiaResults(title, sessiaItems))
            }
        }
        return output
    }

    fun parseCampusPass(body: ResponseBody?): CampusPassData {
        val pretty = body?.string().toString()
        val doc: Document = Jsoup.parse(pretty)
        val switch_content = doc.select("div[id=switch-content]")
        val button_cancel = switch_content.select("button[id=cancel_reservation]").first()
        if (button_cancel == null){

            val campusPassDates: MutableList<CampusPassDate> = mutableListOf()

            val days_container = doc.body().select("div.days")
            val days = days_container.select("a.day-item.day-item--available")
            for (day in days){
                val data_calendar_date = day.attr("data-calendar-date").toString()
                val text = day.ownText().toString()
                val item = CampusPassDate(data_calendar_date, text)
                campusPassDates.add(item)
            }

            return CampusPassData(false, campusPassDates = campusPassDates)
        }else{
            val id = button_cancel.attr("data-reservation-id")
            val pass_info = switch_content.select("div[style=\"font-size: 16px; font-weight: bold; color: #333732; text-align: center\"]").first()
            val pass_info_text = pass_info!!.text().split(". ")
            val taloon_string = pass_info_text[1].split(" ")
            val date_string = pass_info_text[0].split(" ")
            val taloon = taloon_string[1]
            var date = date_string[3]
            date = parseDate(date)
            val time = date_string[5].replace(".", "")
            val pass = CampusPass(date, time, taloon, id)

            return CampusPassData(true, pass)
        }



    }

    fun parseCampusPassTimes(body: ResponseBody?): MutableList<CampusPassTime> {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val prettyJson = gson.toJson(JsonParser.parseString(body?.string()))
        val json = JSONObject(prettyJson)
        val doc = Jsoup.parse(json.get("html").toString())
        val times_ = doc.select("button")
        val times = mutableListOf<CampusPassTime>()

        for (time in times_){
            val isAvailable = "disabled" !in time.toString()
            val time_text = time.ownText().toString()
            val data_interval = time.attr("data-interval")
            val data_interval_start = time.attr("data-interval-start")
            val CampusPassTime = CampusPassTime(isAvailable, time_text, data_interval, data_interval_start)
            if (isAvailable){
                times.add(CampusPassTime)
            }

        }
        return times
    }

    fun parseDate(value: String): String{
        val dateArr = value.split(".")
        val month = when(dateArr[1]){
            "01" -> "января"
            "02" -> "февраля"
            "03" -> "марта"
            "04" -> "апреля"
            "05" -> "мая"
            "06" -> "июня"
            "07" -> "июля"
            "08" -> "августа"
            "09" -> "сентября"
            "10" -> "октября"
            "11" -> "ноября"
            "12" -> "декабря"
            else -> "января"
        }
        val day = dateArr[0].toInt().toString()
        val year = dateArr[2].toString()
        return("$day $month $year года")
    }

    fun parseMoneyYears(input: ResponseBody?): List<String>{
        val years = mutableListOf<String>()
        val pretty = input?.string().toString()
        val doc: Document = Jsoup.parse(pretty)
        val form = doc.select("form[action=https://ciu.nstu.ru/student_study/personal/money]").first()
        val years_list = form?.select("select[name=year] option")
        if (years_list != null) {
            for (year in years_list){
                years.add(year.attr("value"))
            }
        }
        return years
    }

    fun parseMoneyData(input: ResponseBody?): String {
        val pretty = input?.string().toString()
        val doc: Document = Jsoup.parse(pretty.replace("&nbsp;", ""))
        val form = doc.select("form[action=https://ciu.nstu.ru/student_study/personal/money]").first()
        form?.select("div.selectric-wrapper")?.remove()
        form?.select("select")?.remove()
        form?.select("b")?.first()?.remove()
        form?.select("b")?.first()?.remove()
        form?.select("br")?.first()?.remove()
        form?.select("br")?.first()?.remove()
        return form.toString()

    }
}