package io.github.morgaroth.android.mywork

import android.app.Fragment
import android.os.Bundle
import android.view.{ViewGroup, LayoutInflater, View}

class MainActivityFragment extends Fragment {

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
     inflater.inflate(R.layout.fragment_main, container, false)
  }
}
