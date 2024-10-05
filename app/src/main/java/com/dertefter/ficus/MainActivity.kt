package com.dertefter.ficus

import android.animation.ObjectAnimator
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.transition.TransitionManager
import com.dertefter.ficus.databinding.ActivityMainBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.AuthorizationState
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.local.AppPreferences
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import com.google.android.material.navigationrail.NavigationRailView
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dataClient: DataClient
    var netiCore: NETICore? = null
    lateinit var appPreferences: AppPreferences
    var keepSplashOnScreen = true
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_FicusRebuild)
        appPreferences = AppPreferences
        if (appPreferences.monet != false){
            DynamicColors.applyToActivityIfAvailable(this)

        }

        enableEdgeToEdge()



        val delay = 600L

        installSplashScreen().setKeepOnScreenCondition { keepSplashOnScreen }
        //Handler(Looper.getMainLooper()).postDelayed({ keepSplashOnScreen = false }, delay)

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
            showOrHideSwapSpace()
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
        val navOptions = NavOptions.Builder()
        navOptions.setEnterAnim(R.animator.nav_default_enter_anim).setExitAnim(R.animator.nav_default_exit_anim)
            .setPopEnterAnim(R.animator.nav_default_pop_enter_anim).setPopExitAnim(R.animator.nav_default_pop_exit_anim)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        if (binding.nav is BottomNavigationView){
            window.navigationBarColor = MaterialColors.getColor(binding.nav, com.google.android.material.R.attr.colorSurfaceContainer)
            (binding.nav as BottomNavigationView).setupWithNavController(navController)
            (binding.nav as BottomNavigationView).setOnItemSelectedListener{ item ->
                if (item.itemId != (binding.nav as BottomNavigationView).selectedItemId) {
                    navController.popBackStack(item.itemId, inclusive = true, saveState = false)
                    navController.navigate(item.itemId, null, navOptions.build())
                }
                true
            }
        }
        else if (binding.nav  is NavigationRailView){
            window.navigationBarColor = MaterialColors.getColor(binding.nav, com.google.android.material.R.attr.colorSurface)
            (binding.nav as NavigationRailView).setupWithNavController(navController)
            (binding.nav as NavigationRailView).setOnItemSelectedListener{ item ->
                if (item.itemId != (binding.nav as NavigationRailView).selectedItemId) {
                    navController.popBackStack(item.itemId, inclusive = true, saveState = false)
                    navController.navigate(item.itemId)
                }
                true
            }
        }
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            ViewCompat.setOnApplyWindowInsetsListener(binding.navLayout) { v, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                v.updatePadding(insets.left, insets.top, 0, insets.bottom)

                binding.navHostFragment.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = insets.bottom
                    rightMargin = insets.right
                }

                WindowInsetsCompat.CONSUMED
            }
        }
    }

    fun showOrHideSwapSpace(){
        val tr = androidx.transition.Fade()
        tr.duration = 140L
        TransitionManager.beginDelayedTransition(binding?.changeSpaceLayout!!, tr)
        if (binding?.changeSpaceLayout?.visibility == View.VISIBLE){
            binding?.changeSpaceLayout?.visibility = View.GONE
        }else{
            binding?.changeSpaceLayout?.visibility = View.VISIBLE
        }
        binding.toDi!!.setOnClickListener { changeSpace("DiSpace")  }
        binding.toLK!!.setOnClickListener { changeSpace("LK")  }

    }

    fun changeSpace(s: String){
        netiCore?.client?.messagesViewModel?.updateMessagesCount()
        if (s == "DiSpace"){
            if (binding.nav is BottomNavigationView){
                (binding.nav as BottomNavigationView).menu.clear()
                (binding.nav as BottomNavigationView).inflateMenu(R.menu.bottom_nav_menu_dispace)
                (binding.nav as BottomNavigationView).selectedItemId = (binding.nav as BottomNavigationView).menu[1].itemId
                (binding.nav as BottomNavigationView).selectedItemId = (binding.nav as BottomNavigationView).menu[0].itemId
            }else if (binding.nav is NavigationRailView){
                (binding.nav as NavigationRailView).menu.clear()
                (binding.nav as NavigationRailView).inflateMenu(R.menu.bottom_nav_menu_dispace)
                (binding.nav as NavigationRailView).selectedItemId = (binding.nav as NavigationRailView).menu[1].itemId
                (binding.nav as NavigationRailView).selectedItemId = (binding.nav as NavigationRailView).menu[0].itemId
            }

        }
        else{
            if (binding.nav is BottomNavigationView){
                (binding.nav as BottomNavigationView).menu.clear()
                (binding.nav as BottomNavigationView).inflateMenu(R.menu.menu_bottom_nav)
                (binding.nav as BottomNavigationView).selectedItemId = (binding.nav as BottomNavigationView).menu[1].itemId
                (binding.nav as BottomNavigationView).selectedItemId = (binding.nav as BottomNavigationView).menu[0].itemId
            }else if (binding.nav is NavigationRailView){
                (binding.nav as NavigationRailView).menu.clear()
                (binding.nav as NavigationRailView).inflateMenu(R.menu.menu_bottom_nav)
                (binding.nav as NavigationRailView).selectedItemId = (binding.nav as NavigationRailView).menu[1].itemId
                (binding.nav as NavigationRailView).selectedItemId = (binding.nav as NavigationRailView).menu[0].itemId
            }

        }

    }

    fun observeAuthState(){
        lifecycleScope.launch {
            netiCore?.client?.authorizationStateViewModel?.uiState?.collect {
                Log.e("ddddddd", it.toString())
                when(it){
                    AuthorizationState.AUTHORIZED -> {
                        keepSplashOnScreen = false
                        if (netiCore?.client?.isAuthorized != true){
                            netiCore?.client?.initialize(true)
                            createNotificationSnackbar("Успешный вход в аккаунт!", "Авторизация прошла успешно", "success")
                        }
                        observeUserInfo()
                        netiCore?.getUserInfo()
                        observeMessagesCount()
                        netiCore?.client?.messagesViewModel?.updateMessagesCount()
                        netiCore?.diSpaceClient?.initialize()
                        netiCore?.diSpaceClient?.auth()


                    }
                    AuthorizationState.AUTHORIZED_WITH_ERROR -> {
                        keepSplashOnScreen = false
                        netiCore?.client?.initialize()
                        createNotificationSnackbar("Ошибка авторизации!", "Проверьте логин и пароль", "error")
                        binding.dispaceButton!!.isEnabled = false
                    }
                    AuthorizationState.UNAUTHORIZED -> {
                        keepSplashOnScreen = false
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
                Log.e("dispaceeee", it.toString())
                when(it){
                    AuthorizationState.AUTHORIZED -> {
                        binding.dispaceButton.isEnabled = true
                    }
                    AuthorizationState.AUTHORIZED_WITH_ERROR -> {
                        binding.dispaceButton.isEnabled = false
                    }
                    AuthorizationState.UNAUTHORIZED -> {
                        binding.dispaceButton.isEnabled = false
                    }
                    AuthorizationState.LOADING -> {
                        binding.dispaceButton.isEnabled = false

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

    fun observeMessagesCount(){
        netiCore?.client?.messagesViewModel?.messagesCountLiveData?.observe(this){
            Log.e("mescount", it.toString())
            when (it.status){
                Status.SUCCESS -> {
                    if (binding.nav is BottomNavigationView){
                        if (it.data.isNullOrEmpty() || it.data!![0].isNullOrEmpty()){
                            (binding.nav as BottomNavigationView).removeBadge(R.id.messagesFragment)
                        }else{
                            try{
                                (binding.nav as BottomNavigationView).removeBadge(R.id.messagesFragment)
                                var badge = (binding.nav as BottomNavigationView).getOrCreateBadge(R.id.messagesFragment)
                                badge.isVisible = true
                                badge.text = it.data!![0]
                            } catch (e: Exception){}
                        }
                    }else if (binding.nav is NavigationRailView){
                        if (it.data.isNullOrEmpty() || it.data!![0].isNullOrEmpty()){
                            (binding.nav as NavigationRailView).removeBadge(R.id.messagesFragment)
                        }else{
                            try{
                                (binding.nav as NavigationRailView).removeBadge(R.id.messagesFragment)
                                var badge = (binding.nav as NavigationRailView).getOrCreateBadge(R.id.messagesFragment)
                                badge.isVisible = true
                                badge.text = it.data!![0]
                            } catch (e: Exception){}
                        }
                    }

                }
                Status.ERROR -> {
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