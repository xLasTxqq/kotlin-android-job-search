package com.example.findwork

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.findwork.databinding.ActivityMainBinding
import com.example.findwork.ui.data.DataActiveFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private val dataModel: DataActiveFragment by viewModels()
//    private val dataModelVacancies: VacanciesViewModel by viewModels()
    //TODO очишать вакансии при создании и обновлении
    private lateinit var binding: ActivityMainBinding

    init {
        instance = this
    }

    companion object {
        private var instance: MainActivity? = null
        fun getContext(): Context {
            return instance!!.applicationContext
        }

        fun checkUser(Auth:Boolean) {
            val db = UsageDB(getContext()).readDB(null , null , null , "id" , 0)
            if (db.size > 0)
                this.instance?.renderMenu(db[0][2].toInt(),Auth)
            else
                this.instance?.renderMenu(2,Auth)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        renderMenu(2)
        checkUser(false)

//        val navView: BottomNavigationView = binding.navView
//        val navController = findNavController(R.id.nav_host_fragment_activity_main)
//        //nav_host_fragment_activity_main_activity
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
////                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_login, R.id.navigation_register
//                R.id.navigation_vacancies_fragment, R.id.navigation_login, R.id.navigation_register, R.id.navigation_login_employer, R.id.navigation_register_employer,
//                R.id.navigation_home
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
    }

    private fun renderMenu(id: Int, Auth:Boolean) {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_vacancies_fragment,
                R.id.navigation_login, R.id.navigation_register, R.id.navigation_login_employer,
                R.id.navigation_register_employer,  R.id.navigation_summary, R.id.navigation_new_vacancy,
                R.id.navigation_summaries,R.id.navigation_employer_vacancies
            //R.id.navigation_home,
            )
        )
//        binding.navHostFragmentActivityMain.adapter = Vp2Adapter(this, listOf(VacanciesFragment(),FiltersFragment()))
//        TabLayoutMediator
        navView.menu.clear()
//        navView.inflateMenu(0)
        navView.menu.add(Menu.NONE, R.id.navigation_vacancies_fragment, Menu.NONE, R.string.title_vacancies).setIcon(R.drawable.ic_dashboard_black_24dp)
        when (id) {
            0 -> {
                navView.menu.add(Menu.NONE, R.id.navigation_summaries, Menu.NONE, R.string.action_summaries).setIcon(R.drawable.ic_dashboard_black_24dp)
                navView.menu.add(Menu.NONE, R.id.navigation_summary, Menu.NONE, R.string.title_summary).setIcon(R.drawable.ic_dashboard_black_24dp)
            }
            1 -> {
                navView.menu.add(Menu.NONE, R.id.navigation_employer_vacancies, Menu.NONE, getString(R.string.title_employer_vacancies)).setIcon(R.drawable.ic_dashboard_black_24dp)
                navView.menu.add(Menu.NONE, R.id.navigation_new_vacancy, Menu.NONE, R.string.title_new_vacancy).setIcon(R.drawable.ic_dashboard_black_24dp)
            }
            else -> {
                navView.menu.add(Menu.NONE, R.id.navigation_login, Menu.NONE, R.string.title_login).setIcon(R.drawable.ic_dashboard_black_24dp)
                navView.menu.add(Menu.NONE, R.id.navigation_register, Menu.NONE, R.string.title_register).setIcon(R.drawable.ic_dashboard_black_24dp)
            }
        }
        setupActionBarWithNavController(navController , appBarConfiguration)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _ , destination , _ ->
            dataModel.activeFragment.value=destination.id
        }
        if(Auth){
            navController.popBackStack(navController.currentDestination?.id ?: R.id.navigation_vacancies_fragment ,true)
            navController.navigate(R.id.navigation_vacancies_fragment)
//            val vacancies = dataModelVacancies.response.value
//            val filters = dataModelVacancies.filtersResponse.value

            this.viewModelStore.clear()

//            dataModelVacancies.response.value = vacancies
//            dataModelVacancies.filtersResponse.value = filters
        }
        else
        dataModel.activeFragment.value?.let {
                navController.navigate(it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (findNavController(R.id.nav_host_fragment_activity_main).currentDestination?.id == R.id.navigation_home)
                    findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_vacancies_fragment)
                else findNavController(R.id.nav_host_fragment_activity_main).popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}