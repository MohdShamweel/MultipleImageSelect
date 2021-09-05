package com.shamweel.multipleimageselect

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.shamweel.multipleimageselect.fragments.AlbumsFragment
import com.shamweel.multipleimageselect.fragments.ImageFragment
import com.shamweel.multipleimageselect.helpers.Constants
import com.shamweel.multipleimageselect.helpers.GlobalVariables
import com.shamweel.multipleimageselect.helpers.ImageHelper
import com.shamweel.multipleimageselect.model.AlbumData


class MultipleImageSelectActivity : AppCompatActivity(), AlbumsFragment.FragmentListener,
    ImageFragment.ImageSelectedListener {

    private lateinit var actionBar: ActionBar
    private lateinit var toolbar: Toolbar
    private lateinit var btnPermission: ExtendedFloatingActionButton
    private lateinit var txtPermission: TextView
    private lateinit var colorAnimation: ValueAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_image_select)

        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        btnPermission = findViewById(R.id.btn_permission)
        txtPermission = findViewById(R.id.txtPermission)

        setSupportActionBar(toolbar)
        actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        actionBar.setDisplayShowTitleEnabled(true)
        actionBar.setTitle(R.string.album_view)



        if (intent == null) {
            finish()
        }
        GlobalVariables.currentLimit =
            intent.getIntExtra(Constants.INTENT_EXTRA_LIMIT, Constants.DEFAULT_LIMIT)
        GlobalVariables.isUriRequired =
            intent.getBooleanExtra(Constants.INTENT_GET_URI, Constants.DEFAULT_URI_REQUIRED)

        requestPermission()
        btnPermission.setOnClickListener { requestPermissionLauncher.launch(Constants.REQUIRED_READ_PERMISSION) }
    }

    private fun beginFragmentTransaction() {
        btnPermission.visibility = View.GONE
        txtPermission.visibility = View.GONE
        addFragment(AlbumsFragment.newInstance(this))
    }

    override fun onAlbumSelectedListener(albumData: AlbumData, view: View) {
        addFragment(ImageFragment.newInstance(albumData.title, view.transitionName, this), view)
    }

    private fun addFragment(fragment: Fragment, view: View) {
        supportFragmentManager
            .beginTransaction()
            .addSharedElement(view, view.transitionName)
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun addFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onImageSelectedToggleListener() {
        //AppBarColorChanges
        val selected: Boolean = ImageHelper.arrayList.size > 0

        //Highlight
        if (selected) {
            var colorFrom =
                ContextCompat.getColor(applicationContext, R.color.multiple_image_select_dark_900)
            var colorTo =
                ContextCompat.getColor(applicationContext, R.color.multiple_image_select)
            toColorFade(colorFrom, colorTo)

        } else {
            colorAnimation.cancel()
            toolbar.setBackgroundColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.toolbarBackgroundColor
                )
            )
        }

        //Title and ImageView
        for (i in 0 until toolbar.childCount) {
            var view = toolbar.getChildAt(i)
            when {
                view is TextView -> {

                    view.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            if (selected) R.color.white else R.color.dark_50
                        )
                    )
                }
                view is ImageView -> {
                    view.imageTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                applicationContext, if (selected) R.color.white else R.color.dark_50
                            )
                        )
                }
            }

        }
    }

    private fun toColorFade(colorFrom: Int, colorTo: Int) {
        colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 250 // milliseconds
        colorAnimation.addUpdateListener { animator -> toolbar.setBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.start()
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount <= Constants.DEFAULT_FRAGMENT_STACK) {
            setResult(RESULT_CANCELED)
            finish()
        } else {
            super.onBackPressed()
        }
    }


    private fun requestPermission() {
        when (PackageManager.PERMISSION_GRANTED
        ) {
            ContextCompat.checkSelfPermission(
                this,
                Constants.REQUIRED_READ_PERMISSION
            ) -> {
                beginFragmentTransaction()
            }
            else -> {
                requestPermissionLauncher.launch(
                    Constants.REQUIRED_READ_PERMISSION
                )
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                beginFragmentTransaction()
            } else {
                Toast.makeText(applicationContext, R.string.permission_denied, Toast.LENGTH_LONG)
                    .show()
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + applicationContext.packageName)
                    )
                )
            }
        }
}

