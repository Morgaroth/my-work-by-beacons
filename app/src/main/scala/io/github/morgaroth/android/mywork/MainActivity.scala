package io.github.morgaroth.android.mywork

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.{FloatingActionButton, Snackbar}
import android.view.{Menu, MenuItem, View}

class MainActivity extends Activity {
  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val fab: FloatingActionButton = findViewById(R.id.fab).asInstanceOf[FloatingActionButton]
    fab.setOnClickListener(new View.OnClickListener() {
      def onClick(view: View) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()
      }
    })
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.menu_main, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    val id: Int = item.getItemId
    if (id == R.id.action_settings) {
      true
    } else {
      super.onOptionsItemSelected(item)
    }
  }
}
