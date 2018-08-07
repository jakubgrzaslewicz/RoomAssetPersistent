package jakubgrzaslewicz.pl.androidroomasset

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        InitializeDatabase()
    }

    private fun InitializeDatabase() {
        MainDatabase.getInstance(this@MainActivity).Test().getAll().forEach{
            Log.d("DatabaseReader", it.Value)
        }
    }
}
