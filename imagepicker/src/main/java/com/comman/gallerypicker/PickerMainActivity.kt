package com.comman.gallerypicker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.comman.gallerypicker.databinding.ActivityMainPickerBinding
import com.comman.gallerypicker.models.MediaItemObj

class PickerMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainPickerBinding
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    companion object {
        var mediaItems: List<MediaItemObj> = mutableListOf()
        var selectedMediaItems: ArrayList<MediaItemObj> = ArrayList()
        var isSingleSelection = false
        var albumName = ""

        const val IMAGE_URI_LIST = "imageUriList"

        @SuppressLint("StaticFieldLeak")
        var mTVCount: TextView? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isSingleSelection = intent?.getBooleanExtra("isSingleSelection", false) == true

        mTVCount = binding.mTVTitle

        selectedMediaItems.clear()

        (mediaItems as ArrayList<MediaItemObj>).clear()

        setupNavigation()

        binding.mIVMoreOption.apply {
            setOnClickListener {

                if (!isSingleSelection) {
                    if (selectedMediaItems.size == 0 || selectedMediaItems.size < 6) {
                        Toast.makeText(
                            this@PickerMainActivity, "Please Select 6 Images..", Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }
                } else {
                    if (selectedMediaItems.size == 0) {
                        Toast.makeText(
                            this@PickerMainActivity,
                            "Please Select Atleast 1 Images..",
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }
                }


                if (isSingleSelection) {
                    /*                  Intent().putExtra("path", selectedMediaItems[0].pathRaw)
                                              .putExtra("imageUri", selectedMediaItems[0].uri)*/
                    setResult(
                        RESULT_OK,
                        Intent().putExtras(Bundle().apply {
                            putParcelableArrayList(IMAGE_URI_LIST, selectedMediaItems)
                        })
                    )
                    finish()
                } else {

                    setResult(
                        RESULT_OK, Intent().putExtras(Bundle().apply {
                            putParcelableArrayList(IMAGE_URI_LIST, selectedMediaItems)
                        })
                    )
                    finish()
                }


            }
        }

        binding.mIVBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupNavigation()
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun setupNavigation() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.picker_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (navController.currentDestination?.id == R.id.albumPickerInfoFragment) {
                navController.popBackStack()
            } else if (navController.currentDestination?.id == R.id.albumPickerFragment) {
                finish()
            }
        }
    }

}