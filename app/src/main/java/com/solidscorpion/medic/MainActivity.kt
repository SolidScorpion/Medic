package com.solidscorpion.medic

import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.webkit.WebViewClientCompat
import com.solidscorpion.medic.adapter.CustomArrayAdapter
import com.solidscorpion.medic.adapter.RVAdapter
import com.solidscorpion.medic.databinding.ActivityMainBinding
import com.solidscorpion.medic.pojo.BaseItem
import com.solidscorpion.medic.pojo.ModelMenuItem
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.drawer_layout.*


class MainActivity : AppCompatActivity(), MainActivityContract.View {

    private lateinit var binding: ActivityMainBinding
    private lateinit var presenter: MainActivityContract.Presenter
    private var spinnerCheck = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val application = (application as MedicApplication)
        presenter = MainActivityPresenter(this, application.api)
        binding.webview.webViewClient = object : WebViewClientCompat() {

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                binding.pbLoading.visibility = View.VISIBLE
                if (url != "https://dev.medic.co.il/?app") {
                    binding.toolbar.btnShare.visibility = View.VISIBLE
                }
                else {
                    binding.toolbar.btnShare.visibility = View.GONE
                }
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.pbLoading.visibility = View.GONE
                super.onPageFinished(view, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (!request.hasGesture()) return false
                binding.webview.loadUrl(StringBuilder()
                    .append(request.url)
                    .append("?app")
                    .toString())
                return true
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
                super.onReceivedSslError(view, handler, error)
            }
        }
        binding.drawerLayout.search.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && !TextUtils.isEmpty(v.text)) {
                presenter.performSearch(v.text, 0)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        binding.toolbar.btnShare.setOnClickListener { onShareClicked(binding.webview.url) }
        val settings = binding.webview.settings
        settings.javaScriptEnabled = true
        binding.webview.loadUrl("https://dev.medic.co.il/?app")
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
            setHomeAsUpIndicator(ResourcesCompat.getDrawable(resources, R.drawable.ic_menu_black_24dp, theme))
        }
        binding.drawerLayout.search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""
                presenter.performSearch(text, 1)
            }
        })
        binding.drawerLayout.closeDrawer.setOnClickListener { slideUp(binding.drawerLayout.drawerContainer) }
        binding.drawerLayout.menu.layoutManager = LinearLayoutManager(this)
        presenter.loadMenuItems()
        binding.drawerLayout.searchIcon.setOnClickListener { loadEmptySearch() }

    }

    override fun showResults(results: List<BaseItem>) {
        val adapter = CustomArrayAdapter(this, results)
        spinner.adapter = adapter
        spinner.performClick()
        spinnerCheck = 0
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(++spinnerCheck > 1) {
                    binding.webview.loadUrl(adapter.getBaseItem(position).url.plus("/?app"))
                    slideUp(binding.drawerLayout.drawerContainer)
                    spinnerCheck = 0
                }
            }
        }
    }

    private fun loadEmptySearch() {
        binding.webview.loadUrl("https://dev.medic.co.il/medic-search/${binding.drawerLayout.search.text}".plus("/?app"))
        slideUp(binding.drawerLayout.drawerContainer)
    }

    override fun showProgress() {
        binding.drawerLayout.searchProgress.visibility = View.VISIBLE
        binding.drawerLayout.search.isEnabled = false
    }

    override fun hideProgress() {
        binding.drawerLayout.searchProgress.visibility = View.GONE
        binding.drawerLayout.search.isEnabled = true
    }

    private fun onShareClicked(url: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url.substring(0, url.length - 5))
            type = "text/plain"
        }
        startActivity(sendIntent)
    }

    open fun openMain(view: View){
        slideUp(binding.drawerLayout.drawerContainer)
        binding.webview.loadUrl("https://dev.medic.co.il/?app")
    }

    override fun onStop() {
        presenter.onStop()
        super.onStop()
    }

    override fun onMenuItemsLoaded(items: List<ModelMenuItem>) {
        val adapter = RVAdapter(this, items) {
            if (it.link.length > 1 || it.link == "/") {
                binding.webview.loadUrl(
                    StringBuilder()
                        .append("https://dev.medic.co.il")
                        .append(it.link)
                        .append("?app")
                        .toString()
                )
                slideUp(binding.drawerLayout.drawerContainer)
            }
        }
        binding.drawerLayout.menu.adapter = adapter
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

    private fun slideUp(view: View) {
        view.animate().translationY(-view.height.toFloat()).setDuration(500).start()
    }

    private fun slideDown(view: View) {
        view.animate().translationY(0f).setDuration(500).start()
    }

}
