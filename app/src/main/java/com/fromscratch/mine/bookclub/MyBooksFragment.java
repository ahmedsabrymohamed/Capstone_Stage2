package com.fromscratch.mine.bookclub;



import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.fromscratch.mine.bookclub.Adapters.BooksListAdapter;
import com.fromscratch.mine.bookclub.Classes.BookClub;
import com.fromscratch.mine.bookclub.Classes.LastMessage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;


public class MyBooksFragment extends Fragment  implements BooksListAdapter.SetOncLickListener{

    private static final String ARG_PARAM1 = "Uid";



    private OnSelectedListenerMyBooks mListener;
    private ArrayList<BookClub> myBookClubArrayList;
    private HashMap<String,BookClub> myBookClubMap;
    private String uid;
    private BooksListAdapter listAdapter;
    RecyclerView recyclerView;
    LinearLayoutManager manager;
    int position;

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref ;


    public MyBooksFragment() {
        // Required empty public constructor
    }



    public static MyBooksFragment newInstance(String param1) {
        MyBooksFragment fragment = new MyBooksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myBookClubMap =new HashMap<>();
        if (getArguments() != null) {
            uid = getArguments().getString(ARG_PARAM1);

        }
        myBookClubArrayList =new ArrayList<>();
        ref = database.getReference("clubsData");
        listAdapter=new BooksListAdapter(getActivity(),this) ;
        if(savedInstanceState!=null)
        {
            ArrayList<BookClub>clubs=savedInstanceState.getParcelableArrayList("mySelectedClubs");
            for(BookClub club:clubs)
                myBookClubMap.put(club.getClubId(),club);


        }
        getData();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root=inflater.inflate(R.layout.fragment_my_books, container, false);
        manager=new LinearLayoutManager(getActivity());
        recyclerView=root.findViewById(R.id.myBooks_list);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(listAdapter);
        if(savedInstanceState!=null)
            position=savedInstanceState.getInt("listPosition");
        else
            position=0;

        return root;
    }







    @Override
    public void SetOnclick(BookClub club) {
        mListener.onBookClubClicked(club);
    }

    @Override
    public void OnSelectChange(HashMap<String,BookClub> bookClubsSelected) {
        myBookClubMap =bookClubsSelected;
        mListener.onSelectedChangeMyBooks(bookClubsSelected);
    }


    public void onStart() {

        super.onStart();


    }
    public interface OnSelectedListenerMyBooks {
        void onSelectedChangeMyBooks(HashMap<String,BookClub> bookClubsSelected);

        void onBookClubClicked(BookClub bookClub);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectedListenerMyBooks) {
            mListener = (OnSelectedListenerMyBooks) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList("mySelectedClubs",new ArrayList<>(myBookClubMap.values()));
        outState.putInt("listPosition",
                manager.findFirstCompletelyVisibleItemPosition());
        super.onSaveInstanceState(outState);
    }
    public void getData(){

        ref.orderByChild("Users/"+uid).equalTo(true).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                myBookClubArrayList.clear();
                for (final DataSnapshot postSnapshot: dataSnapshot.getChildren()) {


                    BookClub bookClub=postSnapshot.getValue(BookClub.class);
                    bookClub.setClubId(postSnapshot.getKey());
                    myBookClubArrayList.add(bookClub);

                }
                listAdapter.refresh();
                listAdapter.setBookClubs(myBookClubMap, myBookClubArrayList);
                recyclerView.scrollToPosition(position);
               // Log.d("ahmed123", "onDataChange: "+position);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                //   Log.d("ahmed", "onCancelled: "+databaseError.getMessage());
            }
        });
    }

}


