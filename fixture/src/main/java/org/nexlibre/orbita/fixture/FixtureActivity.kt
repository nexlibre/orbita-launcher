package org.nexlibre.orbita.fixture
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
class FixtureActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContentView(TextView(this).apply { text = "Aplicación de prueba abierta por ÓRBITA" }) }
}
