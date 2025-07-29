package com.dawnbellsuha.uha

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dawnbellsuha.uha.adapters.DialogHelper
import com.dawnbellsuha.uha.adapters.SocialMediaHelper
import com.dawnbellsuha.uha.databinding.ActivityMainBinding
import com.dawnbellsuha.uha.models.HomeViewModel
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var dialogHelper: DialogHelper
    private lateinit var socialMediaHelper: SocialMediaHelper
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var homeViewModel: HomeViewModel

    companion object {
        private const val USER_PREFS = "user_prefs"
        private const val SELECTED_CLASS_KEY = "selected_class"
        private const val LOGOUT_DELAY = 2000L

        // Social Media URLs
        private const val INSTAGRAM_URL = "https://www.instagram.com/dawnbellsindia"
        private const val FACEBOOK_URL = "https://www.facebook.com/dawnbellsindia"
        private const val LINKEDIN_URL = "https://www.linkedin.com/company/dawnbells"
        private const val YOUTUBE_URL = "https://youtube.com/@dawnbells9721"

        // Website URLs
        private const val PRIVACY_URL = "https://dawnbells.com/policy.php"
        private const val TERMS_URL = "https://dawnbells.com/disclaimer.php"
        private const val FAQ_URL = "https://uha360.in/contact.html#faq"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeViews()
        setupNavigation()
        initializeHelpers()
        FirebaseApp.initializeApp(this)

        observeViewModel()

        // Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

    }

    private fun initializeViews() {
        setSupportActionBar(binding.toolbar)
        drawerLayout = binding.drawerLayout
        sharedPreferences = getSharedPreferences(USER_PREFS, MODE_PRIVATE)
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.profileFragment, R.id.resourcesFragment),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
        binding.bottomNav.setupWithNavController(navController)
        binding.navView.setNavigationItemSelectedListener(this)
    }

    private fun initializeHelpers() {
        dialogHelper = DialogHelper(this)
        socialMediaHelper = SocialMediaHelper(this)
    }



    private fun observeViewModel() {
        homeViewModel.selectedClass.observe(this) { selectedClass ->
            // Update fragments with the new class selection
            when (navController.currentDestination?.id) {
                R.id.homeFragment -> {
                    // The HomeFragment will observe this change itself
                }
            }
        }
    }

//    private fun showClassChangeSnackbar(className: String) {
//        Snackbar.make(binding.root, "Showing content for $className", Snackbar.LENGTH_SHORT)
//            .setAction("OK") { }
//            .setActionTextColor(ContextCompat.getColor(this, R.color.primary_blue))
//            .show()
//    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        handleNavigationItemClick(item.itemId)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleNavigationItemClick(itemId: Int) {
        when (itemId) {
            R.id.homeFragment, R.id.profileFragment, R.id.resourcesFragment -> {
                navigateToFragment(itemId)
            }
            R.id.nav_about -> dialogHelper.showAboutDialog()
            R.id.nav_contact -> dialogHelper.showContactDialog()
            R.id.nav_privacy -> dialogHelper.showPrivacyPolicyDialog { openWebUrl(PRIVACY_URL) }
            R.id.nav_terms -> dialogHelper.showTermsConditionsDialog { openWebUrl(TERMS_URL) }
            R.id.nav_help -> dialogHelper.showHelpSupportDialog { openWebUrl(FAQ_URL) }
            R.id.nav_instagram -> socialMediaHelper.openUrl(INSTAGRAM_URL)
            R.id.nav_facebook -> socialMediaHelper.openUrl(FACEBOOK_URL)
            R.id.nav_linkedin -> socialMediaHelper.openUrl(LINKEDIN_URL)
            R.id.nav_youtube -> socialMediaHelper.openUrl(YOUTUBE_URL)
//            R.id.nav_logout -> showLogoutConfirmation()
        }
    }

    private fun navigateToFragment(fragmentId: Int) {
        try {
            navController.navigate(fragmentId)
        } catch (e: Exception) {
            showSnackbar(getString(R.string.navigation_error))
        }
    }

    private fun openWebUrl(url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(browserIntent)
        } catch (e: Exception) {
            showSnackbar(getString(R.string.browser_error))
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setAction(getString(R.string.ok)) { }
            .setActionTextColor(ContextCompat.getColor(this, R.color.primary_blue))
            .show()
    }

//    private fun showLogoutConfirmation() {
//        MaterialAlertDialogBuilder(this)
//            .setTitle("Logout")
//            .setMessage("Are you sure you want to logout?")
//            .setPositiveButton("Logout") { _, _ ->
//                performLogout()
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }

//    private fun performLogout() {
//        lifecycleScope.launch {
//            // Show loading state
//            dialogHelper.showLoadingDialog("Logging out...")
//
//            // Simulate logout process
//            delay(LOGOUT_DELAY)
//
//            // Clear preferences and navigate to login
//            sharedPreferences.edit { clear() }
//            dialogHelper.dismissLoadingDialog()
//
//            val intent = Intent(this@MainActivity, LoginActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//            finish()
//        }
//    }
}