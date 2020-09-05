package com.darklinden.fml

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.darklinden.fml.data.SecFileItem
import kotlinx.android.synthetic.main.fragment_detail.view.*

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [ItemListActivity]
 * in two-pane mode (on tablets) or a [ItemDetailActivity]
 * on handsets.
 */
class ItemDetailFragment : Fragment() {

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: SecFileItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            //            if (it.containsKey(ARG_ITEM_ID)) {
//                // Load the dummy content specified by the fragment
//                // arguments. In a real-world scenario, use a Loader
//                // to load content from a content provider.
//                item = DummyContent.ITEM_MAP[it.getString(ARG_ITEM_ID)]
////                activity?.toolbar_layout?.title = item?.content
//            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_detail, container, false)

        rootView.et_title.text =
        // Show the dummy content as text in a TextView.
//        item?.let {
//            rootView.item_detail.text = it.title
//        }

        return rootView
    }
}
