package com.shamweel.multipleimageselect.fragments

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shamweel.multipleimageselect.R
import com.shamweel.multipleimageselect.adapter.AlbumAdapter
import com.shamweel.multipleimageselect.model.AlbumData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class AlbumsFragment : Fragment(), AlbumAdapter.AlbumListener {

    private var albums: ArrayList<AlbumData> = ArrayList()

    private lateinit var layoutProgressBar: ConstraintLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var albumAdapter: AlbumAdapter
    private lateinit var mContext: Context
    private var recylerViewState : Parcelable? = null


    private val projection = arrayOf(
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media._ID
    )

    companion object {
        private lateinit var albumSelectedListener: FragmentListener

        @JvmStatic
        fun newInstance(fragmentListener: FragmentListener) =
            AlbumsFragment().apply {
                setListener(fragmentListener)
            }

        private fun setListener(fragmentListener: FragmentListener) {
            albumSelectedListener = fragmentListener
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onStart() {
        super.onStart()
        setAppBarTitle()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_album, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        layoutProgressBar = view.findViewById(R.id.layout_progress)
        recyclerView = view.findViewById(R.id.recyclerview)
        initializeRecyclerview()

        lifecycleScope.launch(Dispatchers.Main) {
            layoutProgressBar.visibility = View.VISIBLE
            albums.clear()
            albums.addAll(loadData())
            albumAdapter.notifyDataSetChanged()
            orientationBasedUI(resources.configuration.orientation)
            layoutProgressBar.visibility = View.GONE
            recyclerView.layoutManager?.onRestoreInstanceState(recylerViewState);
            startPostponedEnterTransition()
        }
    }

    override fun onPause() {
        super.onPause()
        recylerViewState = recyclerView.getLayoutManager()?.onSaveInstanceState()!!
    }

    private fun setAppBarTitle() {
        (context as AppCompatActivity).supportActionBar!!.title =
            requireActivity().resources.getString(R.string.album_view)
    }

    private fun initializeRecyclerview(): Boolean {
        albumAdapter = AlbumAdapter(
            mContext,
            albums,
            this
        )
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(mContext, 2)
        recyclerView.adapter = albumAdapter
        return true
    }

    @SuppressLint("Range")
    suspend fun loadData(): ArrayList<AlbumData> = withContext(Dispatchers.IO) {
        var albums: ArrayList<AlbumData> = ArrayList()
        val cursor = mContext.contentResolver
            .query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                null, null, MediaStore.Images.Media.DATE_ADDED
            )
        val temp: java.util.ArrayList<AlbumData> = java.util.ArrayList<AlbumData>(cursor!!.count)
        val albumSet = HashSet<String>()
        var file: File
        if (cursor.moveToLast()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                val uri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                val album = cursor.getString(cursor.getColumnIndex(projection[0]))
                val image = cursor.getString(cursor.getColumnIndex(projection[1]))

                file = File(image)
                if (file.exists() && !albumSet.contains(album)) {
                    temp.add(AlbumData(uri, album))
                    albumSet.add(album)
                }
            } while (cursor.moveToPrevious())
        }
        cursor.close()

        albums.clear()
        albums.addAll(temp)
        return@withContext albums
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        orientationBasedUI(newConfig.orientation)
    }

    private fun orientationBasedUI(orientation: Int) {
        val windowManager = mContext.getSystemService(WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val size =
            if (orientation == Configuration.ORIENTATION_PORTRAIT) metrics.widthPixels / 2 else metrics.widthPixels / 4
        albumAdapter.setLayoutParams(size)
        recyclerView.layoutManager = GridLayoutManager(
            mContext,
            if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 4
        )

    }

    override fun onAlbumClick(albumData: AlbumData, view: View) {
        albumSelectedListener.onAlbumSelectedListener(albumData, view)
    }


    interface FragmentListener {
        fun onAlbumSelectedListener(albumData: AlbumData, view: View)
    }

}