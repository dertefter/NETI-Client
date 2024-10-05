package com.dertefter.neticore.NETICoreClient

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.dertefter.neticore.TinyDB
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.data.Person
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit

//это пиздец... так делать нельзя
class PersonHelper(application: Application) {
    var tinyDB : TinyDB = TinyDB(application.applicationContext)
    fun retrivePerson(link: String?, liveData: MutableLiveData<Person?>){
        if (link.isNullOrEmpty()){
           return
        }
        if (getPersonData(link) != null){
            val person = getPersonData(link)
            showPerson(person, liveData)
        }
        CoroutineScope(Dispatchers.IO).launch {

            try{
                val retrofit = Retrofit.Builder()
                    .baseUrl(link)
                    .build()
                val service = retrofit.create(APIService::class.java)
                val response = service.basePage()
                if (response.isSuccessful){
                    val pretty = response.body()?.string().toString()
                    val doc: Document = Jsoup.parse(pretty)
                    val name = doc.select("div.page-title").first()?.text().toString()
                    val image = doc.select("div.contacts__card-image").first()?.select("img")?.first()?.attr("src")
                    val imageLink = "https://ciu.nstu.ru/kaf/$image"
                    val address = doc.select("div.contacts__card-address").first()?.ownText()
                    var mail = ""//doc.select("div.contacts__card-email").first()?.html().toString()
                    if (!doc.select("a.mailto").attr("data-email-1").isNullOrEmpty()){
                        val before = doc.select("a.mailto").attr("data-email-1").toString()
                        val after = doc.select("a.mailto").attr("data-email-2").toString()
                        mail = "$before@$after"
                    }
                    val time = doc.select("div.contacts__card-time").first()?.ownText()
                    var about_disc = doc.select("div.about_disc").html()
                    var lastIndex = about_disc.lastIndexOf("<br>")
                    if (lastIndex != -1) {
                        about_disc = about_disc.removeRange(lastIndex, lastIndex + 4)
                    }

                    var profiles = doc.select("div.col-9").html()
                    var profilesLastIndex = profiles.lastIndexOf("<br>")
                    if (profilesLastIndex != -1) {
                        profiles = profiles.removeRange(profilesLastIndex, profilesLastIndex + 4)
                    }

                    val personPost = doc.select("div.contacts__card-post").first()?.ownText().toString()

                    val hasTimetable = doc.html().contains("расписание занятий", true)
                    val person = Person(name, toShortName(name), mail, link, imageLink, hasTimetable, address, time, about_disc, personPost, profiles)
                    Log.e("truepp", person.toString())
                    showPerson(person, liveData)
                    savePersonData(person)
                }
            }
            catch (_: Exception){ }
            catch (e: Error) { }
            catch (e: Throwable) { }
            catch (e: JSONException) { }


        }
    }

    fun retrivePersonByName(name: String?, liveData: MutableLiveData<Person?>){
        if (name.isNullOrEmpty()){
            return
        }
        Log.e("name", name.toString())
        if (getPersonDataByName(name) != null){
            val person = getPersonDataByName(name)
            showPerson(person, liveData)
        }else{
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val url = "https://www.nstu.ru/"
                    val retrofit = Retrofit.Builder().baseUrl(url).build()
                    val service = retrofit.create(APIService::class.java)
                    val response = service.findPerson(name.split(".")[0], "1")
                    val parsedData = ResponseParser().parsePersonList(response.body())
                    if (parsedData.isNotEmpty()){
                        val item = parsedData[0]
                        val person = Person(
                            item.name,
                            toShortName(item.name),
                            item.mail,
                            item.site,
                            item.pic,
                            item.hasTimetable
                        )
                        savePersonData(person, true)
                        showPerson(person, liveData)
                    }
                }
                catch (_: Exception){ }
                catch (e: Error) { }
                catch (e: Throwable) { }
                catch (e: JSONException) { }
            }
        }

    }

    fun showPerson(person: Person?, liveData: MutableLiveData<Person?>) {
        liveData.postValue(person)
    }

    fun getPersonDataByName(shortName: String): Person? {
        var lastSave: MutableList<Any> = tinyDB.getListObject("personListNamed", Person::class.java).toMutableList()
        Log.e("lastSave", lastSave.toString())
        if (lastSave.isEmpty()){
            return null
        }
        val p = lastSave.find { (it as Person).shortName == shortName }
        if (p == null){
            return null
        }else{
            return p as Person
        }
    }
    fun getPersonData(link: String): Person? {
        var lastSave: MutableList<Any> = tinyDB.getListObject("personList", Person::class.java).toMutableList()
        if (lastSave.isEmpty()){
            return null
        }
        val p = lastSave.find { (it as Person).site == link }
        if (p == null){
            return null
        }else{
            return p as Person
        }
    }

    fun toShortName(fullName: String): String {
        val parts = fullName.split(" ")
        if (parts.size < 3) {
            return fullName
        }
        val lastName = parts[0]
        val firstNameInitial = parts[1].first().toString()
        val patronymicInitial = parts[2].first().toString()

        return "$lastName $firstNameInitial.$patronymicInitial."
    }

    fun savePersonData(person: Person, isNamed: Boolean = false){
        val key = if (isNamed) "personListNamed" else "personList"
        var lastSave = tinyDB.getListObject(key, Person::class.java).toMutableList()

        if (lastSave.isEmpty()) {
            lastSave = mutableListOf()
        }
        val existingIndex = if (isNamed){
            lastSave.indexOfFirst { it is Person && it.name == person.name }
        }else{
            lastSave.indexOfFirst { it is Person && it.site == person.site }
        }
        if (existingIndex != -1) {
            lastSave[existingIndex] = person
        } else {
            lastSave.add(person)
        }
        tinyDB.putListObject(key, lastSave as java.util.ArrayList<Any>)
    }

}
