package com.dertefter.ficus

import android.animation.ObjectAnimator
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.util.Log
import android.util.TimeUtils
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.get
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dertefter.ficus.databinding.ActivityMainBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.AuthorizationState
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.local.AppPreferences
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.android.material.navigationrail.NavigationRailView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.activity.addCallback
import androidx.core.os.BuildCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dataClient: DataClient
    var netiCore: NETICore? = null
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_FicusRebuild)
        appPreferences = AppPreferences
        if (appPreferences.monet != false){
            DynamicColors.applyToActivityIfAvailable(this)

        }
        enableEdgeToEdge()

        var keepSplashOnScreen = true
        val delay = 600L

        installSplashScreen().setKeepOnScreenCondition { keepSplashOnScreen }
        Handler(Looper.getMainLooper()).postDelayed({ keepSplashOnScreen = false }, delay)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        netiCore = (application as Ficus).netiCore

        dataClient = Wearable.getDataClient(this)


        setupNavigation()

        //run neticore and check auth
        observeAuthState()
        observeDiAuthState()
        observeGroupInfo()
        netiCore?.checkAuthorization()

        binding.dispaceButton?.setOnClickListener {
            changeSpace()
        }
    }


    override fun onPause() {
        super.onPause()
        netiCore?.client?.scheduleViewModel?.currentGroupLiveData?.removeObservers(this)
        netiCore?.client?.scheduleViewModel?.weeksLiveData?.removeObservers(this)
    }

    private fun sendGroupToWear(groupTitle: String) {
        try{
            val putDataReq: PutDataRequest = PutDataMapRequest.create("/group_info").run {
                dataMap.putString("group", groupTitle)
                asPutDataRequest()
            }
            val putDataTask: Task<DataItem> = dataClient.putDataItem(putDataReq).apply {
                addOnCompleteListener { Log.e("putDataTask", "OnComplete") }
                addOnSuccessListener { Log.e("putDataTask", "OnSuccess")  }
                addOnFailureListener { Log.e("putDataTask", "OnFail")  }
            }
        }catch (_: Exception){}

    }

    fun setupNavigation(){
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        var animation: ObjectAnimator? = null
        if (binding.nav is BottomNavigationView){ //BottomNavigationView
            (binding.nav as BottomNavigationView).setupWithNavController(navController)
            animation = ObjectAnimator.ofFloat(binding.navLayout, "translationY", 0f,1000f).apply {
                duration = 400
                doOnStart {
                    binding.navHostFragment.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        bottomToBottom = binding.parent.id
                        bottomToTop = ConstraintLayout.LayoutParams.UNSET
                    }
                }
                doOnEnd {
                    binding.navLayout.visibility = View.GONE
                    binding.navLayout.translationY = 0f
                }

            }
        }
        if (binding.nav is NavigationRailView){ //BottomNavigationView
            (binding.nav as NavigationRailView).setupWithNavController(navController)
            animation = ObjectAnimator.ofFloat(binding.navLayout, "translationX", 0f,-1000f).apply {
                duration = 400
                doOnStart {
                    binding.navHostFragment.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        startToStart = binding.parent.id
                        startToEnd = ConstraintLayout.LayoutParams.UNSET
                    }
                }
                doOnEnd {
                    binding.navLayout.visibility = View.GONE
                    binding.navLayout.translationX = 0f
                }

            }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideNavIds = listOf(R.id.authFragment,
                R.id.searchGroupFragment,
                R.id.searchPersonFragment,
                R.id.readNewsFragment,
                R.id.readMessageFragment,
                R.id.settingsFragment,
                R.id.personPageFragment,
                R.id.personScheduleFragment,
                R.id.diChatViewFragment,
                R.id.messagesChatViewFragment,
                R.id.filesFragment,
                R.id.aboutFragment,
                R.id.sessiaResultsFragment,
                R.id.moneyFragment,
                R.id.campusPassFragment,
                R.id.courseViewFragment)
            val diSpaceIds = listOf(R.id.diChatViewFragment, R.id.messagesChatViewFragment, R.id.courseViewFragment, R.id.diSpaceCources)
            if (destination.label!!.contains("di", true)){
                if (binding.dispaceButton.text.contains("di", true)){
                    if (binding.nav is BottomNavigationView){
                        binding.dispaceButton?.text = "Личный кабинет"
                        (binding.nav as BottomNavigationView).menu.clear()
                        (binding.nav as BottomNavigationView).inflateMenu(R.menu.bottom_nav_menu_dispace)
                    }else if (binding.nav is NavigationRailView){
                        binding.dispaceButton?.text = "ЛК"
                        (binding.nav as NavigationRailView).menu.clear()
                        (binding.nav as NavigationRailView).inflateMenu(R.menu.bottom_nav_menu_dispace)
                    }
                }



            }
            else{
                if (!binding.dispaceButton.text.contains("di", true)){
                    if (binding.nav is BottomNavigationView){
                        binding.dispaceButton?.text = "DiSpace"
                        (binding.nav as BottomNavigationView).menu.clear()
                        (binding.nav as BottomNavigationView).inflateMenu(R.menu.menu_bottom_nav)
                    }else if (binding.nav is NavigationRailView){
                        binding.dispaceButton?.text = "Di"
                        (binding.nav as NavigationRailView).menu.clear()
                        (binding.nav as NavigationRailView).inflateMenu(R.menu.menu_bottom_nav)
                    }
                }

            }

            if (!hideNavIds.contains(destination.id)){
                animation?.cancel()
                if (binding.nav is BottomNavigationView){
                    binding.navHostFragment.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                        bottomToTop = binding.navLayout.id
                    }
                }else if (binding.nav is NavigationRailView){
                    binding.navHostFragment.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        startToStart = ConstraintLayout.LayoutParams.UNSET
                        startToEnd = binding.navLayout.id
                    }
                }
                binding.navLayout.visibility = View.VISIBLE

            }else{
                if (R.id.settingsFragment == destination.id){
                    binding.navLayout!!.visibility = View.GONE
                }
                animation?.start()
            }

        }



    }

    fun changeSpace(){
        if (binding.dispaceButton?.text == "DiSpace" ||binding.dispaceButton?.text == "Di"){
            if (binding.nav is BottomNavigationView){
                binding.dispaceButton?.text = "Личный кабинет"
                (binding.nav as BottomNavigationView).menu.clear()
                (binding.nav as BottomNavigationView).inflateMenu(R.menu.bottom_nav_menu_dispace)
                (binding.nav as BottomNavigationView).selectedItemId = (binding.nav as BottomNavigationView).menu[0].itemId
            }else if (binding.nav is NavigationRailView){
                binding.dispaceButton?.text = "ЛК"
                (binding.nav as NavigationRailView).menu.clear()
                (binding.nav as NavigationRailView).inflateMenu(R.menu.bottom_nav_menu_dispace)
                (binding.nav as NavigationRailView).selectedItemId = (binding.nav as NavigationRailView).menu[0].itemId
            }

        }
        else{
            if (binding.nav is BottomNavigationView){
                binding.dispaceButton?.text = "DiSpace"
                (binding.nav as BottomNavigationView).menu.clear()
                (binding.nav as BottomNavigationView).inflateMenu(R.menu.menu_bottom_nav)
                (binding.nav as BottomNavigationView).selectedItemId = (binding.nav as BottomNavigationView).menu[0].itemId
            }else if (binding.nav is NavigationRailView){
                binding.dispaceButton?.text = "Di"
                (binding.nav as NavigationRailView).menu.clear()
                (binding.nav as NavigationRailView).inflateMenu(R.menu.menu_bottom_nav)
                (binding.nav as NavigationRailView).selectedItemId = (binding.nav as NavigationRailView).menu[0].itemId
            }

        }
    }

    fun observeAuthState(){
        lifecycleScope.launch {
            netiCore?.client?.authorizationStateViewModel?.uiState?.collect {
                when(it){
                    AuthorizationState.AUTHORIZED -> {
                        if (netiCore?.client?.isAuthorized != true){
                            netiCore?.client?.initialize(true)
                            createNotificationSnackbar("Успешный вход в аккаунт!", "Авторизация прошла успешно", "success")
                        }
                        observeUserInfo()
                        netiCore?.getUserInfo()
                        netiCore?.diSpaceClient?.initialize()
                        netiCore?.diSpaceClient?.auth()


                    }
                    AuthorizationState.AUTHORIZED_WITH_ERROR -> {
                        netiCore?.client?.initialize()
                        createNotificationSnackbar("Ошибка авторизации!", "Проверьте логин и пароль", "error")
                        binding.dispaceButton!!.isEnabled = false
                    }
                    AuthorizationState.UNAUTHORIZED -> {
                        netiCore?.client?.initialize()
                        binding.dispaceButton!!.isEnabled = false
                    }
                    AuthorizationState.LOADING -> {
                        if (netiCore?.client?.isAuthorized != true){
                            createNotificationSnackbar("Авторизация...", "Подождите чуть-чуть...", "loading")
                            binding.dispaceButton!!.isEnabled = false
                        }

                    }
                }
            }
        }
    }

    fun observeDiAuthState(){
        lifecycleScope.launch {
            netiCore?.diSpaceClient?.authorizationStateViewModel?.uiState?.collect {
                when(it){
                    AuthorizationState.AUTHORIZED -> {
                        binding.dispaceButton!!.isEnabled = true
                    }
                    AuthorizationState.AUTHORIZED_WITH_ERROR -> {
                        binding.dispaceButton!!.isEnabled = false
                    }
                    AuthorizationState.UNAUTHORIZED -> {
                        binding.dispaceButton!!.isEnabled = false
                    }
                    AuthorizationState.LOADING -> {
                        binding.dispaceButton!!.isEnabled = false

                    }
                }
            }
        }
    }

    fun observeGroupInfo(){
        netiCore?.client?.scheduleViewModel?.currentGroupLiveData?.observe(this){
            if (it != null && it.title.isNotEmpty()){
                sendGroupToWear(it.title)
            }
        }
    }

    fun observeUserInfo(){
        netiCore?.client?.userInfoViewModel?.userInfoLiveData?.observe(this){
            when (it.status){
                Status.SUCCESS -> {
                    if (netiCore?.client?.scheduleViewModel?.currentGroupLiveData?.value == null){
                        netiCore?.setGroup(it.data?.group)
                    }

                }
                Status.ERROR -> {
                    createNotificationSnackbar("Ошибка получения данных аккаунта!", "Проверьте подключение к интернету", "error")
                }
                Status.LOADING -> {

                }
            }
        }
    }

    fun createNotificationSnackbar(title: String, text: String, type: String){
        binding.notificationSnackbar.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(binding.notificationSnackbar, "translationY", 100f, 0f).start()
        if (type == "success"){
            binding.notificationProgressBar?.visibility = View.INVISIBLE
            binding.notificationImage.visibility = View.VISIBLE
            binding.notificationImage.setImageResource(R.drawable.done)
        }else if (type == "error"){
            binding.notificationProgressBar?.visibility = View.INVISIBLE
            binding.notificationImage.visibility = View.VISIBLE
            binding.notificationImage.setImageResource(R.drawable.info)
        }else if (type == "loading"){
            binding.notificationProgressBar?.visibility = View.VISIBLE
            binding.notificationImage.visibility = View.GONE
        }
        binding.notificationTitle.text = title
        binding.notificationText.text = text
        ObjectAnimator.ofFloat(binding.notificationSnackbar, "translationY", 0f, 500f).apply {
            startDelay = 3000
            doOnEnd {
                binding.notificationSnackbar.visibility = View.INVISIBLE

            }
            start()

        }
    }

}