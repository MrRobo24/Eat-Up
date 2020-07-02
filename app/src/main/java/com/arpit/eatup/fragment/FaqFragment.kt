package com.arpit.eatup.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.arpit.eatup.R
import com.arpit.eatup.activity.MainActivity
import com.arpit.eatup.adapter.FaqRecyclerAdapter
import com.arpit.eatup.model.QA


class FaqFragment : Fragment() {

    lateinit var recyclerFaq: RecyclerView
    lateinit var recyclerAdapter: FaqRecyclerAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager

    val qnaList = arrayListOf<QA>(
        QA(
            "What is this app about?",
            "This app is called Eat Up. It is a food ordering app. It will help " +
                    "you fill your empty stomach anytime you want. This app will be your food partner in late night binges or early morning breakfasts. Enjoy!"
        ),
        QA(
            "Is this app free?",
            "Yes! Eat Up app is completely free of cost to download. You just have to pay for the food services you avail."
        ),
        QA(
            "Is this app safe to use?",
            "Yes of course! Eat up application is built with user's privacy in mind. You can be rest assured!"
        ),
        QA(
            "How much food can I order from this app?",
            "The quantity of dishes that can be ordered through this app is limited only by the menu of restaurant you want to order from. In simple " +
                    "words you can order all you want!"
        ),
        QA(
            "Is this app available on Play Store?",
            "Well, this app is currently not available on the Play Store but you will find it there too in the near future. Thanks for asking!"
        ),
        QA(
            "Can I run this app on a basic smartphone?",
            "There's nothing you should be worried about if you try to use this app on the low-end smartphone. We have got you covered! Enjoy!"
        )
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_faq, container, false)
        (activity as MainActivity).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)




        recyclerFaq = view.findViewById(R.id.recyclerFaq)
        recyclerAdapter = FaqRecyclerAdapter(activity as Context, qnaList)
        layoutManager = LinearLayoutManager(activity as Context)
        recyclerFaq.adapter = recyclerAdapter
        recyclerFaq.layoutManager = layoutManager

        return view
    }
}
