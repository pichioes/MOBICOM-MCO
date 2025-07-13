/*package com.mobdeve.s17.mco2.group88

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView

fun NavbarActivity(
    navBar: BottomNavigationView,
    fragmentManager: FragmentManager,
    containerId: Int
) {
    navBar.setOnItemSelectedListener { item ->
        val selectedFragment: Fragment? = when (item.itemId) {
            R.id.home -> HomeFragment()
            R.id.profile -> ProfileFragment()
            R.id.settings -> SettingsFragment()
            else -> null
        }

        selectedFragment?.let {
            fragmentManager.beginTransaction()
                .replace(containerId, it)
                .commit()
        }

        true
    }
}
*/