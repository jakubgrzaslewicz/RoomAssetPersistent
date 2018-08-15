package jakubgrzaslewicz.pl.roomassetpersistent

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        InitializeDatabase()
    }

    private fun InitializeDatabase() {
        MainDatabase.getInstance(this@MainActivity).Test().getAll().forEach{
            textView.text = it.Value
        }
    }
}
