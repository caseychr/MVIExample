package com.codingwithmitch.mviexample.ui.main

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codingwithmitch.mviexample.R
import com.codingwithmitch.mviexample.model.BlogPost
import com.codingwithmitch.mviexample.ui.DataStateListener
import com.codingwithmitch.mviexample.ui.main.state.MainStateEvent.*
import com.codingwithmitch.mviexample.util.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_main.*
import java.util.ArrayList

class MainFragment : Fragment(),
    MainRecyclerAdapter.Interaction
{
    private val TAG: String = "AppDebug"

    override fun onItemSelected(position: Int, item: BlogPost) {
        viewModel.setBlogPost(item)
        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(
                R.id.fragment_container,
                DetailFragment::class.java,
                null
                )
            ?.addToBackStack("DetailFragment")
            ?.commit()
    }

    private lateinit var viewModel: MainViewModel

    private lateinit var dataStateHandler: DataStateListener

    private lateinit var mainRecyclerAdapter: MainRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        subscribeObservers()

        if(savedInstanceState == null){
            triggerGetBlogsEvent()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = activity?.run {
            ViewModelProvider(this).get(MainViewModel::class.java)
        }?: throw Exception("Invalid Activity")

        savedInstanceState?.let { inState ->
            (inState["blog_posts"] as ArrayList<BlogPost>?)?.let{ blogList ->
                viewModel.setBlogListData(blogList)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("blog_posts", viewModel.viewState.value?.blogPosts)
    }

    private fun initRecyclerView(){
        main_recycler_view.apply {
            layoutManager = LinearLayoutManager(this@MainFragment.context)
            val topSpacingDecorator = TopSpacingItemDecoration(30)
            removeItemDecoration(topSpacingDecorator) // does nothing if not applied already
            addItemDecoration(topSpacingDecorator)

            val requestOptions = RequestOptions
                .placeholderOf(R.drawable.default_image)
                .error(R.drawable.default_image)

            mainRecyclerAdapter = MainRecyclerAdapter(
                this@MainFragment,
                Glide.with(this@MainFragment)
                    .applyDefaultRequestOptions(requestOptions)
            )

            adapter = mainRecyclerAdapter
        }
    }

    private fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->

            if(dataState != null){
                // Handle Loading and Message
                dataStateHandler.onDataStateChange(dataState)

                // handle Data<T>
                dataState.data?.let{ event ->
                    event.getContentIfNotHandled()?.let{ mainViewState ->

                        println("DEBUG: DataState: ${mainViewState}")

                        viewModel.setBlogListData(mainViewState.blogPosts)
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer {viewState ->

            if(viewState != null){
                // set BlogPosts to RecyclerView
                println("DEBUG: Setting blog posts to RecyclerView: ${viewState.blogPosts}")
                mainRecyclerAdapter.submitList(
                    list = viewState.blogPosts
                )
            }
        })
    }

    fun triggerGetBlogsEvent(){
        viewModel.setStateEvent(GetBlogPostsEvent())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            dataStateHandler = context as DataStateListener
        }catch(e: ClassCastException){
            println("$context must implement DataStateListener")
        }
    }

}














