package br.com.antoniomonteiro.checkdetector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import br.com.antoniomonteiro.checkdetector.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }
}