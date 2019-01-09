package com.solidscorpion.medic

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
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
import android.os.Build
import android.os.Handler
import android.view.inputmethod.InputMethodManager
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MainActivityContract.View {

    private lateinit var binding: ActivityMainBinding
    private lateinit var presenter: MainActivityContract.Presenter
    private var isMenuOpened = false
    private var spinnerCheck = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forceRTLIfSupported()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val application = (application as MedicApplication)
        presenter = MainActivityPresenter(this, application.api)
        binding.webview.webViewClient = object : WebViewClientCompat() {

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                binding.pbLoading.visibility = View.VISIBLE
                if (url != "https://dev.medic.co.il/?app") {
                    binding.toolbar.btnShare.visibility = View.VISIBLE
                    binding.toolbar.imgBack.visibility = View.VISIBLE
                    setCustomToolbar(100)
                } else {
                    binding.toolbar.btnShare.visibility = View.GONE
                    binding.toolbar.imgBack.visibility = View.GONE
                    setCustomToolbar(58)
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
        binding.toolbar.toolsearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && !TextUtils.isEmpty(v.text)) {
                val input = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                input.hideSoftInputFromWindow(toolsearch
                        .applicationWindowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS)
                Handler().postDelayed({
                    loadEmptySearch()
                }, 700)
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
            title = ""
        }
        binding.toolbar.toolsearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""
                presenter.performSearch(text, 1)
            }
        })
        binding.toolbar.toolsearch.typeface = Typeface.createFromAsset(assets,
                "fonts/IBMPlexSans-Text.ttf")
        binding.toolbar.toolsearch.textSize = 17F
        binding.drawerLayout.menu.layoutManager = LinearLayoutManager(this)
        presenter.loadMenuItems()
        binding.toolbar.searchIcon.setOnClickListener { loadEmptySearch() }
        binding.toolbar.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbar.btnHome.setOnClickListener {
            isMenuOpened = if (isMenuOpened) {
                if (binding.webview.url == "https://dev.medic.co.il/?app") {
                    setCustomToolbar(58)
                }
                enableScroll()
                slideUp(binding.drawerLayout.drawerContainer)
                false
            } else {
                if (binding.webview.url == "https://dev.medic.co.il/?app") {
                    setCustomToolbar(100)

                }
                disableScroll()
                slideDown(binding.drawerLayout.drawerContainer)
                true
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun forceRTLIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
    }

    override fun showResults(results: List<BaseItem>) {
        val adapter = CustomArrayAdapter(this, results)
        binding.toolbar.toolspinner.adapter = adapter
        binding.toolbar.toolspinner.performClick()
        spinnerCheck = 0
        binding.toolbar.toolspinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (++spinnerCheck > 1) {
                    binding.webview.loadUrl(adapter.getBaseItem(position).url)
                    slideUp(binding.drawerLayout.drawerContainer)
                    spinnerCheck = 0
                }
            }
        }
    }

    private fun loadEmptySearch() {
        slideUp(binding.drawerLayout.drawerContainer)
        binding.webview.loadUrl("https://dev.medic.co.il/medic-search/${binding.toolbar.toolsearch.text}".plus("/?app"))
    }

    private fun disableScroll() {
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = 0
        appbar.requestLayout()
    }

    private fun enableScroll() {
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
        appbar.requestLayout()
    }

    private fun setCustomToolbar(dp: Int) {
        if (dp == 100) {
            binding.toolbar.toolsearch.visibility = View.VISIBLE
            binding.toolbar.toolspinner.visibility = View.VISIBLE
            binding.toolbar.searchIcon.visibility = View.VISIBLE
        } else {
            binding.toolbar.toolsearch.visibility = View.GONE
            binding.toolbar.toolspinner.visibility = View.GONE
            binding.toolbar.searchIcon.visibility = View.GONE
        }
        setToolbarHeight(dp)
    }

    private fun setToolbarHeight(dp: Int) {
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        var scale = resources.displayMetrics.density
        val pixels = (dp * scale + 0.5f)
        params.height = pixels.toInt()
        appbar.requestLayout()
    }

    override fun showProgress() {
        binding.toolbar.toolsearchProgress.visibility = View.VISIBLE
        binding.toolbar.toolsearch.isEnabled = false
    }

    override fun hideProgress() {
        binding.toolbar.toolsearchProgress.visibility = View.GONE
        binding.toolbar.toolsearch.isEnabled = true
    }

    private fun onShareClicked(url: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url.substring(0, url.length - 5))
            type = "text/plain"
        }
        startActivity(sendIntent)
    }

    open fun openMain(view: View) {
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
        binding.toolbar.btnHome.setImageResource(R.drawable.ic_menu)
    }

    private fun slideDown(view: View) {
        view.animate().translationY(0f).setDuration(500).start()
        binding.toolbar.btnHome.setImageResource(R.drawable.ic_close_black_24dp)
    }

}
