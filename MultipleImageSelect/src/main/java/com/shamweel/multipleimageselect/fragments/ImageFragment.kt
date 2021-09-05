package com.shamweel.multipleimageselect.fragments

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.transition.TransitionInflater
import android.util.DisplayMetrics
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.shamweel.multipleimageselect.R
import com.shamweel.multipleimageselect.adapter.ImageAdapter
import com.shamweel.multipleimageselect.helpers.Constants
import com.shamweel.multipleimageselect.helpers.CopyToScopeStorageHelper
import com.shamweel.multipleimageselect.helpers.GlobalVariables
import com.shamweel.multipleimageselect.helpers.ImageHelper
import com.shamweel.multipleimageselect.model.ImageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class ImageFragment : Fragment(), ImageAdapter.ImageListener {

    private var album: String? = null
    private var transitionName: String? = null
    private var images: ArrayList<ImageData> = ArrayList()

    private lateinit var layoutProgressBar: ConstraintLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    lateinit var mContext: Context
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATA
    )

    companion object {
        private lateinit var listener: ImageSelectedListener

        @JvmStatic
        fun newInstance(album: String, transitionName: String, listener: ImageSelectedListener) =
            ImageFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.BUNDLE_EXTRA_ALBUM, album)
                    putString(Constants.TRANSITION_NAME, transitionName)
                }
                setListener(listener)
            }

        fun setListener(listener: ImageSelectedListener) {
            this.listener = listener
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sharedElementEnterTransition =
                TransitionInflater.from(context).inflateTransition(android.R.transition.move);
        }
        arguments?.let {
            album = it.getString(Constants.BUNDLE_EXTRA_ALBUM)
            transitionName = it.getString(Constants.TRANSITION_NAME)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAppBarTitle()
        recyclerView = view.findViewById(R.id.recyclerview)
        layoutProgressBar = view.findViewById(R.id.layout_progress)
        handler = Handler(Looper.getMainLooper())


        initializeRecyclerview()
        lifecycleScope.launch {
            layoutProgressBar.visibility = View.VISIBLE
            images.addAll(loadData())
            adapter.notifyDataSetChanged()
           // adapter.setTName(transitionName!!)
            recyclerView.transitionName = transitionName
            orientationBasedUI(resources.configuration.orientation)
            layoutProgressBar.visibility = View.GONE
            startPostponedEnterTransition()
        }


        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (ImageHelper.arrayList.size > 0) {
                        ImageHelper.arrayList.clear()
                        adapter.notifyDataSetChanged()
                        (context as AppCompatActivity).invalidateOptionsMenu()
                        setAppBarTitle()
                        listener.onImageSelectedToggleListener()
                    } else {
                        activity?.supportFragmentManager?.popBackStack()
                    }
                }
            })

    }

    fun setAppBarTitle() {
        (context as AppCompatActivity).supportActionBar!!.title =
            if (ImageHelper.arrayList.size > 0) "${ImageHelper.arrayList.size} selected" else "Tap to select images"
    }


    private fun initializeRecyclerview(): Boolean {
        adapter = ImageAdapter(
            mContext,
            images,
            this
        )
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(mContext, 2)
        recyclerView.adapter = adapter
        return true

    }

    private suspend fun loadData(): ArrayList<ImageData> = withContext(Dispatchers.IO) {
        var images: ArrayList<ImageData> = ArrayList()

        val cursor = mContext.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?",
            arrayOf(album),
            MediaStore.Images.Media.DATE_ADDED
        )

        var file: File
        var temp: ArrayList<ImageData> = ArrayList(cursor!!.count)

        if (cursor.moveToLast()) {
            do {
                var id: Long = cursor.getLong(cursor.getColumnIndex(projection[0]))
                var name = cursor.getString(cursor.getColumnIndex(projection[1]))
                var path = cursor.getString(cursor.getColumnIndex(projection[2]))

                var uri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                file = File(path)
                if (file.exists()) {
                    temp.add(ImageData(id, name, uri.toString()))
                }

            } while (cursor.moveToPrevious())
        }
        cursor.close()
        images.addAll(temp)
        return@withContext images
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        orientationBasedUI(newConfig.orientation)
    }

    private fun orientationBasedUI(orientation: Int) {
        val windowManager =
            mContext.applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val size =
            if (orientation == Configuration.ORIENTATION_PORTRAIT) metrics.widthPixels / 3 else metrics.widthPixels / 6
        adapter.setLayoutParams(size)
        recyclerView.layoutManager = GridLayoutManager(
            mContext,
            if (orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 6
        )
    }

    override fun onImageLongClick(view: View, imageData: ImageData) {
        var dialog: PreviewDialogFragment = PreviewDialogFragment.newInstance(imageData)
        dialog.show(childFragmentManager, "tag")


        view.setOnTouchListener { view, motionEvent ->
            closeDialog(motionEvent, dialog)
            return@setOnTouchListener false
        }

        recyclerView.setOnTouchListener { view, motionEvent ->
            closeDialog(motionEvent, dialog)
            return@setOnTouchListener false
        }
    }

    private fun closeDialog(
        motionEvent: MotionEvent,
        dialog: PreviewDialogFragment
    ) {
        if (motionEvent.action == MotionEvent.ACTION_UP) {
            dialog.dialog?.dismiss()
        }
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.menu_item_add_image).isVisible = ImageHelper.arrayList.size > 0
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_action_bar, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ImageHelper.arrayList.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_add_image -> {
                sendIntent()
            }
            R.id.home -> activity?.onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun sendIntent() {

        lifecycleScope.launch {
            if (!GlobalVariables.isUriRequired) {
                layoutProgressBar.visibility = View.VISIBLE
                var list = copyToScopeStorage()
                layoutProgressBar.visibility = View.GONE
            }
            val intent = Intent()
            intent.putExtra(Constants.INTENT_EXTRA_IMAGES, ImageHelper.arrayList)
            activity?.setResult(Activity.RESULT_OK, intent)
            activity?.finish()
        }


    }

    private suspend fun copyToScopeStorage(): ArrayList<ImageData> = withContext(Dispatchers.IO) {
        //Convert Files Uri's to Paths
        var arrayList: ArrayList<ImageData> = ArrayList()
        for (imageData in ImageHelper.arrayList) {
            var newImageData: ImageData = imageData
            newImageData.Uri = CopyToScopeStorageHelper.copyFileToInternalStorage(
                activity?.applicationContext!!,
                Uri.parse(imageData.Uri),
                "Sent"
            )
            arrayList.add(newImageData)
        }
        return@withContext arrayList
    }


    override fun onImageSelectedListChange(imageData: ImageData, remove: Boolean) {
        (context as AppCompatActivity).invalidateOptionsMenu()
        setAppBarTitle()
        listener.onImageSelectedToggleListener()

        if (remove){
            handler.removeCallbacksAndMessages(null)
            runnable = Runnable { resetSelectedTexts() }
            handler.postDelayed(runnable, Constants.RESET_SELECTED_LONG_MILLIS)
        }
    }

    private fun resetSelectedTexts(){
        //Notifying Selected Items Incremented Text on Item Remove to all the items present in the list
            /*for (i in 0 until ImageHelper.arrayList.size) {
                var index =
                    ImageHelper.getSelectedIndexInAdapter(images, ImageHelper.arrayList[i]);
                if (index != -1) adapter.notifyItemChanged(index)
            }*/

        //Sometimes an extra slide gets selected and currently unable to find the issue so resetting/notifying all the items
        adapter.notifyDataSetChanged()
    }

    interface ImageSelectedListener {
        fun onImageSelectedToggleListener()
    }


}