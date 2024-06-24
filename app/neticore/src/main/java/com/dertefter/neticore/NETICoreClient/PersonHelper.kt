package com.dertefter.neticore.NETICoreClient

import android.app.Application
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dertefter.neticore.TinyDB
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.data.Person
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit

//это пиздец... так делать нельзя
class PersonHelper(application: Application) {
    var tinyDB : TinyDB = TinyDB(application.applicationContext)
    fun retrivePerson(link: String?, nameView: TextView? = null, avatarView: ImageView? = null, needFullName: Boolean = false){
        if (link.isNullOrEmpty()){
           return
        }
        if (getPersonData(link) != null){
            val person = getPersonData(link)
            val imageLink = person?.pic
            if (needFullName){
                nameView?.text = person?.name
            }else{
                nameView?.text = toShortName(person?.name!!)
            }

            avatarView?.visibility = View.VISIBLE
            Picasso.get().load(imageLink).resize(200,200).centerCrop().into(avatarView)
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
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
                val person = Person(name, null, link, imageLink, )
                savePersonData(person)
                withContext(Dispatchers.Main){
                    if (needFullName){
                        nameView?.text = person.name
                    }else{
                        nameView?.text = toShortName(person.name)
                    }
                    Picasso.get().load(imageLink).resize(200,200).centerCrop().into(avatarView)
                    avatarView?.visibility = View.VISIBLE
                }
            }
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
        if (parts.size != 3) {
            return fullName
        }
        val lastName = parts[0]
        val firstNameInitial = parts[1].first().toString()
        val patronymicInitial = parts[2].first().toString()

        return "$lastName $firstNameInitial. $patronymicInitial."
    }

    fun savePersonData(person: Person){
        var lastSave = tinyDB.getListObject("personList", Person::class.java).toMutableList()
        if (lastSave.isEmpty()){
            lastSave = mutableListOf<Any>()
        }
        lastSave.add(person)
        tinyDB.putListObject("personList", lastSave as java.util.ArrayList<Any>)
    }
}