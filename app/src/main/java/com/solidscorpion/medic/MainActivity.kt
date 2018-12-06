package com.solidscorpion.medic

import android.net.http.SslError
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.webkit.WebViewClientCompat
import com.solidscorpion.medic.adapter.RVAdapter
import com.solidscorpion.medic.databinding.ActivityMainBinding
import com.solidscorpion.medic.pojo.ModelMenuItem


class MainActivity : AppCompatActivity(), MainActivityContract.View {
    override fun onMenuItemsLoaded(items: List<ModelMenuItem>) {
        val adapter = RVAdapter(this, items) {
            binding.webview.loadUrl(
                StringBuilder()
                    .append("https://dev.medic.co.il")
                    .append(it.link)
                    .append("?app")
                    .toString())
           slideUp(binding.drawerLayout.drawerContainer)
        }
        binding.drawerLayout.menu.adapter = adapter
    }


    private lateinit var binding: ActivityMainBinding
    private lateinit var presenter: MainActivityContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val api = (application as MedicApplication).api
        presenter = MainActivityPresenter(this, api)
        binding.webview.webViewClient = object : WebViewClientCompat() {

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (!request.hasGesture()) return false
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
                super.onReceivedSslError(view, handler, error)
            }
        }
        val settings = binding.webview.settings
        settings.javaScriptEnabled = true
        binding.webview.loadUrl("https://dev.medic.co.il/?app")
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
            setHomeAsUpIndicator(ResourcesCompat.getDrawable(resources, R.drawable.ic_menu_black_24dp, theme))
        }
        binding.drawerLayout.closeDrawer.setOnClickListener { slideUp(binding.drawerLayout.drawerContainer) }
        binding.drawerLayout.menu.layoutManager = LinearLayoutManager(this)
        presenter.loadMenuItems()
    }

    private fun slideUp(view: View) {
        view.animate().translationY(-view.height.toFloat()).setDuration(500).start()
    }

    private fun slideDown(view: View) {
        view.animate().translationY(0f).setDuration(500).start()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                slideDown(binding.drawerLayout.drawerContainer)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (binding.webview.canGoBack()) {
            binding.webview.goBack()
        } else super.onBackPressed()
    }
}
