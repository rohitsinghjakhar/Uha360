package com.dawnbellsuha.uha.studentdesk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dawnbellsuha.uha.R
import com.dawnbellsuha.uha.databinding.ActivityStudentDeskBinding
import com.dawnbellsuha.uha.fragments.BoardsListFragment

class StudentDeskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentDeskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDeskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
//        setSupportActionBar(binding.toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.title = "Student Desk"

        // Load the initial fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BoardsListFragment())
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}