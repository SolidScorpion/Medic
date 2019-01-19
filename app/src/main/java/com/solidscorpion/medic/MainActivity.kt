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
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MainActivityContract.View, AppBarLayout.OnOffsetChangedListener {

    private lateinit var binding: ActivityMainBinding

    private val FULL_TOOLBAR_HEIGHT_DP = 100
    private val SMALL_TOOLBAR_HEIGHT_DP = 58

    private lateinit var presenter: MainActivityContract.Presenter
    private var isMenuOpened = false
    private var isSearchIcon = true
    private var toolbarOffset = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forceRTLIfSupported()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.appbar.addOnOffsetChangedListener(this)
        val application = (application as MedicApplication)
        presenter = MainActivityPresenter(this, application.api)
        binding.webview.webViewClient = object : WebViewClientCompat() {

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                binding.pbLoading.visibility = View.VISIBLE
                if (url != "https://dev.medic.co.il/?app") {
                    binding.toolbar.btnShare.visibility = View.VISIBLE
                    binding.toolbar.imgBack.visibility = View.VISIBLE
                    setCustomToolbar(FULL_TOOLBAR_HEIGHT_DP)
                } else {
                    binding.toolbar.btnShare.visibility = View.GONE
                    binding.toolbar.imgBack.visibility = View.GONE
                    setCustomToolbar(SMALL_TOOLBAR_HEIGHT_DP)
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
        binding.toolbar.autocomplete.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && !TextUtils.isEmpty(v.text)) {
                onSearchClicked()
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
        binding.toolbar.autocomplete.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""
                if (autocomplete.isPopupShowing) {
                    binding.toolbar.searchIcon.setImageDrawable(getDrawable(R.drawable.ic_close_black_24dp))
                    isSearchIcon = false
                }
                if (!text.isEmpty()) {
                    presenter.performSearch(text, 0)
                } else {
                    binding.toolbar.searchIcon.setImageDrawable(getDrawable(R.drawable.tinted_drawable))
                    isSearchIcon = true
                }
            }
        })
        binding.toolbar.autocomplete.typeface = Typeface.createFromAsset(assets,
                "fonts/IBMPlexSans-Text.ttf")
        binding.toolbar.autocomplete.textSize = 17F
        binding.drawerLayout.menu.layoutManager = LinearLayoutManager(this)
        presenter.loadMenuItems()
        binding.toolbar.searchIcon.setOnClickListener {
            if (!TextUtils.isEmpty(binding.toolbar.autocomplete.text)) {
                if (isSearchIcon) {
                    onSearchClicked()
                } else {
                    binding.toolbar.autocomplete.dismissDropDown()
                    (autocomplete.adapter as CustomArrayAdapter).clear()
                    isSearchIcon = true
                    binding.toolbar.searchIcon.setImageDrawable(getDrawable(R.drawable.tinted_drawable))
                }

            }
        }
        binding.toolbar.imgBack.setOnClickListener { onBackPressed() }
        binding.toolbar.btnHome.setOnClickListener {
            isMenuOpened = if (isMenuOpened) {
                hideKeyboard()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (imm.isAcceptingText) {
                    Handler().postDelayed({
                        if (binding.webview.url == "https://dev.medic.co.il/?app") {
                            setCustomToolbar(SMALL_TOOLBAR_HEIGHT_DP)
                        }
                        slideUp(binding.drawerLayout.drawerContainer)
                    }, 700)
                } else {
                    if (binding.webview.url == "https://dev.medic.co.il/?app") {
                        setCustomToolbar(SMALL_TOOLBAR_HEIGHT_DP)
                    }
                    slideUp(binding.drawerLayout.drawerContainer)
                }
                false
            } else {
                hideKeyboard()
                if (binding.webview.url == "https://dev.medic.co.il/?app") {
                    setCustomToolbar(FULL_TOOLBAR_HEIGHT_DP)

                }
                slideDown(binding.drawerLayout.drawerContainer)
                true
            }
        }
    }

    private fun onSearchClicked() {
        hideKeyboard()
        isMenuOpened = false
        presenter.onStop()
        Handler().postDelayed({
            loadEmptySearch()
            clearSearch()
        }, 700)
    }

    private fun clearSearch() {
        binding.toolbar.autocomplete.setText("")
        binding.toolbar.autocomplete.clearFocus()
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun forceRTLIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
    }

    override fun showResults(results: List<BaseItem>) {
        val adapter = CustomArrayAdapter(this, results)
        binding.toolbar.autocomplete.setAdapter(adapter)
        adapter.notifyDataSetChanged()
        binding.toolbar.autocomplete.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                hideKeyboard()
                clearSearch()
                presenter.onStop()
                isMenuOpened = false
                Handler().postDelayed({
                    binding.webview.loadUrl(adapter.getBaseItem(p2).url)
                    slideUp(binding.drawerLayout.drawerContainer)
                }, 700)
            }
        }
        if (results.isNotEmpty() && autocomplete.text.isNotEmpty()) {
            binding.toolbar.searchIcon.setImageDrawable(getDrawable(R.drawable.ic_close_black_24dp))
            isSearchIcon = false
        } else {
            binding.toolbar.searchIcon.setImageDrawable(getDrawable(R.drawable.tinted_drawable))
            isSearchIcon = true
        }
    }

    private fun hideKeyboard() {
        val input = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        input.hideSoftInputFromWindow(autocomplete
                .applicationWindowToken,
                InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun loadEmptySearch() {
        slideUp(binding.drawerLayout.drawerContainer)
        binding.webview.loadUrl("https://dev.medic.co.il/medic-search/${binding.toolbar.autocomplete.text}".plus("/?app"))
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
        if (dp == FULL_TOOLBAR_HEIGHT_DP) {
            toolbarOffset = calcOffset(FULL_TOOLBAR_HEIGHT_DP)
            binding.toolbar.autocomplete.visibility = View.VISIBLE
            binding.toolbar.searchIcon.visibility = View.VISIBLE
        } else {
            toolbarOffset = calcOffset(SMALL_TOOLBAR_HEIGHT_DP)
            binding.toolbar.autocomplete.visibility = View.GONE
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

    private fun calcOffset(dp: Int): Int {
        var scale = resources.displayMetrics.density
        val pixels = (dp * scale + 0.5f)
        return pixels.toInt() * -1
    }

    override fun showProgress() {
//        binding.toolbar.toolsearchProgress.visibility = View.VISIBLE
    }

    override fun hideProgress() {
//        binding.toolbar.toolsearchProgress.visibility = View.GONE
    }

    private fun onShareClicked(url: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url.substring(0, url.length - 5))
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "Share using?"))
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
        val adapter = RVAdapter(this, items,
                onClick = {
                    if (it.link.length > 1 || it.link == "/") {
                        binding.webview.loadUrl(
                                StringBuilder()
                                        .append("https://dev.medic.co.il")
                                        .append(it.link)
                                        .toString()
                        )
                        slideUp(binding.drawerLayout.drawerContainer)
                    }
                },
                onSignUpClick = {
                    binding.webview.loadUrl(
                        StringBuilder()
                            .append("https://dev.medic.co.il/subscribe")
                            .append("/?app")
                            .toString())
                    slideUp(binding.drawerLayout.drawerContainer)
        })
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
        enableScroll()
        view.animate().translationY(-view.height.toFloat()).setDuration(500).start()
        binding.toolbar.btnHome.setImageResource(R.drawable.ic_menu)
    }

    private fun slideDown(view: View) {
        disableScroll()
        view.animate().translationY(0f).setDuration(500).start()
        binding.toolbar.btnHome.setImageResource(R.drawable.ic_close_black_24dp)
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        if (verticalOffset == toolbarOffset) {
            binding.webview.loadUrl("javascript:ShowInfoLineHeader()")
        } else if (verticalOffset > toolbarOffset) {
            binding.webview.loadUrl("javascript:HideInfoLineHeader()")
        }
    }

}
